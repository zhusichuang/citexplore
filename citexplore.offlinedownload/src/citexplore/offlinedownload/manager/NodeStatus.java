package citexplore.offlinedownload.manager;

/**
 * 离线下载器节点状态枚举。
 *
 * @author Zhang, Yin
 */
public enum NodeStatus {

    // **************** 公开变量

    alive(2), recoverying(1), unknown(0), dying(-1), dead(-2);

    // **************** 私有变量

    private int index = -1;

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 得到离线下载器节点状态枚举数字标识。
     *
     * @return 离线下载器节点状态枚举数字标识。
     */
    public int toInt() {
        return index;
    }

    /**
     * 将离线下载器节点状态枚举数字标识映射为离线下载器节点状态枚举。
     *
     * @param index 离线下载器节点状态枚举数字标识。
     * @return 离线下载器节点状态枚举。
     */
    public static NodeStatus intToType(int index) {
        for (NodeStatus type : NodeStatus.values()) {
            if (type.index == index) {
                return type;
            }
        }
        return null;
    }

    // **************** 私有方法

    /**
     * 离线下载器节点状态枚举构造函数。
     */
    NodeStatus(int index) {
        this.index = index;
    }

}
