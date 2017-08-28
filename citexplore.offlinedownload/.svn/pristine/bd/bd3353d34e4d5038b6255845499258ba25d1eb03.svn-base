package citexplore.offlinedownload.downloader;

import citexplore.foundation.Config;
import com.sun.istack.internal.NotNull;
import org.apache.htrace.fasterxml.jackson.databind.JsonNode;
import org.apache.htrace.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Properties;

/**
 * 下载完成通知器测试类。
 *
 * @author Zhu, Sichuang
 */
public class JobFinishedInformerTest {

    // **************** 公开变量

    /**
     * 下载完成通知器Zookeeper服务器配置项键。
     */
    public static final String ZOOKEEPER_SERVERS = "cx.ofd" + "" + "" +
            ".jobfinishedinformertest.zookeeperservers";

    /**
     * 离线下载管理器Zookeeper会话超时时间配置项键。
     */
    public static final String ZOOKEEPER_SESSION_TIMEOUT = "cx.ofd" + "" + "" +
            ".jobfinishedinformertest.zookeepersessiontimeout";

    /**
     * 离线下载管理器zookeeper.sync.time.ms配置项键。
     */
    public static final String ZOOKEEPER_SYNC_TIME = "cx.ofd" + "" + "" +
            ".jobfinishedinformertest.zookeepersynctime";

    /**
     * 离线下载管理器auto.commit.interval.ms配置项键。
     */
    public static final String KAFKA_AUTO_COMMIT_INTERVAL = "cx.ofd" + "" + "" +
            ".jobfinishedinformertest.kafkaautocommitinterval";

    // **************** 私有变量

    /**
     * Log4j logger。
     */
    private static Logger logger = LogManager.getLogger
            (JobFinishedInformerTest.class);

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 清空kafka。
     */
    @BeforeClass
    public static void cleanKafkaBefore() {
        cleanKafka();
    }

    /**
     * 清空kafka。
     */
    @After
    public void cleanKafkaAfter() {
        cleanKafka();
    }

    /**
     * 测试下载完成通知功能。
     */
    @Test
    public void testInform() {
        String url = "http://pdf.d.cnki.net/cjfdsearch/pdfdownloadnew.asp?" +
                "encode=gb&nettype=cnet&zt=I136&filename=" +
                "TS5wmcEVlTxpFcNNmZLFHckFWewYkUhtmTBNGUC9EZSNlaYVUSrcU" +
                "ZlBFa00WRN52dwYnVxcHUOtEW4NTSxV2YY92Mp1GVPN3cXBnRSNXYh" +
                "lmd=0TRHJVV2hXOLZXOGV0NpJ0YxhDTpZFWUdEM2knW390NlNkasV2" +
                "Mrh2b6NVWLdGd2EVTuVzbURjVCVWOI9kQx5mRroFRZh1TlZkb6Z2ZJV" +
                "VZ4E&doi=CNKI:SUN:JSJX.0.2013-01-014&filetitle=%ce%ef%c" +
                "1%aa%cd%f8%cc%e5%cf%b5%bd%e1%b9%b9%d3%eb%ca%b5%cf%d6%b" +
                "7%bd%b7%a8%b5%c4%b1%c8%bd%cf%d1%d0%be%bf_%b3%c2%ba%a" +
                "3%c3%f7&p=CJFQ&cflag=&pager=170-190";
        JobFinishedInformer.instance.inform(url, true);
        Consumer<Integer, String> consumer = subscribe(consumer());
        ConsumerRecords<Integer, String> records = consumer.poll(Long
                .MAX_VALUE);
        for (ConsumerRecord<Integer, String> record : records) {
            try {
                JsonNode jsonNode = new ObjectMapper().readTree(record.value());
                Assert.assertEquals(url, jsonNode.get("url").asText());
                Assert.assertEquals("downloadFinished", jsonNode.get
                        ("command").asText());
                Assert.assertTrue(jsonNode.get("succeeded").asBoolean());
            } catch (Exception e) {
                logger.info(e);
                e.printStackTrace();
            }
            consumer.close();
        }
    }

    /**
     * 订阅kafka消费者。
     *
     * @param consumer 要订阅的kafka消费者。
     * @return 订阅后的kafka消费者。
     */
    protected static Consumer<Integer, String> subscribe(@NotNull
                                                                 Consumer<Integer, String> consumer) {
        consumer.subscribe(Arrays.asList("cx_download_finished"));
        return consumer;
    }

    /**
     * 根据配置文件生成kafka消费者。
     *
     * @return 根据配置文件生成的Kafka消费者。
     */
    protected static Consumer<Integer, String> consumer() {
        Properties properties = new Properties();
        properties.put("zookeeper.connect", Config.getNotNull
                (ZOOKEEPER_SERVERS));
        properties.put("bootstrap.servers", Config.getNotNull
                (JobFinishedInformer.KAFKA_BOOTSTRAP_SERVERS));
        properties.put("group.id",
                "download_request_consumer_jobfinishedinformertest");
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
     * 清空kafka。
     */
    public static void cleanKafka() {
        logger.info("Cleaning kafka...");
        Consumer<Integer, String> consumer = subscribe(consumer());
        consumer.poll(5000);
        consumer.close();
    }

    // **************** 私有方法

}
