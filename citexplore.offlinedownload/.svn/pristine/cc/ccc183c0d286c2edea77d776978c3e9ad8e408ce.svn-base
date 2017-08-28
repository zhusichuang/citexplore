package citexplore.offlinedownload.manager;

import citexplore.offlinedownload.Resource;
import citexplore.offlinedownload.ResourceStatus;
import citexplore.offlinedownload.ResourceStorage;
import citexplore.offlinedownload.ResourceStorageTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.util.Random;

/**
 * 离线下载管理器测试类。
 *
 * @author Zhang, Yin
 */
public class ManagerTest {

    // **************** 公开变量

    // **************** 私有变量

    /**
     * Log4j logger。
     */
    private static Logger logger = LogManager.getLogger(ManagerTest.class);

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 清理数据库并关闭Manager单件。
     */
    @BeforeClass
    public static void cleanKafkaCloseManager() {
        ManagerTest.cleanDatabase();
    }

    /**
     * 清理数据库。
     */
    public static void cleanDatabase() {
        NodeJobStorageTest.cleanMongoDb();
        NodeJobStorage.instance.nodeResourceMap.clear();
        NodeJobStorage.instance.resourceNodeMap.clear();
        ResourceStorageTest.cleanRiak();
        ManagerTest.cleanKafka();
    }

    /**
     * 清理kafka。
     */
    public static void cleanKafka() {
        logger.info("Cleaning kafka...");
        Consumer<Integer, String> consumer = Manager.subscribe(Manager
                .consumer());
        consumer.poll(5000);
        consumer.close();
        logger.info("Done.");
    }

    /**
     * 清理数据库。
     */
    @After
    public void cleanDatabaseAfter() {
        ManagerTest.cleanDatabase();
    }

    /**
     * 测试下载已经完成及一天之内下载失败。
     */
    private static int haproxy = 0;

