package citexplore.offlinedownload.downloader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import citexplore.foundation.util.NetUtil;

/**
 * Acm dl pdf url重定向类。
 *
 * @author Zhu, SiChuang
 * @author Zhang, Yin
 */
public class AcmDlPdfUrlRedirector extends PdfUrlRedirector {

	// **************** 公开变量

	// **************** 私有变量

	/**
	 * Log4j logger。
	 */
	private static Logger logger = LogManager
			.getLogger(AcmDlPdfUrlRedirector.class);

	// **************** 继承方法

	/**
	 * 重定向url。
	 *
	 * @param url
	 *            url。
	 * @return 包含重定向信息的UrlRedirection对象。
	 */
	@Override
	public UrlRedirection redirectImpl(String url) {
		String redirectedUrl = "";
		String html = NetUtil.fetchStringSafely(url);
		int start;
		if ((start = html.indexOf("FullText PDF")) != -1) {
			redirectedUrl = url.substring(0, url.lastIndexOf("/") + 1)
					+ html.substring(html.indexOf("href=\"", start) + 6, html
							.indexOf("\"", html.indexOf("href=\"", start) + 6));
		}
		return redirectedUrl.equals("") ? new UrlRedirection(url, false)
				: new UrlRedirection(redirectedUrl, false);
	}

	// **************** 公开方法

	// **************** 私有方法

}
