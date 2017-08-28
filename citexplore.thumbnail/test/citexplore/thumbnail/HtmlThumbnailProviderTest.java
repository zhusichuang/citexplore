package citexplore.thumbnail;

import citexplore.foundation.Config;
import citexplore.offlinedownload.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

/**
 * html缩略图生成测试类。
 *
 * @author Zhu, Sichuang
 * @author Zhang, Yin
 */
public class HtmlThumbnailProviderTest {

    // **************** 公开变量

    // **************** 私有变量

    /**
     * Log4j logger。
     */
    private static Logger logger = LogManager.getLogger
            (HtmlThumbnailProviderTest.class);

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 删除生成的图片。
     */
    @BeforeClass
    public static void deleteImageBefore() {
        ThumbnailProviderTest.cleanIamge();
    }

    /**
     * 删除生成的图片。
     */
    @After
    public void deleteImageAfter() {
        ThumbnailProviderTest.cleanIamge();
    }

    /**
     * 测试生辰缩略图。
     */
    @Test
    public void testThumbnail() {
        HtmlThumbProvider htmlThumbProvider = new HtmlThumbProvider();
        Resource resource = new Resource("http://blog.csdn" +
                ".net/s_ghost/article/details/7466040");
        resource.relativePath = "test/resource/baidu";
        String pathName = htmlThumbProvider.thumbnail(resource, 105, 152,
                Config.getFolder(ThumbnailProvider.WORKING_PATH));

        Assert.assertTrue(new File(pathName).exists());
    }

    /**
     * 测试同时生成多个缩略图。
     */
    @Test
    public void testThumbnailMultiThread() {
        String urls[] = new String[5];
        urls[0] = "https://github.com/Chrriis/DJ-Native-Swing";
        urls[1] = "http://bbs.csdn.net/topics/390961890";
        urls[2] = "http://cn.bing" +
                ".com/search?q=addWebBrowserListener&qs=n&form=QBLH&pq" +
                "=addwebbrowserlistener&sc=0-21&sp=-1&sk=&cvid" +
                "=E213571981FE4B78A7962B571C840E58";
        urls[3] = "http://www.cnblogs.com/IamThat/archive/2013/03/21/2972707" +
                ".html";
        urls[4] = "https://www.baidu" +
                ".com/baidu?tn=monline_3_dg&wd=%E7%88%B1%E5%AE%9D%E7%9A%84%E5" +
                "%A6%8D";

        Resource[] resources = new Resource[urls.length];
        Thread[] threads = new Thread[urls.length];

        for (int i = 0; i < urls.length; i++) {
            int j = i;
            resources[i] = new Resource(urls[i]);
            resources[i].relativePath = "test/resource/" + Math.abs((short) (
                    (urls[i].hashCode())));
            threads[i] = new Thread() {
                @Override
                public void run() {
                    HtmlThumbProvider htmlThumbProvider = new
                            HtmlThumbProvider();
                    String pathName = htmlThumbProvider.thumbnail
                            (resources[j], 105, 152, Config.getFolder
                                    (ThumbnailProvider.WORKING_PATH));
                    Assert.assertTrue(new File(pathName).exists());
                }
            };
        }
        for (int i = 0; i < urls.length; i++) {
            threads[i].start();
        }
        for (int i = 0; i < urls.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // **************** 私有方法

}
