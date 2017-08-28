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
public class WileyPdfUrlRedirectorTest {

    // **************** 公开变量

    // **************** 私有变量

    /**
     * Log4j logger。
     */
    private static Logger logger = LogManager.getLogger(WileyPdfUrlRedirectorTest.class);

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 测试redirectImpl函数。
     */
    @Test
    public void testRedirectImpl() {
        WileyPdfUrlRedirector wileyPdfUrlRedirector = new
        		WileyPdfUrlRedirector();

        try {	
        	//new article view
        	
            UrlRedirection urlRedirection = wileyPdfUrlRedirector
					.redirectImpl("http://onlinelibrary.wiley.com/doi/10.1002/asi.22955/full");
            urlRedirection = wileyPdfUrlRedirector
					.redirectImpl(urlRedirection.redirectedUrl);
            Assert.assertEquals("application/pdf", NetUtil.urlConnection
					(urlRedirection.redirectedUrl).getContentType());
            Assert.assertTrue(!urlRedirection.chain);
            
            //old article view
            urlRedirection = wileyPdfUrlRedirector
					.redirectImpl("http://onlinelibrary.wiley.com/wol1/doi/10.1002/asi.22955/full");
            urlRedirection = wileyPdfUrlRedirector
					.redirectImpl(urlRedirection.redirectedUrl);
            logger.info(urlRedirection.redirectedUrl);
            Assert.assertEquals("application/pdf", NetUtil.urlConnection
					(urlRedirection.redirectedUrl).getContentType());
            Assert.assertTrue(!urlRedirection.chain);
            urlRedirection = wileyPdfUrlRedirector
					.redirectImpl("http://onlinelibrary.wiley.com/doi/10.1002/9781444328202.ch11/pdf");
            logger.info(urlRedirection.redirectedUrl);
            Assert.assertEquals("application/pdf", NetUtil.urlConnection(urlRedirection.redirectedUrl).getContentType());
           
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

    }
    // **************** 私有方法

}
