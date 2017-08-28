package citexplore.offlinedownload;

import citexplore.foundation.Config;
import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.commands.kv.DeleteValue;
import com.basho.riak.client.api.commands.kv.FetchValue;
import com.basho.riak.client.api.commands.kv.StoreValue;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

/**
 * Riak基本功能测试类。
 *
 * @author Zhang, Yin
 */
public class RiakTest {

    // **************** 公开变量

    // **************** 私有变量

    /**
     * Log4j logger。
     */
    private static Logger logger = LogManager.getLogger(RiakTest.class);

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 测试riak基本功能。
     */
    @Test
    public void testRiak() {
        try {
            String[] ss = Config.getNotNull(ResourceStorage.RIAK_SERVERS)
                    .split(";")[0].split(":");
            RiakClient client = RiakClient.newClient(Integer.parseInt(ss[1]),
                    ss[0]);
            Location location = new Location(new Namespace
                    ("citexplore_resource"), "k1");
            StoreValue sv = new StoreValue.Builder("v1").withLocation
                    (location).build();
            client.execute(sv);

            FetchValue fv = new FetchValue.Builder(location).build();
            FetchValue.Response fvResponse = client.execute(fv);
            Assert.assertEquals("v1", fvResponse.getValue(String.class));

            DeleteValue dv = new DeleteValue.Builder(location).build();
            client.execute(dv);

            client.shutdown();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    // **************** 私有方法

}
