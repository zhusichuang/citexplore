package citexplore.offlinedownload.downloader;

import citexplore.foundation.util.UrlUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Pdf url重定向抽象类。
 *
 * @author Zhang, Yin
 */
public abstract class PdfUrlRedirector {

    // **************** 公开变量

    // **************** 私有变量

    /**
     * Log4j logger。
     */
    private static Logger logger = LogManager.getLogger(PdfUrlRedirector.class);

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 重定向url。
     *
     * @param url url。
     * @return 重定向的Url。
     */
    public String redirect(String url) {
        UrlRedirection redirection = RedirectionStorage.instance.get(url);
        if (redirection == null) {
            redirection = redirectImpl(url);
            RedirectionStorage.instance.put(url, redirection);
        }

        return redirection.chain ? PdfUrlRedirector.produce(redirection
                .redirectedUrl).redirect(redirection.redirectedUrl) :
                redirection.redirectedUrl;
    }

    /**
     * 重定向url。
     *
     * @param url url。
     * @return 包含重定向信息的UrlRedirection对象。
     */
    protected abstract UrlRedirection redirectImpl(String url);

    /**
     * 根据url获取pdf url重定向工具。
     *
     * @param url Url。
     * @return pdf url重定向工具。
     */
    public static PdfUrlRedirector produce(String url) {
        String domain = UrlUtil.domain(url).toLowerCase();

        if (domain.equals("xueshu.baidu.com")) {
            return new BaiduXueshuPdfUrlRedirector();
        }

        if (domain.contains("acm.org")) {
            return new AcmDlPdfUrlRedirector();
        }

        if (domain.contains("ieee")) {
            return new IeeeXplorePdfUrlRedirector();
        }

        if (domain.contains("springer")) {
            return new SpringerPdfUrlRedirector();
        }

        if (domain.contains("sciencedirect")) {
            return new ScienceDirectPdfUrlRedirector();
        }

        if (domain.contains("wiley")) {
            return new WileyPdfUrlRedirector();
        }
        
        return new NoRedirectPdfUrlRedirector(url);
    }

    // **************** 私有方法

}
