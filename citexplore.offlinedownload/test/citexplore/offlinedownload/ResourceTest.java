package citexplore.offlinedownload;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.javacrumbs.jsonunit.JsonAssert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.sql.Timestamp;

/**
 * 资源信息测试类。
 *
 * @author Zhu, Sichuang
 */
public class ResourceTest {

    // **************** 公开变量

    // **************** 私有变量

    /**
     * Log4j logger。
     */
    private static Logger logger = LogManager.getLogger(ResourceTest.class);

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 测试json函数的基本功能。
     */
    @Test
    public void testJson() {
        Resource resource = new Resource("http://download.apache.com");
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        resource.json(node);
        try {
            JsonAssert.assertJsonEquals(new ObjectMapper().readTree(new File
                    (getClass().getResource("json/TestResourceJson.json")
                            .getFile())), node);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 测试json函数的基本功能，带有其他参数。
     */
    @Test
    public void testJsonWithParam() {
        Resource resource = new Resource("http://download.apache.com");
        resource.mime = FormalizedMime.produce("pdf");
        resource.time = new Timestamp(5);
        resource.relativePath = "/home/1.pdf";
        resource.status = ResourceStatus.finished;
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        resource.json(node);
        try {
            JsonAssert.assertJsonEquals(new ObjectMapper().readTree(new File
                    (getClass().getResource("json/TestResourceJsonWithParam"
                            + ".json").getFile())), node);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 测试fromJson的基本功能。
     */
    @Test
    public void testFromJson() {
        Resource resource = new Resource("http://download.apache.com");
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode node = mapper.readTree(new File(getClass().getResource
                    ("json/TestResourceFromJson.json").getFile()));
            Resource resource2 = Resource.formJson((ObjectNode) node);
            Assert.assertEquals(resource.url, resource2.url);
            Assert.assertEquals(resource.mime, resource2.mime);
            Assert.assertEquals(resource.time, resource2.time);
            Assert.assertEquals(resource.relativePath, resource2.relativePath);
            Assert.assertEquals(resource.status, resource2.status);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 测试fromJson的基本功能，带有其他参数。
     */
    @Test
    public void testFromJsonWithParam() {
        Resource resource = new Resource("http://download.apache.com");
        resource.mime = FormalizedMime.produce("pdf");
        resource.time = new Timestamp(5);
        resource.relativePath = "/home/1.pdf";
        resource.status = ResourceStatus.finished;

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode node = mapper.readTree(new File(getClass().getResource
                    ("json/TestResourceFromJsonWithParam.json").getFile()));
            Resource resource2 = Resource.formJson((ObjectNode) node);
            Assert.assertEquals(resource.url, resource2.url);
            Assert.assertEquals(resource.mime, resource2.mime);
            Assert.assertEquals(resource.time, resource2.time);
            Assert.assertEquals(resource.relativePath, resource2.relativePath);
            Assert.assertEquals(resource.status, resource2.status);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    // **************** 私有方法
}
