package citexplore.offlinedownload.downloader;

import citexplore.offlinedownload.Resource;
import citexplore.offlinedownload.ResourceStatus;
import citexplore.offlinedownload.ResourceStorage;
import citexplore.offlinedownload.manager.DownloaderNodeTest;
import com.sun.net.httpserver.HttpServer;
import org.apache.hadoop.fs.Path;
import org.apache.htrace.fasterxml.jackson.databind.JsonNode;
import org.apache.htrace.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;


/**
 * 离线下载工具测试类。
 *
 * @author Zhu, Sichuang
 */
public class DownloaderTest {

    // **************** 公开变量

    // **************** 私有变量

    /**
     * Log4j logger。
     */
    private static Logger logger = LogManager.getLogger(DownloaderTest.class);

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 清理riak，hdfs和kafka。
     */
    @BeforeClass
    public static void cleanAllBefore() {
        DownloadJobTest.cleanAll();
    }

    /**
     * 清理riak，hdfs和kafka。
     */
    @After
    public void cleanAllAfter() {
        DownloadJobTest.cleanAll();
    }

    /**
     * 测试判断下载完成功能。
     */
    @Test
    public void testDownloaded() {
        String url1 = "zhusichuang";
        String url2 = "liudali";
        String url3 = "lipengfei";

        Resource resource1 = new Resource(url1);
        resource1.status = ResourceStatus.finished;
        ResourceStorage.instance.put(resource1);

        Resource resource2 = new Resource(url2);
        resource1.status = ResourceStatus.downloadFailed;
        ResourceStorage.instance.put(resource2);

        Resource resource3 = new Resource(url3);
        ResourceStorage.instance.put(resource3);

        Assert.assertTrue(Downloader.instance.downloaded(url1));
        Assert.assertFalse(Downloader.instance.downloaded(url3));
        Assert.assertFalse(Downloader.instance.downloaded(url3));
        Assert.assertFalse(Downloader.instance.downloaded("liuxinwei"));
    }

