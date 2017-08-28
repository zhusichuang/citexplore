package citexplore.offlinedownload.manager;

import citexplore.offlinedownload.Resource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.istack.internal.NotNull;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 离线下载器节点。
 *
 * @author Zhang, Yin
 */
public class DownloaderNode {

    // **************** 公开变量

    /**
     * 离线下载器节点id。
     */
    public final String id;

    /**
     * 离线下载器端点。
     */
    public final String endpoint;

    /**
     * 离线下载器节点心跳检测周期。
     */
    public final long heartbeatInterval;

    /**
     * 离线下载器节点心跳检测连接超时时间。
     */
    public final int heartbeatConnectTimeout;

    /**
     * 离线下载器节点心跳检测读取超时时间。
     */
    public final int heartbeatReadTimeout;

    /**
     * 离线下载器节点命令连接超时时间。
     */
    public final int commandConnectTimeout;

    /**
     * 离线下载器节点命令读取超时时间。
     */
    public final int commandReadTimeout;

    /**
     * 离线下载器节点成功心跳数。
     */
    public long succeededHeartbeat = 0;

    /**
     * 离线下载器节点监听者。
     */
    protected final NodeListener listener;

    // **************** 私有变量

    /**
     * 离线下载器节点状态。
     */
    private NodeStatus _status = NodeStatus.unknown;

    /**
     * 离线下载器节点心跳检测定时器。
     */
    private Timer heartbeatTimer = null;

    /**
     * Log4j logger。
     */
    private static Logger logger = LogManager.getLogger(DownloaderNode.class);

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 离线下载器节点构造函数。
     *
     * @param id                      离线下载器节点id。
     * @param endpoint                离线下载器端点。
     * @param heartbeatInterval       离线下载器节点心跳检测周期。
     * @param heartbeatConnectTimeout 离线下载器节点心跳检测连接超时时间。
     * @param heartbeatReadTimeout    离线下载器节点心跳检测读取超时时间。
     * @param commandConnectTimeout   离线下载器节点命令连接超时时间。
     * @param commandReadTimeout      离线下载器节点命令读取超时时间。
     * @param listener                离线下载器节点监听者。
     */
    public DownloaderNode(@NotNull String id, @NotNull String endpoint, long
            heartbeatInterval, int heartbeatConnectTimeout, int
            heartbeatReadTimeout, int commandConnectTimeout, int
            commandReadTimeout, @NotNull NodeListener listener) {
        this.id = id;
        this.endpoint = endpoint;
        this.heartbeatInterval = heartbeatInterval;
        this.heartbeatConnectTimeout = heartbeatConnectTimeout;
        this.heartbeatReadTimeout = heartbeatReadTimeout;
        this.commandConnectTimeout = commandConnectTimeout;
        this.commandReadTimeout = commandReadTimeout;
        this.listener = listener;

        heartbeatTimer = new Timer();
        heartbeatTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                heartbeat();
            }
        }, 0, heartbeatInterval);
    }

    /**
     * 获得离线下载器节点状态。
     *
     * @return 离线下载器节点状态。
     */
    public NodeStatus status() {
        return _status;
    }

    /**
     * 向离线下载器发送下载命令。
     *
     * @param resource 需要下载的资源。
     * @return 下载命令是否成功发送。
     */
    public boolean download(@NotNull Resource resource) {
        ObjectNode commandNode = JsonNodeFactory.instance.objectNode();
        commandNode.put("command", "download");
        commandNode.put("url", resource.url);
        boolean ret = Manager.sendJsonCommand(new HttpPost(endpoint +
                "download.jsp"), commandNode, commandConnectTimeout,
                commandReadTimeout);
        if (!ret) {
            logger.error("Error sending download command to downloader node " +
                    id + ": " + commandNode.toString());
        }
        return ret;
    }

    /**
     * 向离线下载器发送恢复下载命令。
     *
     * @param urls 需要恢复下载的资源urls。
     * @return 恢复下载命令是否发送成功。
     */
    public boolean recover(@NotNull String[] urls) {
        ObjectNode commandNode = JsonNodeFactory.instance.objectNode();
        commandNode.put("command", "recover");
        commandNode.set("urls", Arrays.stream(urls).collect
                (commandNode::arrayNode, ArrayNode::add, ArrayNode::addAll));
        boolean ret = Manager.sendJsonCommand(new HttpPost(endpoint +
                "recover.jsp"), commandNode, commandConnectTimeout,
                commandReadTimeout);
        if (!ret) {
            logger.error("Error sending recover command to downloader node "
                    + id + ": " + commandNode.toString());
        }
        return ret;
    }

    /**
     * 关闭离线下载器节点。
     */
    public void close() {
        heartbeatTimer.cancel();
        heartbeatTimer.purge();
        logger.info("Downloader node " + id + " closed.");
    }

    // **************** 私有方法

    /**
     * 离线下载器节点心跳检测函数。
     */
    private void heartbeat() {
        HttpGet get = new HttpGet(endpoint + "heartbeat.jsp");
        get.setConfig(RequestConfig.custom().setConnectTimeout
                (heartbeatConnectTimeout).setSocketTimeout
                (heartbeatReadTimeout).build());
        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response = null;

        boolean statusChanged = false;
        NodeStatus previousStatus = _status;
        try {
            response = client.execute(get);
            HttpEntity responseEntity = response.getEntity();
            JsonNode responseNode = new ObjectMapper().readTree(EntityUtils
                    .toString(responseEntity));
            EntityUtils.consume(responseEntity);

            if (NodeStatus.intToType(responseNode.get("status").asInt()) ==
                    NodeStatus.alive) {
                statusChanged = heartbeatSucceeded();
            } else {
                statusChanged = heartbeatFailed("Wrong status code: " +
                        responseNode.get("status").asInt());
            }
        } catch (Exception e) {
            statusChanged = heartbeatFailed("Exception occurred: " + e
                    .getMessage());
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (Exception ignored) {
                }
            }

            try {
                client.close();
            } catch (Exception ignored) {
            }
        }

        if (statusChanged) {
            listener.statusChanged(this, previousStatus);
        }
    }

    /**
     * 离线下载器节点心跳成功。
     *
     * @return 离线下载器节点状态是否发生改变。
     */
    private boolean heartbeatSucceeded() {
        if (succeededHeartbeat < 0) {
            succeededHeartbeat = 0;
        }

        succeededHeartbeat = succeededHeartbeat + 1;

        if (_status == NodeStatus.alive) {
            return false;
        } else if (succeededHeartbeat >= 3) {
            _status = NodeStatus.alive;
            return true;
        } else if (_status != NodeStatus.recoverying) {
            _status = NodeStatus.recoverying;
            return true;
        } else {
            return false;
        }
    }

    /**
     * 离线下载器节点心跳失败。
     *
     * @param message 离线下载器节点心跳失败信息。
     * @return 离线下载器节点状态是否发生改变。
     */
    private boolean heartbeatFailed(String message) {
        logger.error("Heartbeat failed for downloader node " + id + ": " +
                message);

        if (succeededHeartbeat > 0) {
            succeededHeartbeat = 0;
        }

        succeededHeartbeat = succeededHeartbeat - 1;

        if (_status == NodeStatus.dead) {
            return false;
        } else if (succeededHeartbeat <= -3) {
            _status = NodeStatus.dead;
            return true;
        } else if (_status != NodeStatus.dying) {
            _status = NodeStatus.dying;
            return true;
        } else {
            return false;
        }
    }

}
