package citexplore.offlinedownload.downloader;

import citexplore.foundation.Config;
import citexplore.offlinedownload.Resource;
import citexplore.offlinedownload.ResourceStatus;
import citexplore.offlinedownload.ResourceStorage;
import citexplore.offlinedownload.ResourceStorageTest;
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;

/**
 * 离线下载任务测试类。
 *
 * @author Zhu, Sichuang
 * @author Zhang, Yin
 */
public class DownloadJobTest {

    // **************** 公开变量

    // **************** 私有变量

    /**
     * Log4j logger。
     */
    private static Logger logger = LogManager.getLogger(DownloadJobTest.class);

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 清理hdfs、kafka和riak。
     */
    @BeforeClass
    public static void beforeClass() {
        cleanAll();
    }

    /**
     * 清理hdfs、kafka和riak。
     */
    @After
    public void after() {
        cleanAll();
    }

    /**
     * 清理hdfs、kafka和riak。
     */
    public static void cleanAll() {
        DownloadJobTest.cleanHdfs();
        JobFinishedInformerTest.cleanKafka();
        ResourceStorageTest.cleanRiak();
    }

    public static void cleanHdfs() {
        logger.info("Cleaning hdfs...");
        try {
            Path path = new Path(DownloadJob.hdfsWorkingPath + "test");
            DownloadJob.hdfs.delete(path, true);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 测试下载一个错误的链接。
     */
    @Test
    public void testDownloadWithWrongUrl() {
        String url = "http://localhost:12345/download.jsp";
        Resource resource = new Resource(url);
        resource.time = new Timestamp(System.currentTimeMillis());

        resource.relativePath = "test/resource/" + (short) url.hashCode();
        ResourceStorage.instance.put(resource);
        DownloadJob downloadJob = new DownloadJob(url, Config.getFolder
                (Downloader.WORKING_PATH), Downloader.instance);
        downloadJob.start();

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
            }
        }
        consumer.close();
        Resource resource2 = ResourceStorage.instance.get(url);

        Assert.assertEquals(url, resource2.url);
        Assert.assertEquals(resource.time.getTime(), resource2.time.getTime());
        Assert.assertEquals(ResourceStatus.downloadFailed, resource2.status);
        Assert.assertEquals(resource.relativePath, resource2.relativePath);
    }

    /**
     * 测试下载一个正确的链接。
     */
    @Test
    public void testDownloadWithRightUrl() {
        String ret = "<html><body><h1>This is heading 1</h1>" + "<h2>This is " +
                "heading 2</h2><h3>This is heading 3</h3>" + "<h4>This is " +
                "heading 4</h4><h5>This is heading 5</h5>" + "<h6>This is " +
                "heading 6</h6></body></html>";
        HttpServer server = downloadContext(DownloaderNodeTest.httpServer
                (54345), ret);
        server.start();

        String url = "http://localhost:54345/download.jsp";
        Resource resource = new Resource(url);
        resource.time = new Timestamp(System.currentTimeMillis());
        resource.relativePath = "test/resource/" + (short) url.hashCode();
        ResourceStorage.instance.put(resource);
        DownloadJob downloadJob = new DownloadJob(url, Config.getFolder
                (Downloader.WORKING_PATH), Downloader.instance);
        downloadJob.download();

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
            }
        }
        consumer.close();

        try {
            Path path = new Path(DownloadJob.hdfsWorkingPath + resource
                    .relativePath);
            BufferedReader buffer = new BufferedReader((new InputStreamReader
                    (DownloadJob.hdfs.open(path))));
            String str;
            String ret1 = "";
            while ((str = buffer.readLine()) != null) {
                ret1 = ret1 + str;
            }
            buffer.close();
            Assert.assertEquals(ret, ret1);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        Resource resource2 = ResourceStorage.instance.get(url);

        Assert.assertEquals(url, resource2.url);
        Assert.assertEquals(resource.time.getTime(), resource2.time.getTime());
        Assert.assertEquals(ResourceStatus.finished, resource2.status);
        Assert.assertEquals(resource.relativePath, resource2.relativePath);

        server.stop(0);
    }

    /**
     * 测试多线程下载同一个链接。
     */
    @Test
    public void testDownloadMultiThread() {
        String ret = "<html><body><h1>This is heading 1</h1>" + "<h2>This is " +
                "heading 2</h2><h3>This is heading 3</h3>" + "<h4>This is " +
                "heading 4</h4><h5>This is heading 5</h5>" + "<h6>This is " +
                "heading 6</h6></body></html>";
        HttpServer server = downloadContext(DownloaderNodeTest.httpServer
                (54343), ret);
        server.start();

        String url = "http://localhost:54343/download.jsp";
        Resource resource = new Resource(url);
        resource.time = new Timestamp(System.currentTimeMillis());
        resource.relativePath = "test/resource/" + (short) url.hashCode();
        ResourceStorage.instance.put(resource);
        Thread[] downloadJobThreads = new Thread[500];
        DownloadJob[] downloaderJobs = new DownloadJob[downloadJobThreads
                .length];

        for (int i = 0; i < downloadJobThreads.length; i++) {
            int j = i;
            downloadJobThreads[i] = new Thread() {
                @Override
                public void run() {
                    downloaderJobs[j] = new DownloadJob(url, Config
                            .getFolder(Downloader.WORKING_PATH), Downloader
                            .instance);
                    downloaderJobs[j].download();
                }
            };
        }

        for (int i = 0; i < downloadJobThreads.length; i++) {
            downloadJobThreads[i].start();
        }

        Consumer<Integer, String> consumer = JobFinishedInformerTest
                .subscribe(JobFinishedInformerTest.consumer());
        for (int i = 0; i < downloadJobThreads.length; ) {
            ConsumerRecords<Integer, String> records = consumer.poll(Long
                    .MAX_VALUE);
            i = i + records.count();
            logger.info(i + " messages received");
            for (ConsumerRecord<Integer, String> record : records) {
                try {
                    JsonNode jsonNode = new ObjectMapper().readTree(record
                            .value());
                    Assert.assertEquals("downloadFinished", jsonNode.get
                            ("command").asText());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        consumer.close();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e1) {
            logger.error(e1.getMessage(), e1);
            throw new RuntimeException(e1);
        }

        for (int i = 0; i < downloaderJobs.length; i++) {
            Assert.assertEquals("Mismatch #" + Integer.toString(i), JobStage
                    .finished, downloaderJobs[i].status.stage);
        }
        try {
            Path path = new Path(DownloadJob.hdfsWorkingPath + resource
                    .relativePath);
            BufferedReader buffer = new BufferedReader((new InputStreamReader
                    (DownloadJob.hdfs.open(path))));
            String str;
            String ret1 = "";
            while ((str = buffer.readLine()) != null) {
                ret1 = ret1 + str;
            }
            buffer.close();
            Assert.assertEquals(ret, ret1);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        Resource resource2 = ResourceStorage.instance.get(url);

        Assert.assertEquals(url, resource2.url);
        Assert.assertEquals(resource.time.getTime(), resource2.time.getTime());
        Assert.assertEquals(ResourceStatus.finished, resource2.status);
        Assert.assertEquals(resource.relativePath, resource2.relativePath);

        server.stop(0);
    }

    // **************** 私有方法

    /**
     * 向HttpServer中放入下载文件。
     *
     * @param server HttpServer。
     * @param ret    返回内容。
     * @return HttpServer。
     */
    protected static HttpServer downloadContext(HttpServer server, String ret) {
        return DownloaderNodeTest.context(server, "/download.jsp", ret);
    }

}
