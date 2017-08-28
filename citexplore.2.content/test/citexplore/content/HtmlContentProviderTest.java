package citexplore.content;

import citexplore.offlinedownload.Resource;
import citexplore.offlinedownload.downloader.DownloadJob;
import net.sourceforge.groboutils.junit.v1.MultiThreadedTestRunner;
import net.sourceforge.groboutils.junit.v1.TestRunnable;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

/**
 * Html内容提供者测试类。
 *
 * @author Zhu, Sichuang
 */
public class HtmlContentProviderTest {


    // **************** 公开变量

    // **************** 私有变量

    /**
     * Log4j logger。
     */
    private static Logger logger = LogManager.getLogger(HdfsUnitTest.class);

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 清里hdfs。
     */
    @BeforeClass
    public static void cleanHdfsBefore() {
        HdfsUnitTest.CleanHdfs();
    }

    /**
     * 清里hdfs。
     */
    @After
    public void cleanHdfsAfter() {
        HdfsUnitTest.CleanHdfs();
    }

    /**
     * 测试content函数。
     */
    @Test
    public void testContent() {
        Resource resource = new Resource("sadsadsadsaffaewqe");
        resource.relativePath = "test/resource/" + Math.abs((short) resource
                .url.hashCode());
        Assert.assertFalse(HdfsUtil.instance.fileExists(resource));
        String fileData = "<html><body><h4>表头：</h4><table " +
                "border=\"1\"><tr><th>姓名</th><th>电话</th><th>电话</th></tr><tr" +
                "><td>Bill Gates</td><td>555 77 854</td><td>555 77 " +
                "855</td></tr></table><h4>垂直的表头：</h4><table " +
                "border=\"1\"><tr><th>姓名</th><td>Bill " +
                "Gates</td></tr><tr><th>电话</th><td>555 77 " +
                "854</td></tr><tr><th>电话</th><td>555 77 " +
                "855</td></tr></table></body></html>";

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
        String content = Jsoup.parse(fileData).text();
        HtmlContentProvider htmlContentProvider = new HtmlContentProvider
                (resource);
        Assert.assertEquals(content, htmlContentProvider.content());
        Assert.assertTrue(HdfsUtil.instance.contentExists(resource));
        Assert.assertEquals(content, HdfsUtil.instance.content(resource));
    }

    /**
     * 多线程测试content函数。
     */
    @Test
    public void testContentMultiThreads() {
        Resource resource = new Resource("sadsadsadsaffaewqe");
        resource.relativePath = "test/resource/" + Math.abs((short) resource
                .url.hashCode());
        Assert.assertFalse(HdfsUtil.instance.fileExists(resource));
        String fileData = "<html><body><h4>表头：</h4><table " +
                "border=\"1\"><tr><th>姓名</th><th>电话</th><th>电话</th></tr><tr" +
                "><td>Bill Gates</td><td>555 77 854</td><td>555 77 " +
                "855</td></tr></table><h4>垂直的表头：</h4><table " +
                "border=\"1\"><tr><th>姓名</th><td>Bill " +
                "Gates</td></tr><tr><th>电话</th><td>555 77 " +
                "854</td></tr><tr><th>电话</th><td>555 77 " +
                "855</td></tr></table></body></html>";

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

        String content = Jsoup.parse(fileData).text();
        TestRunnable[] threads = new TestRunnable[100];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new TestRunnable() {
                @Override
                public void runTest() throws Throwable {
                    HtmlContentProvider htmlContentProvider = new
                            HtmlContentProvider(resource);
                    Assert.assertEquals(content, htmlContentProvider.content());
                }
            };
        }
        try {
            new MultiThreadedTestRunner(threads).runTestRunnables();
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        Assert.assertTrue(HdfsUtil.instance.contentExists(resource));
        String content1 = HdfsUtil.instance.content(resource);
        Assert.assertEquals(content, content1);
    }

    // **************** 私有方法
}
