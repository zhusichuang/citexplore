package citexplore.content;

import citexplore.offlinedownload.Resource;
import citexplore.offlinedownload.downloader.DownloadJob;
import net.sourceforge.groboutils.junit.v1.MultiThreadedTestRunner;
import net.sourceforge.groboutils.junit.v1.TestRunnable;
import org.apache.hadoop.fs.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Html内容提供者测试类。
 *
 * @author Zhu, Sichuang
 */
public class PdfContentProviderTest {


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
        Path src = new Path
                ("test/citexplore/content/pdf/pdfContentProviderTest.pdf");
        Resource resource = new Resource("ddsdsd");
        resource.relativePath = "test/resource/" + Math.abs((short) resource
                .url.hashCode());
        Path dst = new Path(DownloadJob.hdfsWorkingPath + resource
                .relativePath);
        try {
            HdfsUtil.instance.hdfs.copyFromLocalFile(src, dst);
            RandomAccessBuffer random = new RandomAccessBuffer(HdfsUtil
                    .instance.hdfs.open(new Path(DownloadJob.hdfsWorkingPath
                            + resource.relativePath)));
            PDFParser pdfParser = new PDFParser(random);
            pdfParser.parse();
            String content = new PDFTextStripper().getText(pdfParser
                    .getPDDocument());
            PdfContentProvider pdfContentProvider = new PdfContentProvider
                    (resource);
            Assert.assertEquals(content, pdfContentProvider.content());
            Assert.assertTrue(HdfsUtil.instance.fileExists(resource));
            Assert.assertEquals(content, HdfsUtil.instance.content(resource));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 多线程测试content函数。
     */
    @Test
    public void testContentMultiThreads() {
        Path src = new Path
                ("test/citexplore/content/pdf/pdfContentProviderTest.pdf");
        Resource resource = new Resource("ddsdsd");
        resource.relativePath = "test/resource/" + Math.abs((short) resource
                .url.hashCode());
        Path dst = new Path(DownloadJob.hdfsWorkingPath + resource
                .relativePath);
        String content;
        try {
            HdfsUtil.instance.hdfs.copyFromLocalFile(src, dst);
            RandomAccessBuffer random = new RandomAccessBuffer(HdfsUtil
                    .instance.hdfs.open(new Path(DownloadJob.hdfsWorkingPath
                            + resource.relativePath)));
            PDFParser pdfParser = new PDFParser(random);
            pdfParser.parse();
            content = new PDFTextStripper().getText(pdfParser.getPDDocument());

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        TestRunnable[] threads = new TestRunnable[100];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new TestRunnable() {
                @Override
                public void runTest() throws Throwable {
                    PdfContentProvider pdfContentProvider = new
                            PdfContentProvider(resource);
                    Assert.assertEquals(content, pdfContentProvider.content());
                }
            };
        }
        MultiThreadedTestRunner multiThread = new MultiThreadedTestRunner
                (threads);
        try {
            multiThread.runTestRunnables();
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        Assert.assertTrue(HdfsUtil.instance.fileExists(resource));
        Assert.assertEquals(content, HdfsUtil.instance.content(resource));
    }

    // **************** 私有方法
}
