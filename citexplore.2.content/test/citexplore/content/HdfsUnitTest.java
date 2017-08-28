package citexplore.content;

import citexplore.offlinedownload.Resource;
import citexplore.offlinedownload.downloader.DownloadJob;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

/**
 * Hdfs工具测试类。
 *
 * @author Zhu, Sichuang
 */
public class HdfsUnitTest {

    // **************** 公开变量

    // **************** 私有变量

    /**
     * Log4j logger。
     */
    private static Logger logger = LogManager.getLogger(HdfsUnitTest.class);

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 清理hdfs。
     */
    public static void CleanHdfs() {
        logger.info("Cleaning hdfs...");
        Path path1 = new Path(DownloadJob.hdfsWorkingPath + "test");
        Path path2 = new Path(HdfsUtil.hdfsWorkingPath + "test");
        try {
            HdfsUtil.instance.hdfs.delete(path1, true);
            HdfsUtil.instance.hdfs.delete(path2, true);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 清理hdfs。
     */
    @BeforeClass
    public static void cleanHdfsBefore() {
        CleanHdfs();
    }

    /**
     * 清理hdfs。
     */
    @After
    public void cleanHdfsAfter() {
        CleanHdfs();
    }

    /**
     * 测试content的相关方法。
     */
    @Test
    public void contentTest() {
        Resource resource = new Resource("dsadsadsadfebdffsaewqdfqwe");
        resource.relativePath = "test/resource/" + Math.abs((short) resource
                .url.hashCode());
        Assert.assertFalse(HdfsUtil.instance.contentExists(resource));
        String content =
                "汉语汉字，是一个一字多音的字“的”字有四个读音。读作：de，常用结构助词。如：这把尺子是小花的尺子。读作：d" +
                        "í，真实、实在，如“的确”“的当”“的证”“的真”。读作：dì，箭靶的中心，如“目的”“无的放矢”“众矢之的”。读作：dī，2012年第六版《现代汉语词典》正式将“的士”中的“的”注音为dī。";
        HdfsUtil.instance.write(resource, content);
        Assert.assertTrue(HdfsUtil.instance.contentExists(resource));

        String content1 = HdfsUtil.instance.content(resource);
        Assert.assertEquals(content, content1);
    }

    /**
     * 测试原文件的相关方法。
     */
    @Test
    public void fileTest() {
        Resource resource = new Resource("sadsadsadsaffaewqe");
        resource.relativePath = "test/resource/" + Math.abs((short) resource
                .url.hashCode());
        Assert.assertFalse(HdfsUtil.instance.fileExists(resource));
        String fileData =
                "汉语汉字，是一个一字多音的字“的”字有四个读音。读作：de，常用结构助词。如：这把尺子是小花的尺子。读作：d" +
                        "í，真实、实在，如“的确”“的当”“的证”“的真”。读作：dì，箭靶的中心，如“目的”“无的放矢”“众矢之的”。读作：dī，2012年第六版《现代汉语词典》正式将“的士”中的“的”注音为dī。";

        Path path = new Path(DownloadJob.hdfsWorkingPath + resource
                .relativePath);
        FSDataOutputStream out = null;
        try {
            out = HdfsUtil.instance.hdfs.create(path);
            out.writeUTF(fileData);
            out.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        Assert.assertTrue(HdfsUtil.instance.fileExists(resource));
        String filedata1 = HdfsUtil.instance.stringFile(resource);
        Assert.assertEquals(fileData, filedata1);
    }
}
