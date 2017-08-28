package citexplore.offlinedownload;

import citexplore.foundation.Config;
import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.commands.kv.DeleteValue;
import com.basho.riak.client.api.commands.kv.ListKeys;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Timestamp;

/**
 * 资源信息存储测试类。
 *
 * @author Zhu, Sichuang
 */
public class ResourceStorageTest {

    // **************** 公开变量

    // **************** 私有变量

    /**
     * Log4j logger。
     */
    private static Logger logger = LogManager.getLogger(ResourceStorageTest
            .class);

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 清理Riak。
     */
    @BeforeClass
    public static void beforeClass() {
        ResourceStorageTest.cleanRiak();
    }

    /**
     * 清理Riak。
     */
    @After
    public void after() {
        ResourceStorageTest.cleanRiak();
    }

    /**
     * 清理Riak。
     */
    public static void cleanRiak() {
        logger.info("Cleaning riak...");
        RiakClient client = ResourceStorage.clientFromRiakServers(Config
                .getNotNull(ResourceStorage.RIAK_SERVERS));
        Namespace namespace = new Namespace(ResourceStorage.RIAK_BUCKET);
        ListKeys lk = new ListKeys.Builder(namespace).build();
        try {
            ListKeys.Response response = client.execute(lk);
            for (Location location : response) {
                DeleteValue dv = new DeleteValue.Builder(location).build();
                client.execute(dv);
            }
        } catch (Exception e) {
            logger.info(e);
            e.printStackTrace();
        }
        client.shutdown();
    }

    /**
     * 测试riak存储读取功能。
     */
    @Test
    public void testResourceStorage() {
        Resource resource = new Resource("http://download.apache.com");
        resource.mime = FormalizedMime.produce("pdf");
        resource.time = new Timestamp(5);
        resource.relativePath = "/home/ww.pdf";
        resource.status = ResourceStatus.finished;

        Resource resource3 = ResourceStorage.instance.get(resource.url);
        Assert.assertEquals(resource3, null);

        ResourceStorage.instance.put(resource);

        Resource resource2 = ResourceStorage.instance.get(resource.url);

        Assert.assertEquals(resource.url, resource2.url);
        Assert.assertEquals(resource.mime, resource2.mime);
        Assert.assertEquals(resource.relativePath, resource2.relativePath);
        Assert.assertEquals(resource.status, resource2.status);
        Assert.assertEquals(resource.time, resource2.time);
    }

    // **************** 私有方法

}
