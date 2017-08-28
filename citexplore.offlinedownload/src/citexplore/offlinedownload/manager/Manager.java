package citexplore.offlinedownload.manager;

import citexplore.foundation.Config;
import citexplore.offlinedownload.FormalizedMime;
import citexplore.offlinedownload.Resource;
import citexplore.offlinedownload.ResourceStatus;
import citexplore.offlinedownload.ResourceStorage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.istack.internal.NotNull;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.StreamSupport;

/**
 * 离线下载管理器。
 *
 * @author Zhang, Yin
 */
public class Manager {

    // **************** 公开变量

    /**
     * 离线下载管理器kafka poll超时时间
     */
    public long kafkaPollTimeout = 10000L;

    /**
     * HAProxy端点。
     */
    public final String haproxyEndpoint;

    /**
     * HAProxy命令连接超时时间。
     */
    public final int haproxyCommandConnectTimeout;

    /**
     * HAproxy命令读取超时时间。
     */
    public final int haproxyCommandReadTimeout;

    /**
     * 全局唯一的离线下载管理器。
     */
    public static final Manager instance = new Manager();

    /**
     * 离线下载管理器Zookeeper服务器配置项键。
     */
    public static final String ZOOKEEPER_SERVERS = "cx.ofd.manager" + "" + "" +
            ".zookeeperservers";

    /**
     * 离线下载管理器Kafka bootstrap服务器配置项键。
     */
    public static final String KAFKA_BOOTSTRAP_SERVERS = "cx.ofd.manager" +
            "" + ".kafkabootstrapservers";

    /**
     * 离线下载管理器Zookeeper会话超时时间配置项键。
     */
    public static final String ZOOKEEPER_SESSION_TIMEOUT = "cx.ofd.manager" +
            ".zookeepersessiontimeout";

    /**
     * 离线下载管理器zookeeper.sync.time.ms配置项键。
     */
    public static final String ZOOKEEPER_SYNC_TIME = "cx.ofd.manager" + "" +
            ".zookeepersynctime";

    /**
     * 离线下载管理器auto.commit.interval.ms配置项键。
     */
    public static final String KAFKA_AUTO_COMMIT_INTERVAL = "cx.ofd.manager"
            + ".kafkaautocommitinterval";

    /**
     * 离线下载管理器kafka poll超时时间配置项键。
     */
    public static final String KAFKA_POLL_TIMEOUT = "cx.ofd.manager" + "" + "" +
            ".kafkapolltimeout";

    /**
     * HAProxy端点配置项键。
     */
    public static final String HAPROXY_ENDPOINT = "cx.ofd.manager" + "" + "" +
            ".haproxyendpoint";

    /**
     * HAProxy命令连接超时时间配置项键。
     */
    public static final String HAPROXY_COMMAND_CONNECT_TIMEOUT = "cx.ofd" +
            "" + ".manager.haproxycommandconnecttimeout";

    /**
     * HAProxy命令读取超时时间配置项键。
     */
    public static final String HAPROXY_COMMAND_READ_TIMEOUT = "cx.ofd" + "" +
            ".manager" + ".haproxycommandreadtimeout";

    // **************** 私有变量

    /**
     * 离线下载器节点池。
     */
    private NodePool pool = null;

    /**
     * Kafka线程。
     */
    private Thread thread = null;

    /**
     * Kafka获取标识。
     */
    private volatile boolean poll = true;

    /**
     * 下载请求消息生产者。
     */
    private Producer<Integer, String> producer = null;

    /**
     * Log4j logger。
     */
    private static Logger logger = LogManager.getLogger(Manager.class);

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 保护的构造函数。
     */
    protected Manager() {
        haproxyEndpoint = Config.getNotNull(HAPROXY_ENDPOINT);
        haproxyCommandConnectTimeout = Config.getInt
                (HAPROXY_COMMAND_CONNECT_TIMEOUT, 10000);
        haproxyCommandReadTimeout = Config.getInt
                (HAPROXY_COMMAND_READ_TIMEOUT, 10000);
    }

