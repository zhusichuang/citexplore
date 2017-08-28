package citexplore.offlinedownload.downloader;

import org.junit.Assert;
import org.junit.Test;

/**
 * 下载状态类型枚举测试类。
 *
 * @author Zhu, Sichuang
 */
public class JobStageTest {

    // **************** 公开变量

    // **************** 私有变量

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 测试toInt函数的基本功能。
     */
    @Test
    public void testToInt() {
        Assert.assertEquals(-1, JobStage.downloadFailed.toInt());
        Assert.assertEquals(0, JobStage.unknown.toInt());
        Assert.assertEquals(1, JobStage.finished.toInt());
        Assert.assertEquals(101, JobStage.readyToStart.toInt());
        Assert.assertEquals(102, JobStage.testingUrl.toInt());
        Assert.assertEquals(103, JobStage.downloading.toInt());
        Assert.assertEquals(104, JobStage.verifying.toInt());
        Assert.assertEquals(105, JobStage.almostDone.toInt());

    }

    /**
     * 测试intToType函数的基本功能。
     */
    @Test
    public void testIntToTpye() {
        Assert.assertEquals(JobStage.downloadFailed, JobStage.intToType(-1));
        Assert.assertEquals(JobStage.unknown, JobStage.intToType(0));
        Assert.assertEquals(JobStage.finished, JobStage.intToType(1));
        Assert.assertEquals(JobStage.readyToStart, JobStage.intToType(101));
        Assert.assertEquals(JobStage.testingUrl, JobStage.intToType(102));
        Assert.assertEquals(JobStage.downloading, JobStage.intToType(103));
        Assert.assertEquals(JobStage.verifying, JobStage.intToType(104));
        Assert.assertEquals(JobStage.almostDone, JobStage.intToType(105));
    }

    // **************** 私有方法

}
