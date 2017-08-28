package citexplore.offlinedownload.downloader;

import citexplore.foundation.util.NetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ScienceDirect pdf url重定向类。
 * 
 * @author Zhang, Yin
 */
public class ScienceDirectPdfUrlRedirector extends PdfUrlRedirector {

    // **************** 公开变量

    // **************** 私有变量

    /**
     * Log4j logger。
     */
    private static Logger logger = LogManager.getLogger(ScienceDirectPdfUrlRedirector.class);

    // **************** 继承方法

    /**
     * 重定向url。
     *
     * @param url url。
     * @return 包含重定向信息的UrlRedirection对象。
     */
    @Override
    public UrlRedirection redirectImpl(String url) {
        String redirectedUrl = "";
        String html = NetUtil.fetchStringSafely(url);
        int end;
        if ((end = html.indexOf("Download PDF")) != -1) {
            redirectedUrl = html.substring(html.lastIndexOf("pdfurl", end) +
					8, html.indexOf("\"", html.lastIndexOf("pdfurl", end) +
					8));
        }
        return redirectedUrl.equals("") ? new UrlRedirection(url, false) :
				new UrlRedirection(redirectedUrl, false);
    }

    // **************** 公开方法

    // **************** 私有方法

}
