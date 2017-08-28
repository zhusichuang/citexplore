package citexplore.offlinedownload.manager;

import citexplore.foundation.Config;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 离线下载器节点任务存储测试类。
 *
 * @author Zhang, Yin
 */
public class NodeJobStorageTest {

    // **************** 公开变量

    // **************** 私有变量

    /**
     * Log4j logger。
     */
    private static Logger logger = LogManager.getLogger(NodeJobStorageTest
            .class);

    // **************** 继承方法

    // **************** 公开方法

    @BeforeClass
    @AfterClass
    public static void cleanMongoDb() {
        MongoClient client = NodeJobStorage.clientFromMongodbServers(Config
                .get(NodeJobStorage.MONGODB_SERVERS, "localhost:27017"));
        MongoCollection collection = client.getDatabase("cx").getCollection
                ("ofd_nodejobstorage");
        collection.drop();
        client.close();
    }

    /**
     * 测试remove函数基本功能。
     */
    @Test
    public void testRemove() {
        NodeJobStorage storage = new NodeJobStorage();
        storage.put("t1", "r1");
        storage.remove("r1");
        Assert.assertEquals(0, storage.nodeResourceMap.get("t1").size());
        Assert.assertEquals(0, storage.resourceNodeMap.size());

        MongoClient client = NodeJobStorage.clientFromMongodbServers(Config
                .get(NodeJobStorage.MONGODB_SERVERS, "localhost:27017"));
        MongoCollection collection = client.getDatabase("cx").getCollection
                ("ofd_nodejobstorage");
        Document d = new Document();
        d.put("nodeId", "t1");
        d.put("resourceUrl", "r1");
        Assert.assertNull(collection.findOneAndDelete(d));
        client.close();
    }

    /**
     * 测试获取功能。
     */
    @Test
    public void testGets() {
        NodeJobStorage storage = new NodeJobStorage();
        storage.put("t1", "r1");
        Assert.assertEquals("t1", storage.nodeId("r1"));
        Assert.assertEquals("r1", storage.resourceUrls("t1")[0]);
        Assert.assertEquals("r1", storage.getAndRemoveResourceUrls("t1")[0]);
        storage.close();

        MongoClient client = NodeJobStorage.clientFromMongodbServers(Config
                .get(NodeJobStorage.MONGODB_SERVERS, "localhost:27017"));
        MongoCollection collection = client.getDatabase("cx").getCollection
                ("ofd_nodejobstorage");
        Document d = new Document();
        d.put("nodeId", "t1");
        d.put("resourceUrl", "r1");
        Assert.assertNull(collection.findOneAndDelete(d));
        client.close();
    }

    /**
     * 测试set函数基本功能。
     */
    @Test
    public void testSet() {
        NodeJobStorage storage = new NodeJobStorage();
        storage.put("t1", "r1");
        Assert.assertEquals("t1", storage.resourceNodeMap.get("r1"));
        storage.close();

        MongoClient client = NodeJobStorage.clientFromMongodbServers(Config
                .get(NodeJobStorage.MONGODB_SERVERS, "localhost:27017"));
        MongoCollection collection = client.getDatabase("cx").getCollection
                ("ofd_nodejobstorage");
        Document d = new Document();
        d.put("nodeId", "t1");
        d.put("resourceUrl", "r1");
        Assert.assertNotNull(collection.findOneAndDelete(d));
        client.close();
    }

    /**
     * 测试构造函数。
     */
    @Test
    public void testConstructor() {
        MongoClient client = NodeJobStorage.clientFromMongodbServers(Config
                .get(NodeJobStorage.MONGODB_SERVERS, "localhost:27017"));
        MongoCollection collection = client.getDatabase("cx").getCollection
                ("ofd_nodejobstorage");

        Document d1, d2, d3, d4;

        d1 = new Document();
        d1.put("nodeId", "t1");
        d1.put("resourceUrl", "r2");
        collection.insertOne(d1);

        d2 = new Document();
        d2.put("nodeId", "t1");
        d2.put("resourceUrl", "r3");
        collection.insertOne(d2);

        d3 = new Document();
        d3.put("nodeId", "t2");
        d3.put("resourceUrl", "r4");
        collection.insertOne(d3);

        d4 = new Document();
        d4.put("nodeId", "t2");
        d4.put("resourceUrl", "r5");
        collection.insertOne(d4);

        NodeJobStorage storage = new NodeJobStorage();
        Assert.assertEquals(2, storage.nodeResourceMap.size());
        Assert.assertEquals(2, storage.nodeResourceMap.get("t1").size());
        Assert.assertEquals(2, storage.nodeResourceMap.get("t2").size());
        Assert.assertTrue(storage.nodeResourceMap.get("t1").get("r3"));
        Assert.assertTrue(storage.nodeResourceMap.get("t2").get("r5"));
        Assert.assertEquals("t2", storage.resourceNodeMap.get("r5"));
        storage.close();

        collection.findOneAndDelete(d1);
        collection.findOneAndDelete(d2);
        collection.findOneAndDelete(d3);
        collection.findOneAndDelete(d4);
        client.close();
    }

    // **************** 私有方法

}
