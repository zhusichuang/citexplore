package citexplore.offlinedownload.downloader;

import citexplore.foundation.util.NetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Springer pdf url重定向测试类。
 *
 * @author Zhu, Sichuang
 */
public class SpringerPdfUrlRedirectorTest {

    // **************** 公开变量

    // **************** 私有变量

    /**
     * Log4j logger。
     */
    private static Logger logger = LogManager.getLogger(SpringerPdfUrlRedirectorTest.class);

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 测试redirectImpl函数。
     */
    @Test
    public void testRedirectImpl() {
        SpringerPdfUrlRedirector springerPdfUrlRedirector = new
				SpringerPdfUrlRedirector();

        try {

            UrlRedirection urlRedirection = springerPdfUrlRedirector
					.redirectImpl("http://link.springer.com/chapter/10" +
							".1007/3-540-44533-1_114");
            Assert.assertEquals("application/pdf", NetUtil.urlConnection
					(urlRedirection.redirectedUrl).getContentType());
            Assert.assertTrue(!urlRedirection.chain);

            urlRedirection = springerPdfUrlRedirector.redirectImpl
					("http://link.springer.com/article/10" +
							".1023/A%3A1002893125231");
            Assert.assertEquals("application/pdf", NetUtil.urlConnection
					(urlRedirection.redirectedUrl).getContentType());
            Assert.assertTrue(!urlRedirection.chain);

            urlRedirection = springerPdfUrlRedirector.redirectImpl
					("http://link.springer.com/referenceworkentry/10" +
							".1007/978-1-4020-5614-7_1809");
            Assert.assertEquals("application/pdf", NetUtil.urlConnection
					(urlRedirection.redirectedUrl).getContentType());
            Assert.assertTrue(!urlRedirection.chain);

            urlRedirection = springerPdfUrlRedirector.redirectImpl
					("http://link.springer.com/book/10" +
							".1007/978-1-4419-7716-8");

            Assert.assertEquals("application/pdf", NetUtil.urlConnection
					(urlRedirection.redirectedUrl).getContentType());
            Assert.assertTrue(!urlRedirection.chain);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

    }
    // **************** 私有方法

}
