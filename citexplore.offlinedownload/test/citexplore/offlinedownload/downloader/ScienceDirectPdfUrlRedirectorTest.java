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
public class ScienceDirectPdfUrlRedirectorTest {

    // **************** 公开变量

    // **************** 私有变量

    /**
     * Log4j logger。
     */
    private static Logger logger = LogManager.getLogger(ScienceDirectPdfUrlRedirectorTest.class);

    // **************** 继承方法


    // **************** 公开方法

    /**
     * 测试redirect函数。
     */
    @Test
    public void testRedirect() {
        ScienceDirectPdfUrlRedirector scienceDirectPdfUrlRedirector = new
				ScienceDirectPdfUrlRedirector();
        UrlRedirection urlRedirection = scienceDirectPdfUrlRedirector
				.redirectImpl("http://www.sciencedirect" +
						".com/science/article/pii/S0957417416302627");

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
