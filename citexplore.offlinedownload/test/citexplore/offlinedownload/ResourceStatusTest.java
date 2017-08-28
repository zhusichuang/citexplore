package citexplore.offlinedownload;

import org.junit.Assert;
import org.junit.Test;

/**
 * 资源状态类型枚举测试类。
 *
 * @author Zhu, Sichuang
 */
public class ResourceStatusTest {

    // **************** 公开变量

    // **************** 私有变量

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 测试toInt函数的基本功能。
     */
    @Test
    public void testToInt() {
        Assert.assertEquals(-1, ResourceStatus.downloadFailed.toInt());
        Assert.assertEquals(-0, ResourceStatus.unknown.toInt());
        Assert.assertEquals(1, ResourceStatus.finished.toInt());
    }

    /**
     * 测试intToType函数的基本功能。
     */
    @Test
    public void testIntToTpye() {
        Assert.assertEquals(ResourceStatus.downloadFailed, ResourceStatus
                .intToType(-1));
        Assert.assertEquals(ResourceStatus.unknown, ResourceStatus.intToType
                (0));
        Assert.assertEquals(ResourceStatus.finished, ResourceStatus.intToType
                (1));
    }

    // **************** 私有方法
}
