package citexplore.offlinedownload.manager;

import com.sun.net.httpserver.HttpServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

/**
 * 离线下载器节点池测试类。
 *
 * @author Zhang, Yin
 */
public class NodePoolTest {

    // **************** 公开变量

    // **************** 私有变量

    /**
     * Log4j logger。
     */
    private static Logger logger = LogManager.getLogger(NodePoolTest.class);

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 测试监听者。
     */
    private static boolean testListenerDied = false;
    private static boolean testListenerRecovered = false;

    @Test
    public void testListener() {
        try {
            HttpServer server = DownloaderNodeTest.liveContext
                    (DownloaderNodeTest.httpServer());
            server.start();

            NodePool pool = new NodePool(new PoolListener() {
                @Override
                public void nodeDied(DownloaderNode node) {
                    NodePoolTest.testListenerDied = true;
                }

                @Override
                public void nodeRecovered(DownloaderNode node) {
                    NodePoolTest.testListenerRecovered = true;
                }
            });
            Thread.sleep(2000);
            Assert.assertTrue(NodePoolTest.testListenerRecovered);
            Assert.assertFalse(NodePoolTest.testListenerDied);

            server.stop(0);
            Thread.sleep(2000);
            Assert.assertTrue(NodePoolTest.testListenerDied);
            pool.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 测试状态改变机制。
     */
    @Test
    public void testStatusChanged() {
        try {
            HttpServer server = DownloaderNodeTest.liveContext
                    (DownloaderNodeTest.httpServer());
            server.start();

            NodePool pool = new NodePool(new PoolListener() {
                @Override
                public void nodeDied(DownloaderNode node) {
                }

                @Override
                public void nodeRecovered(DownloaderNode node) {
                }
            });
            Assert.assertEquals(0, pool.healthyNodeList.size());
            Assert.assertEquals(3, pool.unhealthyNodeList.size());

            Thread.sleep(2000);
            Assert.assertEquals(1, pool.healthyNodeList.size());
            Assert.assertEquals(2, pool.unhealthyNodeList.size());
            pool.close();

            server.stop(0);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    // **************** 私有方法

}
