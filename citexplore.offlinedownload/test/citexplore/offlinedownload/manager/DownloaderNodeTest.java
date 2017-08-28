package citexplore.offlinedownload.manager;

import citexplore.offlinedownload.Resource;
import com.sun.net.httpserver.HttpServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * 离线下载器节点测试类。
 *
 * @author Zhang, Yin
 */
public class DownloaderNodeTest {

    // **************** 公开变量

    // **************** 私有变量

    /**
     * Log4j logger。
     */
    private static Logger logger = LogManager.getLogger(DownloaderNodeTest
            .class);

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 测试recover函数基本功能。
     */
    @Test
    public void testRecover() {
        try {
            DownloaderNode node = new DownloaderNode("test", "http" +
                    "://localhost:54345/", 500, 50, 50, 50, 50, (node1,
                                                                 previousStatus) -> {
            });

            Resource resource = new Resource("id1");
            Assert.assertFalse(node.recover(new String[]{resource.url}));

            HttpServer server = DownloaderNodeTest.recoverNaContext
                    (DownloaderNodeTest.httpServer());
            server.start();
            Assert.assertFalse(node.recover(new String[]{resource.url}));

            server.removeContext("/recover.jsp");
            DownloaderNodeTest.recoverContext(server);
            Assert.assertTrue(node.recover(new String[]{resource.url}));

            server.stop(0);
            node.close();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 测试download函数基本功能。
     */
    @Test
    public void testDownload() {
        try {
            DownloaderNode node = new DownloaderNode("test", "http" +
                    "://localhost:54345/", 500, 50, 50, 50, 50, (node1,
                                                                 previousStatus) -> {
            });

            Resource resource = new Resource("id1");
            Assert.assertFalse(node.download(resource));

            HttpServer server = DownloaderNodeTest.downloadNaContext
                    (DownloaderNodeTest.httpServer());
            server.start();
            Assert.assertFalse(node.download(resource));

            server.removeContext("/download.jsp");
            DownloaderNodeTest.downloadContext(server);
            Assert.assertTrue(node.download(resource));

            server.stop(0);
            node.close();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 测试心跳功能。
     */
    @Test
    public void testHeartbeat() {
        try {
            DownloaderNode node = new DownloaderNode("test", "http" +
                    "://localhost:54345/", 500, 50, 50, 10000, 10000, new
                    NodeListener() {
                public int count = 0;

                @Override
                public void statusChanged(DownloaderNode node, NodeStatus
                        previousStatus) {
                    count = count + 1;
                }
            });
            Thread.sleep(1000);
            Assert.assertEquals(NodeStatus.dying, node.status());
            Assert.assertTrue(node.succeededHeartbeat < 0);
            Assert.assertTrue(node.succeededHeartbeat > -3);
            Thread.sleep(1000);
            Assert.assertEquals(NodeStatus.dead, node.status());
            Assert.assertTrue(node.succeededHeartbeat <= -3);

            HttpServer server = DownloaderNodeTest.liveContext
                    (DownloaderNodeTest.httpServer());
            server.start();
            Thread.sleep(1000);
            Assert.assertTrue(node.succeededHeartbeat > 0);
            Assert.assertTrue(node.succeededHeartbeat < 3);
            Assert.assertEquals(NodeStatus.recoverying, node.status());
            Thread.sleep(1000);
            Assert.assertTrue(node.succeededHeartbeat > 3);
            Assert.assertEquals(NodeStatus.alive, node.status());
            try {
                Assert.assertEquals(4, node.listener.getClass()
                        .getDeclaredField("count").get(node.listener));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }

            node.close();
            server.stop(0);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }


    /**
     * 向HttpServer中放入恢复na下文。
     *
     * @param server HttpServer。
     * @return HttpServer
     */
    protected static HttpServer recoverNaContext(HttpServer server) {
        return DownloaderNodeTest.context(server, "/recover.jsp",
                "{\n\"response\":\"na\"\n}");
    }

    /**
     * 向HttpServer中放入恢复下文。
     *
     * @param server HttpServer。
     * @return HttpServer
     */
    protected static HttpServer recoverContext(HttpServer server) {
        return DownloaderNodeTest.context(server, "/recover.jsp",
                "{\n\"response\":\"accepted\"\n}");
    }


    /**
     * 向HttpServer中放入下载na下文。
     *
     * @param server HttpServer。
     * @return HttpServer
     */
    protected static HttpServer downloadNaContext(HttpServer server) {
        return DownloaderNodeTest.context(server, "/download.jsp",
                "{\n\"response\":\"na\"\n}");
    }

    /**
     * 向HttpServer中放入下载下文。
     *
     * @param server HttpServer。
     * @return HttpServer
     */
    protected static HttpServer downloadContext(HttpServer server) {
        return DownloaderNodeTest.context(server, "/download.jsp",
                "{\n\"response\":\"accepted\"\n}");
    }

    /**
     * 向HttpServer中放入存活上下文。
     *
     * @param server HttpServer。
     * @return HttpServer
     */
    protected static HttpServer liveContext(HttpServer server) {
        return DownloaderNodeTest.context(server, "/heartbeat.jsp",
                "{\n\"status\":" + NodeStatus.alive.toInt() + "\n}");
    }

    /**
     * 向HttpServer中放入上下文。
     *
     * @param server  HttpServer。
     * @param context 上下文。
     * @param ret     返回内容。
     * @return HttpServer。
     */
    public static HttpServer context(HttpServer server, String context,
                                     String ret) {
        server.createContext(context, httpExchange -> {
            httpExchange.sendResponseHeaders(200, 0);
            OutputStream response = httpExchange.getResponseBody();
            response.write(ret.getBytes());
            response.close();
        });
        return server;
    }

    /**
     * 获得HttpServer。
     *
     * @return HttpServer。
     */
    protected static HttpServer httpServer() {
        return DownloaderNodeTest.httpServer(54345);
    }


    /**
     * 获得HttpServer。
     *
     * @param port 端口。
     * @return HttpServer。
     */
    public static HttpServer httpServer(int port) {
        try {
            return HttpServer.create(new InetSocketAddress(port), 0);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    // **************** 私有方法

}
