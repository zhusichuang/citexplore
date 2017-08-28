package citexplore.offlinedownload;

/**
 * 资源状态类型枚举。
 *
 * @author Zhang, Yin; Zhu, Sichuang
 */
public enum ResourceStatus {

    // **************** 公开变量

    downloadFailed(-1), unknown(0), finished(1);

    // **************** 私有变量

    private int index = 0;

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 得到资源状态类型枚举数字标识。
     *
     * @return 资源状态类型枚举数字标识。
     */
    public int toInt() {
        return index;

    }

    /**
     * 将资源状态类型枚举数字标识映射为资源状态类型
     *
     * @param index 资源状态类型枚举数字标识。
     * @return 资源状态类型枚举
     */
    public static ResourceStatus intToType(int index) {
        for (ResourceStatus statu : ResourceStatus.values()) {
            if (statu.index == index) {
                return statu;
            }
        }
        return null;
    }

    // **************** 私有方法

    /**
     * 私有构造函数。
     */
    private ResourceStatus(int index) {
        this.index = index;
    }

}
