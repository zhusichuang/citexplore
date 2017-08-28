package citexplore.offlinedownload.manager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

/**
 * 离线下载器节点状态枚举测试类。
 *
 * @author Zhang, Yin
 */
public class NodeStatusTest {

    // **************** 公开变量

    // **************** 私有变量

    /**
     * Log4j logger。
     */
    private static Logger logger = LogManager.getLogger(NodeStatusTest.class);

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 测试toInt函数基本功能。
     */
    @Test
    public void testToInt() {
        Assert.assertEquals(2, NodeStatus.alive.toInt());
        Assert.assertEquals(-1, NodeStatus.dying.toInt());
        Assert.assertEquals(-2, NodeStatus.dead.toInt());

    }

    @Test
    public void testIntToType() {
        Assert.assertEquals(NodeStatus.alive, NodeStatus.intToType(2));
        Assert.assertEquals(NodeStatus.dying, NodeStatus.intToType(-1));
        Assert.assertEquals(NodeStatus.dead, NodeStatus.intToType(-2));

    }

    // **************** 私有方法

}
