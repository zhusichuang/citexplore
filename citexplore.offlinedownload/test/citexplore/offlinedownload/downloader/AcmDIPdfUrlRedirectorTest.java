package citexplore.offlinedownload.downloader;

import citexplore.foundation.util.NetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Acm dl pdf url重定向测试类。
 *
 * @author Zhu, Sichuang
 */
public class AcmDIPdfUrlRedirectorTest {

    // **************** 公开变量

    // **************** 私有变量

    /**
     * Log4j logger。
     */
    private static Logger logger = LogManager.getLogger(AcmDIPdfUrlRedirectorTest.class);

    // **************** 继承方法


    // **************** 公开方法

    /**
     * 测试redirectImpl函数。
     */
    @Test
    public void testRedirectImpl() {
        AcmDlPdfUrlRedirector acmDlPdfUrlRedirector = new
				AcmDlPdfUrlRedirector();
        UrlRedirection urlRedirection = acmDlPdfUrlRedirector.redirectImpl
				("http://dl.acm.org/citation" +
						".cfm?id=2063654&CFID=804544847&CFTOKEN=21269996");

        try {
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
