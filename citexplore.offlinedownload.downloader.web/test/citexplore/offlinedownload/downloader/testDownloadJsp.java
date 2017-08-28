package citexplore.offlinedownload.downloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import citexplore.offlinedownload.Resource;
import citexplore.offlinedownload.ResourceStatus;
import citexplore.offlinedownload.ResourceStorage;
import citexplore.offlinedownload.downloader.DownloadJobTest;
import citexplore.offlinedownload.downloader.JobFinishedInformerTest;
import citexplore.offlinedownload.manager.DownloaderNodeTest;
import net.javacrumbs.jsonunit.JsonAssert;

/**
 * download.jsp测试类。
 * 
 * @author Zhu, Sichuang
 * @author Zhang, Yin
 */
public class testDownloadJsp {

    // **************** 公开变量

    // **************** 私有变量

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 清理hdfs，riak和kafka。
     */
    @BeforeClass
    public static void cleanAllBefore() {
        DownloadJobTest.cleanAll();
    }

    /**
     * 清理hdfs，riak和kafka。
     */
    @After
    public void cleanAllAfter() {
        DownloadJobTest.cleanAll();
    }

    /**
     * 发送正确的参数测试下载功能。
     */
    @Test
    public void testDownloadWithRightParam() {
        String ret = "<html><body>" + "<h4>表头：</h4><table border=\"1\">"
                + "<tr><th>姓名</th>" + "<th>电话</th>" + "<th>电话</th>"
                + "</tr><tr><td>Bill Gates</td>" + "<td>555 77 854</td>"
                + "<td>555 77 855</td>" + "</tr></table>" + "<h4>垂直的表头：</h4>"
                + "<table border=\"1\"><tr>" + "<th>姓名</th>"
                + "<td>Bill Gates</td>" + "</tr><tr>" + "<th>电话</th>"
                + "<td>555 77 854</td>" + "</tr><tr><th>电话</th>"
                + "<td>555 77 855</td>" + "</tr></table></body></html>";
        HttpServer httpServer = DownloaderNodeTest.context(
                DownloaderNodeTest.httpServer(54345), "/download.jsp", ret);
        httpServer.start();

        String url1 = "http://localhost:54345/download.jsp";
        Resource resource = new Resource(url1);
        resource.time = new Timestamp(System.currentTimeMillis());
        resource.relativePath = "test/resource/" + (short) url1.hashCode();
        ResourceStorage.instance.put(resource);

        String url = "http://localhost:8080/citexplore.offlinedownload.downloader.web/download.jsp";
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        String param = "command={\"command\":\"download\",\"url\":\"" + url1
                + "\"}";
        try {
            URLConnection conn = new URL(url).openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            out = new PrintWriter(conn.getOutputStream());
            out.print(param);
            out.flush();
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            in.close();
            out.close();
            JsonAssert.assertJsonEquals(
                    new ObjectMapper().readTree(new File(getClass()
                            .getResource(
                                    "/json/TestDownloadWithRightParam.json")
                            .getFile())),
                    new ObjectMapper().readTree(result));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Consumer<Integer, String> consumer = JobFinishedInformerTest
                .subscribe(JobFinishedInformerTest.consumer());
        ConsumerRecords<Integer, String> records = consumer
                .poll(Long.MAX_VALUE);
        for (ConsumerRecord<Integer, String> record : records) {
            try {
                JsonNode jsonNode = new ObjectMapper().readTree(record.value());
                Assert.assertEquals(url1, jsonNode.get("url").asText());
                Assert.assertEquals("downloadFinished",
                        jsonNode.get("command").asText());

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        Resource resource2 = ResourceStorage.instance.get(url1);
        Assert.assertEquals(resource.url, resource2.url);
        Assert.assertEquals(resource.time, resource2.time);
        Assert.assertEquals(resource.relativePath, resource2.relativePath);
        Assert.assertEquals(ResourceStatus.finished, resource2.status);

        consumer.close();
        httpServer.stop(0);
    }

    /**
     * 发送错误的参数测试。
     */
    @Test
    public void testDownloadWithWrongParam() {

        String url = "http://localhost:8080/citexplore.offlinedownload.downloader.web/download.jsp";
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        String param = "command={\"command\":\"downlo2ad\",\"url\":\"url\"}";
        try {
            URL realUrl = new URL(url);
            URLConnection conn = realUrl.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            out = new PrintWriter(conn.getOutputStream());
            out.print(param);
            out.flush();
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }

            in.close();
            out.close();
            JsonAssert.assertJsonEquals(
                    new ObjectMapper().readTree(new File(getClass()
                            .getResource(
                                    "/json/TestDownloadWithWrongParam.json")
                            .getFile())),
                    new ObjectMapper().readTree(result));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // **************** 私有方法

}