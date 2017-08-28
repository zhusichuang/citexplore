package citexplore.offlinedownload.downloader;

/**
 * 离线下载任务状态阶段枚举。
 *
 * @author Zhang, Yin
 */
public enum JobStage {

    // **************** 公开变量

    /**
     * 下载失败。
     */
    downloadFailed(-1),

    /**
     * 未知。
     */
    unknown(0),

    /**
     * 下载完成。
     */
    finished(1),

    /**
     * 准备开始。
     */
    readyToStart(101),

    /**
     * 测试下载地址。
     */
    testingUrl(102),

    /**
     * 正在下载。
     */
    downloading(103),

    /**
     * 正在验证数据。
     */
    verifying(104),

    /**
     * 下载工作已完成，但数据移动尚未完成。
     */
    almostDone(105);

    // **************** 私有变量

    /**
     * 下载载状态类型枚举数字标识。
     */
    private int index = 101;

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 得到下载状态类型枚举数字标识。
     *
     * @return 下载状态类型枚举数字标识。
     */
    public int toInt() {
        return index;

    }

    /**
     * 将下载状态类型枚举数字标识映射为下载状态类型
     *
     * @param stage 下载状态类型枚举数字标识。
     * @return 下载状态类型枚举
     */
    public static JobStage intToType(int stage) {
        for (JobStage stage1 : JobStage.values()) {
            if (stage1.index == stage) {
                return stage1;
            }
        }
        return null;
    }

    // **************** 私有方法

    /**
     * 私有构造函数。
     *
     * @param index 下载状态类型枚举数字标识。
     */
    JobStage(int index) {
        this.index = index;
    }

}
