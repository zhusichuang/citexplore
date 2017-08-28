package citexplore.offlinedownload.downloader;

import citexplore.foundation.util.NetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Zhu, Sichuang
 */
public class IeeeXplorePdfUrlRedirectorTest {

    // **************** 公开变量

    // **************** 私有变量

    /**
     * Log4j logger。
     */
    private static Logger logger = LogManager.getLogger(IeeeXplorePdfUrlRedirectorTest.class);

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 测试redirectImpl函数。
     */
    @Test
    public void testRedirectImpl() {
        IeeeXplorePdfUrlRedirector ieeeXplorePdfUrlRedirector = new
				IeeeXplorePdfUrlRedirector();
        UrlRedirection urlRedirection = ieeeXplorePdfUrlRedirector
				.redirectImpl("http://ieeexplore.ieee.org" +
						"/xpl/articleDetails.jsp?arnumber=4579949&" +
						"newsearch=true&queryText=information%20retrieval");
        try {
            Assert.assertEquals("application/pdf", NetUtil.urlConnection
					(urlRedirection.redirectedUrl).getContentType());
            Assert.assertTrue(!urlRedirection.chain);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        
        urlRedirection = ieeeXplorePdfUrlRedirector
				.redirectImpl("http://ieeexplore.ieee.org/document/4039288/?part=1");
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
