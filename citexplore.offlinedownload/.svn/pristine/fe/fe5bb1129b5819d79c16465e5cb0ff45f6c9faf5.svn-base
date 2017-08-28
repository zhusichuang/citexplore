package citexplore.offlinedownload.downloader;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * 资源信息测试类。
 *
 * @author Zhu, Sichuang
 */
public class JobStatusTest {

    // **************** 公开变量

    // **************** 私有变量

    /**
     * Log4j logger。
     */
    private static Logger logger = LogManager.getLogger(JobStatusTest.class);

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 测试json函数的基本功能。
     */
    @Test
    public void testJson() {
    	JobStatus jobStatus = new JobStatus();
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        jobStatus.json(node);
        try {
            JsonAssert.assertJsonEquals(new ObjectMapper().readTree(new File
                    (getClass().getResource("../json/TestJobStatusJson.json")
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
    	JobStatus jobStatus = new JobStatus();

    	jobStatus.stage = JobStage.downloading;
    	jobStatus.progress = 100;
    	jobStatus.totalProgress = 1000;
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        jobStatus.json(node);
        try {
            JsonAssert.assertJsonEquals(new ObjectMapper().readTree(new File
                    (getClass().getResource("../json/TestJobStatusJsonWithParam"
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
        
    	JobStatus jobStatus = new JobStatus();
    	
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode node = mapper.readTree(new File(getClass().getResource
                    ("../json/TestJobStatusFromJson.json").getFile()));
            JobStatus jobStatus2 = JobStatus.formJson((ObjectNode)node);
            
            Assert.assertEquals(jobStatus.stage, jobStatus2.stage);
            Assert.assertEquals(jobStatus.progress, jobStatus2.progress);
            Assert.assertEquals(jobStatus.totalProgress, jobStatus2.totalProgress);

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
    	
    	JobStatus jobStatus = new JobStatus();
    	jobStatus.stage = JobStage.downloading;
    	jobStatus.progress = 100;
    	jobStatus.totalProgress = 1000;

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode node = mapper.readTree(new File(getClass().getResource
                    ("../json/TestJobStatusFromJsonWithParam.json").getFile()));
            JobStatus jobStatus2 = JobStatus.formJson((ObjectNode)node);
            
            Assert.assertEquals(jobStatus.stage, jobStatus2.stage);
            Assert.assertEquals(jobStatus.progress, jobStatus2.progress);
            Assert.assertEquals(jobStatus.totalProgress, jobStatus2.totalProgress);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    // **************** 私有方法
}