    @Test
    public void testFinishedAndFailed() {
        try {
            ManagerTest.haproxy = 0;

            HttpServer server = DownloaderNodeTest.httpServer(54344);
            server.createContext("/", httpExchange -> {
                ManagerTest.haproxy++;
            });
            server.start();

            Resource resource = new Resource("http://no.such.url");
            resource.status = ResourceStatus.finished;
            ResourceStorage.instance.put(resource);

            Manager manager = new Manager();
            manager.start();

            KafkaProducer<Integer, String> producer = Manager.producer();
            ObjectNode command = JsonNodeFactory.instance.objectNode();
            command.put("command", "download");
            command.put("url", "http://no.such.url");
            producer.send(new ProducerRecord<>("cx_download_request", new
                    ObjectMapper().writeValueAsString(command)));
            logger.info("First download request sent.");

            while (ManagerTest.haproxy < 1) {
                Thread.sleep(1000);
            }

            resource.status = ResourceStatus.downloadFailed;
            resource.time = new Timestamp(System.currentTimeMillis());
            ResourceStorage.instance.put(resource);

            producer.send(new ProducerRecord<>("cx_download_request", new
                    ObjectMapper().writeValueAsString(command)));
            producer.close();
            logger.info("Second download request sent.");

            while (ManagerTest.haproxy < 2) {
                Thread.sleep(1000);
            }

            Assert.assertEquals(2, ManagerTest.haproxy);
            manager.close();
            server.stop(0);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 多线程测试。
     */
    private static int s1download = 0;
    private static int s2download = 0;
    private static int s3download = 0;
    private static int s1skipped = 10;
    private static int s2skipped = 10;
    private static int s3skipped = 10;

    @Test
    public void testMultiThread() {
        s1download = 0;
        s2download = 0;
        s3download = 0;
        s1skipped = 10;
        s2skipped = 10;
        s3skipped = 10;

        try {
            HttpServer s1 = DownloaderNodeTest.httpServer();
            s1.createContext("/heartbeat.jsp", httpExchange -> {
                if (ManagerTest.s1skipped > 0) {
                    ManagerTest.s1skipped = ManagerTest.s1skipped - 1;
                    httpExchange.sendResponseHeaders(200, 0);
                    OutputStream response = httpExchange.getResponseBody();
                    response.write(("{\n\"status\":" + NodeStatus.alive.toInt
                            () + "\n}").getBytes());
                    response.close();
                } else {
                    ManagerTest.s1skipped = 10;
                }
            });
            s1.createContext("/download.jsp", httpExchange -> {
                logger.info("Download requested on s1: " + ManagerTest
                        .extractRequestFromHttpExchange(httpExchange));
                ManagerTest.s1download = ManagerTest.s1download + 1;
                httpExchange.sendResponseHeaders(200, 0);
                OutputStream response = httpExchange.getResponseBody();
                response.write("{\n\"response\":\"accepted\"\n}".getBytes());
                response.close();
            }); DownloaderNodeTest.recoverContext(s1);
            s1.start();

            HttpServer s2 = DownloaderNodeTest.httpServer(54346);
            s2.createContext("/heartbeat.jsp", httpExchange -> {
                if (ManagerTest.s2skipped > 0) {
                    ManagerTest.s2skipped = ManagerTest.s2skipped - 1;
                    httpExchange.sendResponseHeaders(200, 0);
                    OutputStream response = httpExchange.getResponseBody();
                    response.write(("{\n\"status\":" + NodeStatus.alive.toInt
                            () + "\n}").getBytes());
                    response.close();
                } else {
                    ManagerTest.s2skipped = 10;
                }
            });
            s2.createContext("/download.jsp", httpExchange -> {
                logger.info("Download requested on ss: " + ManagerTest
                        .extractRequestFromHttpExchange(httpExchange));
                ManagerTest.s2download = ManagerTest.s2download + 1;
                httpExchange.sendResponseHeaders(200, 0);
                OutputStream response = httpExchange.getResponseBody();
                response.write("{\n\"response\":\"accepted\"\n}".getBytes());
                response.close();
            });
            DownloaderNodeTest.recoverContext(s2);
            s2.start();

            HttpServer s3 = DownloaderNodeTest.httpServer(54347);
            s3.createContext("/heartbeat.jsp", httpExchange -> {
                if (ManagerTest.s3skipped > 0) {
                    ManagerTest.s3skipped = ManagerTest.s3skipped - 1;
                    httpExchange.sendResponseHeaders(200, 0);
                    OutputStream response = httpExchange.getResponseBody();
                    response.write(("{\n\"status\":" + NodeStatus.alive.toInt
                            () + "\n}").getBytes());
                    response.close();
                } else {
                    ManagerTest.s3skipped = 10;
                }
            });
            s3.createContext("/download.jsp", httpExchange -> {
                logger.info("Download requested on s3: " + ManagerTest
                        .extractRequestFromHttpExchange(httpExchange));
                ManagerTest.s3download = ManagerTest.s3download + 1;
                httpExchange.sendResponseHeaders(200, 0);
                OutputStream response = httpExchange.getResponseBody();
                response.write("{\n\"response\":\"accepted\"\n}".getBytes());
                response.close();
            });
            DownloaderNodeTest.recoverContext(s3);
            s3.start();

            Manager manager = new Manager();
            manager.start();
            KafkaProducer<Integer, String> producer = Manager.producer();

            Random r = new Random();
            ObjectNode command = JsonNodeFactory.instance.objectNode();
            command.put("command", "download");
            for (int i = 0; i < 100; i++) {
                command.put("url", "http://no.such.url" + r.nextInt(10));
                producer.send(new ProducerRecord<>("cx_download_request", new
                        ObjectMapper().writeValueAsString(command)));
            }
            producer.close();

            Thread.sleep(2000);

            int wait = 0;
            while (ManagerTest.s1download + ManagerTest.s2download +
                    ManagerTest.s3download < 10) {
                wait = wait + 1;
                Thread.sleep(2000);
            }

            Assert.assertEquals(10, ManagerTest.s1download + ManagerTest
                    .s2download + ManagerTest.s3download);
            logger.info("First 10 asserted.");
            Thread.sleep(2000 * wait);
            Assert.assertEquals(10, ManagerTest.s1download + ManagerTest
                    .s2download + ManagerTest.s3download);
            logger.info("Second 10 asserted.");

            int s2d = ManagerTest.s2download;
            int s3d = ManagerTest.s3download;

            s1.stop(0);
            wait = 0;
            while (ManagerTest.s2download + ManagerTest.s3download < 10) {
                wait = wait + 1;
                Thread.sleep(2000);
            }

            Assert.assertEquals(10, ManagerTest.s2download + ManagerTest
                    .s3download);
            Thread.sleep(2000 * wait);
            Assert.assertEquals(10, ManagerTest.s2download + ManagerTest
                    .s3download);
            Assert.assertEquals(s1download, ManagerTest.s2download +
                    ManagerTest.s3download - s2d - s3d);

            manager.close();
            s2.stop(0);
            s3.stop(0);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 测试下载与恢复功能。
     */
    private static boolean downloadRequested = false;
    private static int skipped = 5;
    private static boolean recoverRequested = false;

    @Test
    public void testRecovered() {
        downloadRequested = false;
        skipped = 5;
        recoverRequested = false;
        try {
            Manager manager = new Manager();
            manager.start();
            KafkaProducer<Integer, String> producer = Manager.producer();

            HttpServer s1 = DownloaderNodeTest.httpServer();
            s1.createContext("/heartbeat.jsp", httpExchange -> {
                if (ManagerTest.skipped > 0) {
                    ManagerTest.skipped = ManagerTest.skipped - 1;
                    httpExchange.sendResponseHeaders(200, 0);
                    OutputStream response = httpExchange.getResponseBody();
                    response.write(("{\n\"status\":" + NodeStatus.alive.toInt
                            () + "\n}").getBytes());
                    response.close();
                } else {
                    ManagerTest.skipped = 5;
                }
            });
            s1.createContext("/download.jsp", httpExchange -> {
                ManagerTest.downloadRequested = true;
                httpExchange.sendResponseHeaders(200, 0);
                OutputStream response = httpExchange.getResponseBody();
                response.write("{\n\"response\":\"accepted\"\n}".getBytes());
                response.close();
            });
            s1.createContext("/recover.jsp", httpExchange -> {
                ManagerTest.recoverRequested = true;
                httpExchange.sendResponseHeaders(200, 0);
                OutputStream response = httpExchange.getResponseBody();
                response.write("{\n\"response\":\"accepted0\"\n}".getBytes());
                response.close();
            });
            s1.start();
            Thread.sleep(2000);

            ObjectNode command = JsonNodeFactory.instance.objectNode();
            command.put("command", "download");
            command.put("url", "http://no.such.url");
            producer.send(new ProducerRecord<>("cx_download_request", new
                    ObjectMapper().writeValueAsString(command)));
            producer.close();

            Thread.sleep(2000);
            while (!ManagerTest.downloadRequested) {
                Thread.sleep(2000);
                logger.info("Waiting for downloadRequested...");
            }
            Assert.assertTrue(ManagerTest.downloadRequested);

            Thread.sleep(2000);
            while (!ManagerTest.recoverRequested) {
                Thread.sleep(2000);
                logger.info("Waiting for recoverRequested...");
            }
            Assert.assertTrue(ManagerTest.recoverRequested);

            manager.close();
            s1.stop(0);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 测试下载与死亡功能。
     */
    private static int downloadRequestS1 = 0;
    private static int downloadRequestS2 = 0;

    @Test
    public void testDownloadAndDied() {
        downloadRequestS1 = 0;
        downloadRequestS2 = 0;
        try {
            Manager manager = new Manager();
            manager.start();
            KafkaProducer<Integer, String> producer = Manager.producer();

            ObjectNode command = JsonNodeFactory.instance.objectNode();
            command.put("command", "download");
            command.put("url", "http://no.such.url");
            producer.send(new ProducerRecord<>("cx_download_request", new
                    ObjectMapper().writeValueAsString(command)));

            Thread.sleep(2000);

            HttpServer s1 = DownloaderNodeTest.liveContext(DownloaderNodeTest
                    .httpServer());
            s1.createContext("/download.jsp", httpExchange -> {
                logger.info("Download requested on s1: " + ManagerTest
                        .extractRequestFromHttpExchange(httpExchange));
                ManagerTest.downloadRequestS1 = ManagerTest.downloadRequestS1
                        + 1;
                httpExchange.sendResponseHeaders(200, 0);
                OutputStream response = httpExchange.getResponseBody();
                response.write("{\n\"response\":\"accepted\"\n}".getBytes());
                response.close();
            });
            s1.start();

            HttpServer s2 = DownloaderNodeTest.liveContext(DownloaderNodeTest
                    .httpServer(54346));
            s2.createContext("/download.jsp", httpExchange -> {
                logger.info("Download requested on s2: " + ManagerTest
                        .extractRequestFromHttpExchange(httpExchange));
                ManagerTest.downloadRequestS2 = ManagerTest.downloadRequestS2
                        + 1;
                httpExchange.sendResponseHeaders(200, 0);
                OutputStream response = httpExchange.getResponseBody();
                response.write("{\n\"response\":\"accepted\"\n}".getBytes());
                response.close();
            });

            Thread.sleep(2000);
            while (ManagerTest.downloadRequestS1 < 1) {
                Thread.sleep(2000);
                logger.info("Waiting for downloadRequestS1...");
            }
            Assert.assertEquals(1, ManagerTest.downloadRequestS1);
            s2.start();

            s1.stop(0);
            Thread.sleep(2000);
            while (ManagerTest.downloadRequestS2 < 1) {
                Thread.sleep(2000);
                logger.info("Waiting for downloadRequestS2...");
            }
            Assert.assertEquals(1, ManagerTest.downloadRequestS2);

            manager.close();
            s2.stop(0);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    // **************** 私有方法

    /**
     * 从HttpExchange中提取请求。
     *
     * @param httpExchange HttpExchange。
     * @return 从HttpExchange中提取的请求。
     */
    private static String extractRequestFromHttpExchange(HttpExchange
                                                                 httpExchange) {
        BufferedReader reader = new BufferedReader(new InputStreamReader
                (httpExchange.getRequestBody()));
        String ret;
        try {
            ret = reader.readLine();
            reader.close();
        } catch (Exception e) {
            ret = e.getMessage();
        }
        return URLDecoder.decode(ret);
    }

}
