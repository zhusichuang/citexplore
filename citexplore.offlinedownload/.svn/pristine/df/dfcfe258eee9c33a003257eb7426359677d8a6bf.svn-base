package citexplore.offlinedownload.downloader;

import citexplore.foundation.Config;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

/**
 * 下载完成通知器。
 *
 * @author Zhu, Sichuang
 */
public class JobFinishedInformer {

    // **************** 公开变量

    /**
     * 全局唯一下载完成通知器。
     */
    public static final JobFinishedInformer instance = new
            JobFinishedInformer();

    /**
     * 下载完成通知器Kafka bootstrap服务器配置项键。
     */
    public static final String KAFKA_BOOTSTRAP_SERVERS = "cx.ofd" + "" +
            ".jobfinishedinformer.kafkabootstrapservers";

    // **************** 私有变量

    /**
     * 下载完成消息生产者。
     */
    private Producer<Integer, String> producer = null;

    /**
     * Log4j logger。
     */
    private static Logger logger = LogManager.getLogger(JobFinishedInformer
            .class);

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 保护的构造函数。
     */
    protected JobFinishedInformer() {
        Properties producerProperties = new Properties();
        producerProperties.put("bootstrap.servers", Config.getNotNull
                (KAFKA_BOOTSTRAP_SERVERS));
        producerProperties.put("key.serializer", "org.apache.kafka.common" +
                ".serialization.IntegerSerializer");
        producerProperties.put("value.serializer", "org.apache.kafka.common"
                + ".serialization.StringSerializer");
        producerProperties.put("request.required.acks", "1");
        producer = new KafkaProducer<>(producerProperties);
    }

    /**
     * 关闭下载完成通知器。
     */
    public void close() {
        producer.close();
    }

    /**
     * 进行下载完成通知。
     *
     * @param url       下载完成的url。
     * @param succeeded 下载是否成功。
     */
    public void inform(String url, boolean succeeded) {
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

    // **************** 私有方法


}
