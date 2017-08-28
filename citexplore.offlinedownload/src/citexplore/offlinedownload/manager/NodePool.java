package citexplore.offlinedownload.manager;

import citexplore.foundation.Config;
import citexplore.foundation.ConfigItemException;
import com.sun.istack.internal.NotNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * 离线下载器节点池。
 *
 * @author Zhang, Yin
 */
public class NodePool {

    // **************** 公开变量

    /**
     * 离线下载器节点池锁。
     */
    public final Object lock = new Object();

    /**
     * 离线下载器节点心跳检测周期。
     */
    public final long nodeHeartbeatInterval;

    /**
     * 离线下载器节点池端点id配置项键。
     */
    public static final String NODE_IDS = "cx.ofd.nodepool.nodeids";

    /**
     * 离线下载器节点池端点配置项键。
     */
    public static final String NODE_ENDPOINTS = "cx.ofd.nodepool.nodeendpoints";

    /**
     * 离线下载器节点池节点心跳检测周期配置项键。
     */
    public static final String NODE_HEARTBEAT_INTERVAL = "cx.ofd.nodepool" +
            ".nodeheartbeatinterval";

    /**
     * 离线下载器节点池节点心跳检测连接超时时间配置项键。
     */
    public static final String NODE_HEARTBEAT_CONNECT_TIMEOUT = "cx.ofd" + "" +
            ".nodepool.nodeheartbeatconnecttimeout";

    /**
     * 离线下载器节点池节点心跳检测读取超时时间配置项键。
     */
    public static final String NODE_HEARTBEAT_READ_TIMEOUT = "cx.ofd" + "" +
            ".nodepool.nodeheartbeatreadtimeout";

    /**
     * 离线下载器节点池节点命令连接超时时间配置项键。
     */
    public static final String NODE_COMMAND_CONNECT_TIMEOUT = "cx.ofd" + "" +
            ".nodepool.nodecommandconnecttimeout";

    /**
     * 离线下载器节点池节点命令读取超时时间配置项键。
     */
    public static final String NODE_COMMAND_READ_TIMEOUT = "cx.ofd.nodepool"
            + ".nodecommandreadtimeout";

    /**
     * 健康离线下载器节点列表。
     */
    protected List<DownloaderNode> healthyNodeList = null;

    /**
     * 不健康离线下载器节点列表。
     */
    protected List<DownloaderNode> unhealthyNodeList = null;

    /**
     * 离线下载器节点是否曾经恢复。
     */
    protected HashMap<DownloaderNode, Boolean> everRecovered = null;

    // **************** 私有变量

    /**
     * 离线下载器节点池监听者。
     */
    private PoolListener listener = null;

    /**
     * Log4j logger。
     */
    private static Logger logger = LogManager.getLogger(NodePool.class);

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 私有的构造函数。
     *
     * @param listener 离线下载器节点池监听者。
     */
    protected NodePool(@NotNull PoolListener listener) {
        this.listener = listener;

        String[] ids = Config.get(NODE_IDS, "dls1").split(";");
        String[] endpoints = Config.get(NODE_ENDPOINTS,
                "http://localhost:54345/").split(";");

        if (Arrays.stream(endpoints).filter(endpoint -> !endpoint.endsWith
                ("/")).findFirst().isPresent()) {
            ConfigItemException cie = new ConfigItemException(NODE_ENDPOINTS,
                    "endpoints should ends with \"/\" and separated with " +
                            "\":\".");
            logger.error(cie);
            throw cie;
        }

        if (ids.length != endpoints.length) {
            ConfigItemException cie = new ConfigItemException(NODE_ENDPOINTS,
                    "Config item " + NODE_IDS + " contains " + ids.length +
                    " ids, but " + NODE_ENDPOINTS + " contains " + endpoints
                            .length + " endpoints.");
            logger.error(cie);
            throw cie;
        }

        healthyNodeList = new ArrayList<>(ids.length);
        unhealthyNodeList = new ArrayList<>(ids.length);
        everRecovered = new HashMap<>(ids.length);

        nodeHeartbeatInterval = Config.getLong(NODE_HEARTBEAT_INTERVAL, 10000L);
        int heartbeatConnectTimeout = Config.getInt
                (NODE_HEARTBEAT_CONNECT_TIMEOUT, 2000);
        int heartbeatReadTimeout = Config.getInt(NODE_HEARTBEAT_READ_TIMEOUT,
                2000);
        int commandConnectTimeout = Config.getInt
                (NODE_COMMAND_CONNECT_TIMEOUT, 10000);
        int commandReadTimeout = Config.getInt(NODE_COMMAND_READ_TIMEOUT,
                10000);

        synchronized (lock) {
            for (int i = 0; i < ids.length; i++) {
                DownloaderNode node = new DownloaderNode(ids[i],
                        endpoints[i], nodeHeartbeatInterval,
                        heartbeatConnectTimeout, heartbeatReadTimeout,
                        commandConnectTimeout, commandReadTimeout,
                        this::nodeStatusChanged);
                unhealthyNodeList.add(node);
                everRecovered.put(node, false);
            }
        }

        logger.info(ids.length + " downloader nodes initialized.");
    }

    /**
     * 获得离线下载器节点。*线程安全*
     *
     * @return 离线下载器节点。
     */
    public DownloaderNode node() {
        synchronized (lock) {
            return healthyNodeList.size() > 0 ? healthyNodeList.get(new
                    Random().nextInt(healthyNodeList.size())) : null;
        }
    }

    /**
     * 关闭离线下载器节点池。
     */
    public void close() {
        synchronized (lock) {
            healthyNodeList.forEach(DownloaderNode::close);
            healthyNodeList.clear();
            unhealthyNodeList.forEach(DownloaderNode::close);
            unhealthyNodeList.clear();
        }
        logger.info("Node pool closed.");
    }

    // **************** 私有方法

    /**
     * 离线下载器节点状态事件处理函数。*线程安全*
     *
     * @param node           状态发生改变的离线下载器节点。
     * @param previousStatus 离线下载器节点此前的状态。
     */
    private void nodeStatusChanged(DownloaderNode node, NodeStatus
            previousStatus) {
        logger.info("Downloader node " + node.id + " changed from " +
                previousStatus.toString() + " to " + node.status().toString());
        synchronized (lock) {
            if (node.status() == NodeStatus.alive) {
                unhealthyNodeList.remove(node);
                healthyNodeList.add(node);
                everRecovered.put(node, true);
                listener.nodeRecovered(node);
            } else if (previousStatus == NodeStatus.alive) {
                healthyNodeList.remove(node);
                unhealthyNodeList.add(node);
            } else if (node.status() == NodeStatus.dead && everRecovered.get
                    (node)) {
                everRecovered.put(node, false);
                listener.nodeDied(node);
            }
        }
    }

}