    /**
     * 启动离线下载管理器。
     */
    public void start() {
        pool = new NodePool(new PoolListener() {
            @Override
            public void nodeDied(DownloaderNode node) {
                Manager.this.nodeDied(node);
            }

            @Override
            public void nodeRecovered(DownloaderNode node) {
                Manager.this.nodeRecovered(node);
            }
        });
        kafkaPollTimeout = Config.getLong(KAFKA_POLL_TIMEOUT, pool
                .nodeHeartbeatInterval);

        producer = Manager.producer();

        thread = new Thread(this::pollCommandFromKafka);
        thread.start();
    }

    /**
     * 设置HAProxy下载服务器。
     *
     * @param id  下载服务器id。
     * @param url 下载资源url。
     */
    protected void setHaproxy(@NotNull String id, @NotNull String url) {
        ObjectNode commandNode = JsonNodeFactory.instance.objectNode();
        commandNode.put("command", "set");
        commandNode.put("downloaderNodeId", id);
        commandNode.put("url", url);
        boolean ret = Manager.sendJsonCommand(new HttpPost(haproxyEndpoint),
                commandNode, haproxyCommandConnectTimeout,
                haproxyCommandReadTimeout);
        if (!ret) {
            logger.error("Error sending set command to haproxy:" +
                    commandNode.toString());
        }
    }

    /**
     * 删除HAProxy下载服务器。
     *
     * @param url 下载资源url。
     */
    protected void removeHaproxy(@NotNull String url) {
        ObjectNode commandNode = JsonNodeFactory.instance.objectNode();
        commandNode.put("command", "remove");
        commandNode.put("url", url);
        boolean ret = Manager.sendJsonCommand(new HttpPost(haproxyEndpoint),
                commandNode, haproxyCommandConnectTimeout,
                haproxyCommandReadTimeout);
        if (!ret) {
            logger.error("Error sending remove command to haproxy:" +
                    commandNode.toString());
        }
    }