    /**
     * 多线程测试下载同一个链接。
     */
    @Test
    public void teatDownloadMultiThread() {
        String ret = "<html><body>" + "<h4>123</h4><table border=\"1\">" +
                "<tr><th>456</th>" + "<th>789</th>" + "<th>123</th>" +
                "</tr><tr><td>Bill Gates</td>" + "<td>555 77 854</td>" +
                "<td>555 77 855</td>" + "</tr></table>" + "<h4>sdsd</h4>" +
                "<table border=\"1\"><tr>" + "<th>sdwe</th>" + "<td>Bill " +
                "Gates</td>" + "</tr><tr>" + "<th>ffe</th>" + "<td>555 77 " +
                "854</td>" + "</tr><tr><th>ere</th>" + "<td>555 77 855</td>"
                + "</tr></table></body></html>";
        HttpServer httpServer = DownloadJobTest.downloadContext
                (DownloaderNodeTest.httpServer(54345), ret);
        httpServer.start();

        String url = "http://localhost:54345/download.jsp";
        Resource resource = new Resource(url);
        resource.time = new Timestamp(System.currentTimeMillis());
        resource.relativePath = "test/resource/" + (short) url.hashCode();
        ResourceStorage.instance.put(resource);

        for (int i = 0; i < 10; i++) {
            new Thread() {
                @Override
                public void run() {
                    Downloader.instance.download(url);
                }
            }.start();
        }

        Consumer<Integer, String> consumer = JobFinishedInformerTest
                .subscribe(JobFinishedInformerTest.consumer());
        ConsumerRecords<Integer, String> records = consumer.poll(Long
                .MAX_VALUE);
        for (ConsumerRecord<Integer, String> record : records) {
            try {
                JsonNode jsonNode = new ObjectMapper().readTree(record.value());
                Assert.assertEquals(url, jsonNode.get("url").asText());
                Assert.assertEquals("downloadFinished", jsonNode.get
                        ("command").asText());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }

        Resource resource2 = ResourceStorage.instance.get(url);
        Assert.assertEquals(resource.url, resource2.url);
        Assert.assertEquals(resource.time, resource2.time);
        Assert.assertEquals(resource.relativePath, resource2.relativePath);
        Assert.assertEquals(ResourceStatus.finished, resource2.status);

        consumer.close();
        httpServer.stop(0);
    }

    /**
     * 测试多线程同时下载多个链接。
     */
    @Test
    public void testDownloadWithMultiUrls() {
        String ret = "<html><body>" + "<h4>表头：</h4><table border=\"1\">" +
                "<tr><th>姓名</th>" + "<th>电话</th>" + "<th>电话</th>" +
                "</tr><tr><td>Bill Gates</td>" + "<td>555 77 854</td>" +
                "<td>555 77 855</td>" + "</tr></table>" + "<h4>垂直的表头：</h4>" +
                "<table border=\"1\"><tr>" + "<th>姓名</th>" + "<td>Bill " +
                "Gates</td>" + "</tr><tr>" + "<th>电话</th>" + "<td>555 77 " +
                "854</td>" + "</tr><tr><th>电话</th>" + "<td>555 77 855</td>" +
                "</tr></table></body></html>";

        HttpServer httpServer = DownloaderNodeTest.httpServer(54345);

        Thread[] downloaderThreads = new Thread[100];

        Set<String> urls = new HashSet<String>();
        Set<String> urls1 = new HashSet<String>();
        Resource[] resources = new Resource[downloaderThreads.length];
        for (int i = 0; i < downloaderThreads.length; i++) {
            DownloaderNodeTest.context(httpServer, "/download" + i + ".jsp",
                    ret);
            resources[i] = new Resource("http://localhost:54345/download" + i
                    + ".jsp");
            resources[i].time = new Timestamp(System.currentTimeMillis());
            resources[i].relativePath = "test/resource/" + (short)
                    ("/download" + i + ".jsp").hashCode();
            ResourceStorage.instance.put(resources[i]);
            urls.add(resources[i].url);
        }

        httpServer.start();

        for (int i = 0; i < downloaderThreads.length; i++) {
            int j = i;
            downloaderThreads[i] = new Thread() {
                @Override
                public void run() {
                    Downloader.instance.download
                            ("http://localhost:54345/download" + j + ".jsp");
                }
            };
        }

        for (int i = 0; i < downloaderThreads.length; i++) {
            downloaderThreads[i].start();
        }

        Consumer<Integer, String> consumer = JobFinishedInformerTest
                .subscribe(JobFinishedInformerTest.consumer());

        while (!urls.isEmpty()) {
            ConsumerRecords<Integer, String> records = consumer.poll(Long
                    .MAX_VALUE);
            for (ConsumerRecord<Integer, String> record : records) {
                try {
                    JsonNode jsonNode = new ObjectMapper().readTree(record
                            .value());
                    logger.info(jsonNode.get("url").asText() + records.count());
                    if (urls1.contains(jsonNode.get("url").asText())) {
                        continue;
                    }
                    Assert.assertTrue(urls.contains(jsonNode.get("url")
                            .asText()));
                    urls1.add(jsonNode.get("url").asText());
                    urls.remove(jsonNode.get("url").asText());
                    Assert.assertEquals("downloadFinished", jsonNode.get
                            ("command").asText());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            }
        }

        Assert.assertTrue(urls.isEmpty());
        for (int i = 0; i < downloaderThreads.length; i++) {

            try {
                Path path = new Path(DownloadJob.hdfsWorkingPath +
                        resources[i].relativePath);
                BufferedReader buffer = new BufferedReader((new
                        InputStreamReader(DownloadJob.hdfs.open(path))));
                String str;
                String ret1 = "";
                while ((str = buffer.readLine()) != null) {
                    ret1 = ret1 + str;
                }
                buffer.close();
                Assert.assertEquals(ret, ret1);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                e.printStackTrace();
            }

            Resource resource = ResourceStorage.instance.get(resources[i].url);
            Assert.assertEquals(resources[i].url, resource.url);
            Assert.assertEquals(resources[i].time, resource.time);
            Assert.assertEquals(resources[i].mime, resource.mime);
            Assert.assertEquals(ResourceStatus.finished, resource.status);
        }


        consumer.close();
        httpServer.stop(0);
    }

    /**
     * 测试下载功能是否正常。
     */
    @Test
    public void testDownload() {
        String ret = "<html><body>" + "<h4>表头：</h4><table border=\"1\">" +
                "<tr><th>姓名</th>" + "<th>电话</th>" + "<th>电话</th>" +
                "</tr><tr><td>Bill Gates</td>" + "<td>555 77 854</td>" +
                "<td>555 77 855</td>" + "</tr></table>" + "<h4>垂直的表头：</h4>" +
                "<table border=\"1\"><tr>" + "<th>姓名</th>" + "<td>Bill " +
                "Gates</td>" + "</tr><tr>" + "<th>电话</th>" + "<td>555 77 " +
                "854</td>" + "</tr><tr><th>电话</th>" + "<td>555 77 855</td>" +
                "</tr></table></body></html>";
        HttpServer httpServer = DownloadJobTest.downloadContext
                (DownloaderNodeTest.httpServer(54345), ret);
        httpServer.start();

        String url = "http://localhost:54345/download.jsp";
        Resource resource = new Resource(url);
        resource.time = new Timestamp(System.currentTimeMillis());
        resource.relativePath = "test/resource/" + (short) url.hashCode();
        ResourceStorage.instance.put(resource);
        Downloader.instance.download(url);

        Consumer<Integer, String> consumer = JobFinishedInformerTest
                .subscribe(JobFinishedInformerTest.consumer());
        ConsumerRecords<Integer, String> records = consumer.poll(Long
                .MAX_VALUE);
        for (ConsumerRecord<Integer, String> record : records) {
            try {
                JsonNode jsonNode = new ObjectMapper().readTree(record.value());
                Assert.assertEquals(url, jsonNode.get("url").asText());
                Assert.assertEquals("downloadFinished", jsonNode.get
                        ("command").asText());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }

        Resource resource2 = ResourceStorage.instance.get(url);
        Assert.assertEquals(resource.url, resource2.url);
        Assert.assertEquals(resource.time, resource2.time);
        Assert.assertEquals(resource.relativePath, resource2.relativePath);
        Assert.assertEquals(ResourceStatus.finished, resource2.status);

        consumer.close();
        httpServer.stop(0);
    }

    /**
     * 测试Downloader中的hashmap是否正常。
     */
    @Test
    public void testHashMap() {

        String ret = "<html><body>" + "<h4>表头：</h4><table border=\"1\">" +
                "<tr><th>姓名</th>" + "<th>电话</th>" + "<th>电话</th>" +
                "</tr><tr><td>Bill Gates</td>" + "<td>555 77 854</td>" +
                "<td>555 77 855</td>" + "</tr></table>" + "<h4>垂直的表头：</h4>" +
                "<table border=\"1\"><tr>" + "<th>姓名</th>" + "<td>Bill " +
                "Gates</td>" + "</tr><tr>" + "<th>电话</th>" + "<td>555 77 " +
                "854</td>" + "</tr><tr><th>电话</th>" + "<td>555 77 855</td>" +
                "</tr></table></body></html>";

        HttpServer httpServer = DownloaderNodeTest.httpServer(54345);
        DownloadJobTest.downloadContext(httpServer, ret);

        String ret2 = "<html><body>" + "<h4>表头：</h4><table border=\"1\">" +
                "<tr><th>name</th>" + "<th>tele</th>" + "<th>电话</th>" +
                "</tr><tr><td>Bill Gates</td>" + "<td>555 77 854</td>" +
                "<td>555 77 855</td>" + "</tr></table>" + "<h4>垂直的表头：</h4>" +
                "<table border=\"1\"><tr>" + "<th>姓名</th>" + "<td>Bill " +
                "Gates</td>" + "</tr><tr>" + "<th>电话</th>" + "<td>555 77 " +
                "854</td>" + "</tr><tr><th>电话</th>" + "<td>555 77 855</td>" +
                "</tr></table></body></html>";

        DownloaderNodeTest.context(httpServer, "/download1.jsp", ret2);
        httpServer.start();

        String url1 = "http://localhost:54345/download.jsp";
        String url2 = "http://localhost:54345/download1.jsp";

        Resource resource = new Resource(url1);
        resource.time = new Timestamp(System.currentTimeMillis());
        resource.relativePath = "test/resource/" + (short) url1.hashCode();
        ResourceStorage.instance.put(resource);

        Resource resource1 = new Resource(url2);
        resource1.time = new Timestamp(System.currentTimeMillis());
        resource1.relativePath = "test/resource/" + (short) url2.hashCode();
        ResourceStorage.instance.put(resource1);

        for (int i = 0; i < 10; i++) {
            new Thread() {
                @Override
                public void run() {
                    Downloader.instance.download(url1);
                    Downloader.instance.download(url2);
                }
            }.start();
        }

        Consumer<Integer, String> consumer = JobFinishedInformerTest
                .subscribe(JobFinishedInformerTest.consumer());

        int messageNum = 0;
        while (messageNum < 2) {
            ConsumerRecords<Integer, String> records = consumer.poll(Long
                    .MAX_VALUE);
            messageNum = messageNum + records.count();
            for (ConsumerRecord<Integer, String> record : records) {
                try {
                    JsonNode jsonNode = new ObjectMapper().readTree(record
                            .value());
                    if (jsonNode.get("url").asText().equals(url1)) {
                        Assert.assertTrue(true);
                    } else if (jsonNode.get("url").asText().equals(url2)) {
                        Assert.assertTrue(true);
                    } else {
                        Assert.assertTrue(false);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            }
        }
        Assert.assertEquals(0, Downloader.instance.jobMap.size());
        consumer.close();
        httpServer.stop(0);
    }

    // **************** 公开方法
}
