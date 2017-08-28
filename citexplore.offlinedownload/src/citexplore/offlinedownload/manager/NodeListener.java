package citexplore.offlinedownload.manager;

import com.sun.istack.internal.NotNull;

/**
 * 离线下载器节点监听者。
 *
 * @author Zhang, Yin
 */
public interface NodeListener {

    // **************** 公开变量

    // **************** 私有变量

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 离线下载器节点状态改变。
     *
     * @param node           状态发生改变的离线下载器节点。
     * @param previousStatus 离线下载器节点此前的状态。
     */
    void statusChanged(@NotNull DownloaderNode node, @NotNull NodeStatus
            previousStatus);

    // **************** 私有方法

}