    /**
     * 关闭离线下载管理器。
     */
    public void close() {
        poll = false;
        try {
            logger.info("Waiting for kafka thread to close (timeout: " +
                    kafkaPollTimeout + ")...");
            thread.join();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        logger.info("Kafka thread closed.");
        producer.close();
        logger.info("Closing node pool...");
        pool.close();
        logger.info("Manager closed");
    }

    /**
     * 发送命令到HttpPost。
     *
     * @param post                  命令需要被发送到的HttpPost。
     * @param command               要发送的命令。
     * @param commandConnectTimeout 命令连接超时时间。
     * @param commandReadTimeout    命令读取超时时间。
     * @return 命令是否发送成功。
     */
    public static boolean sendJsonCommand(@NotNull HttpPost post, @NotNull
            ObjectNode command, int commandConnectTimeout, int
            commandReadTimeout) {
        post.setConfig(RequestConfig.custom().setConnectTimeout
                (commandConnectTimeout).setSocketTimeout(commandReadTimeout)
                .build());
        post.setHeader("Content-Type", "application/x-www-form-urlencoded;" +
                "charset=utf-8");

        List<NameValuePair> pairList = new ArrayList<>(1);
        try {
            pairList.add(new BasicNameValuePair("command", new ObjectMapper()
                    .writeValueAsString(command)));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        post.setEntity(new UrlEncodedFormEntity(pairList, StandardCharsets
                .UTF_8));
        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response = null;

        try {
            response = client.execute(post);
            HttpEntity responseEntity = response.getEntity();
            JsonNode responseNode = new ObjectMapper().readTree(EntityUtils
                    .toString(responseEntity));
            EntityUtils.consume(responseEntity);

            return "accepted".equals(responseNode.get("response").asText());
        } catch (Exception e) {
            logger.error(e);
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

        return false;
    }

    /**
     * 订阅kafka消费者。
     *
     * @param consumer 要订阅的kafka消费者。
     * @return 订阅后的kafka消费者。
     */
    public static Consumer<Integer, String> subscribe(@NotNull
                                                              Consumer<Integer, String> consumer) {
        consumer.subscribe(Arrays.asList("cx_download_request",
                "cx_download_finished"));
        return consumer;
    }

    /**
     * 根据配置文件生成kafka消费者。
     *
     * @return 根据配置文件生成的Kafka消费者。
     */
    public static Consumer<Integer, String> consumer() {
        Properties properties = new Properties();
        properties.put("zookeeper.connect", Config.getNotNull
                (ZOOKEEPER_SERVERS));
        properties.put("bootstrap.servers", Config.getNotNull
                (KAFKA_BOOTSTRAP_SERVERS));
        properties.put("group.id", "download_request_consumer_manager");
        properties.put("zookeeper.session.timeout.ms", Long.toString(Config
                .getLong(ZOOKEEPER_SESSION_TIMEOUT, 500)));
        properties.put("zookeeper.sync.time.ms", Long.toString(Config.getLong
                (ZOOKEEPER_SYNC_TIME, 250)));
        properties.put("auto.commit.interval.ms", Long.toString(Config
                .getLong(KAFKA_AUTO_COMMIT_INTERVAL, 1000)));
        properties.put("key.deserializer", "org.apache.kafka.common" + "" + "" +
                ".serialization.IntegerDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common" + "" +
                ".serialization.StringDeserializer");
        return new KafkaConsumer<>(properties);
    }

    /**
     * 根据配置文件生成kafka生产者。
     *
     * @return 根据配置文件生成的kafka生产者。
     */
    public static KafkaProducer<Integer, String> producer() {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", Config.getNotNull
                (KAFKA_BOOTSTRAP_SERVERS));
        properties.put("key.serializer", "org.apache.kafka.common" + "" + "" +
                ".serialization.IntegerSerializer");
        properties.put("value.serializer", "org.apache.kafka.common" + "" + "" +
                ".serialization.StringSerializer");
        properties.put("request.required.acks", "1");
        return new KafkaProducer<>(properties);
    }


    // **************** 私有方法

    /**
     * 从kafka获取命令消息。
     */
    private void pollCommandFromKafka() {
        Consumer<Integer, String> consumer = Manager.subscribe(Manager
                .consumer());
        logger.info("Polling from kafka.");

        while (poll) {
            ConsumerRecords<Integer, String> records = consumer.poll
                    (kafkaPollTimeout);
            if (records.count() > 0) {
                logger.info(records.count() + " requests polled from kafka.");
            } else {
                System.out.println("Manager: No requests polled.");
            }
            ObjectMapper mapper = new ObjectMapper();
            StreamSupport.stream(records.spliterator(), false).map(record -> {
                try {
                    return (ObjectNode) mapper.readTree(record.value());
                } catch (Exception e) {
                    logger.error("Wrong command: " + record.value(), e);
                    return null;
                }
            }).filter(command -> command != null).forEach(this::executeCommand);
        }
        consumer.close();
    }

    /**
     * 执行从kafka获取的命令消息。
     *
     * @param command 命令ObjectNode。
     */
    private void executeCommand(ObjectNode command) {
        JsonNode commandNode;
        if ((commandNode = command.get("command")) == null) {
            logger.error("Wrong command:" + command.toString());
            return;
        }

        if ("download".equals(commandNode.asText())) {
            download(command);
        } else if ("downloadFinished".equals(commandNode.asText())) {
            downloadFinished(command);
        } else {
            logger.error("Wrong command:" + command.toString());
        }
    }

    /**
     * 选取离线下载器节点并发送下载任务。
     *
     * @param command 下载命令ObjectNode。
     */
    private void download(ObjectNode command) {
        logger.info("Download command received: " + command.toString());

        if (command.get("url") == null) {
            logger.error("Wrong command:" + command.toString());
            return;
        }

        String url = command.get("url").asText();
        Resource resource;
        if ((resource = ResourceStorage.instance.get(url)) == null) {
            resource = new Resource(url);
            resource.mime = FormalizedMime.produce(command.get("mime").asText
                    ());

            Calendar current = Calendar.getInstance();
            resource.relativePath = current.get(Calendar.YEAR) + "/" +
                    current.get(Calendar.MONTH) + "/" + current.get(Calendar
                    .DAY_OF_MONTH) + (int) System.currentTimeMillis() + "-" +
                    url.hashCode();
        }

        String nodeId = NodeJobStorage.instance.nodeId(url);
        if (!"".equals(nodeId)) {
            logger.info("Download command has already been sent to download "
                    + "node " + nodeId + ". Ignored: " + command.toString());
            return;
        }

        if (resource.status == ResourceStatus.finished) {
            logger.info("The download job has already been finished. " +
                    "Ignored: " + command.toString());
            inform(url, true);
            return;
        } else if (resource.status == ResourceStatus.downloadFailed && System
                .currentTimeMillis() - resource.time.getTime() < 86400000l) {
            // 对于下载失败的链接，一天只下载一次。
            logger.info("The download job has failed in 24 hours. " +
                    "Ignored: " + command.toString());
            inform(url, false);
            return;
        }

        resource.time = new Timestamp(System.currentTimeMillis());
        ResourceStorage.instance.put(resource);

        for (DownloaderNode node = pool.node(); poll; node = pool.node()) {
            logger.info("Trying to send the download command: " + command
                    .toString());
            if (node == null || !node.download(resource)) {
                if (node == null) {
                    logger.info("No downloader node available right now.");
                } else {
                    logger.info("Failed sending download command to " +
                            "downloader node " + node.id + ": " + command
                            .toString());
                }
                try {
                    Thread.sleep(pool.nodeHeartbeatInterval);
                    continue;
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            } else {
                logger.info("Download command sent to downloader node " +
                        node.id + ": " + command.toString());
                NodeJobStorage.instance.put(node.id, resource.url);
                setHaproxy(node.id, resource.url);
                break;
            }
        }
    }

    /**
     * 发送下载完成通知。
     *
     * @param url       下载完成的url。
     * @param succeeded 下载是否成功。
     */
    private void inform(String url, boolean succeeded) {
        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("command", "downloadFinished");
        objectNode.put("succeeded", succeeded);
        objectNode.put("url", url);
        try {
            producer.send(new ProducerRecord<Integer, String>
                    ("cx_download_finished", new ObjectMapper()
                            .writeValueAsString(objectNode)));
        } catch (JsonProcessingException e) {
            logger.error(e);
        }
    }

    /**
     * 从离线下载器节点任务存储中删除离线下载节点任务。
     *
     * @param command 下载完成命令ObjectNode。
     */
    private void downloadFinished(ObjectNode command) {
        logger.info("DownloadFinished command received: " + command.toString());

        if (command.get("url") == null) {
            logger.error("Wrong command:" + command.toString());
            return;
        }

        removeHaproxy(command.get("url").asText());
        NodeJobStorage.instance.remove(command.get("url").asText());
    }

    /**
     * 离线下载器节点已死亡。
     *
     * @param node 死亡的离线下载器节点。
     */
    private void nodeDied(DownloaderNode node) {
        ObjectNode command = JsonNodeFactory.instance.objectNode();
        command.put("command", "download");
        ObjectMapper mapper = new ObjectMapper();

        String[] urls = NodeJobStorage.instance.getAndRemoveResourceUrls(node
                .id);
        Arrays.stream(urls).forEach(url -> {
            removeHaproxy(url);
        });
        Arrays.stream(urls).forEach(url -> {
            command.put("url", url);
            try {
                producer.send(new ProducerRecord<>("cx_download_request",
                        mapper.writeValueAsString(command)));
            } catch (Exception e) {
                logger.error("Error sending download request on node died.", e);
            }
        });
        logger.error("Downloader node " + node.id + " died, " + urls.length +
                " download requests sent back to kafka.");
    }

    /**
     * 离线下载器节点已恢复。
     *
     * @param node 已恢复的离线下载器节点。
     */
    private void nodeRecovered(DownloaderNode node) {
        String[] urls = NodeJobStorage.instance.resourceUrls(node.id);
        if (urls.length > 0 && !node.recover(urls)) {
            ObjectNode command = JsonNodeFactory.instance.objectNode();
            command.put("command", "download");
            ObjectMapper mapper = new ObjectMapper();

            urls = NodeJobStorage.instance.getAndRemoveResourceUrls(node.id);
            Arrays.stream(urls).forEach(url -> {
                command.put("url", url);
                try {
                    producer.send(new ProducerRecord<>("cx_download_request",
                            mapper.writeValueAsString(command)));
                } catch (Exception e) {
                    logger.error("Error sending download request on node " +
                            "" + "recover failed.", e);
                }
            });
            logger.error("Sending recover command to downloader node " + node
                    .id + " failed, " + urls.length + " download " +
                    "requests sent back to kafka.");
        } else {
            logger.info("Node " + node.id + " recovered.");
        }
    }

}
