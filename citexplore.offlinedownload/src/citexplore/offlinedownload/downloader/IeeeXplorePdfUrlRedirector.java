package citexplore.offlinedownload.downloader;

import citexplore.foundation.util.NetUntilTest;
import citexplore.foundation.util.NetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Ieee xplore pdf url重定向类。
 *
 * @author Zhu, SiChuang
 * @author Zhang, Yin
 */
public class IeeeXplorePdfUrlRedirector extends PdfUrlRedirector {

	// **************** 公开变量

	// **************** 私有变量

	/**
	 * Log4j logger。
	 */
	private static Logger logger = LogManager
			.getLogger(IeeeXplorePdfUrlRedirector.class);

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
		if ((start = html.indexOf("Full Text as PDF")) != -1) {
			html = NetUtil.fetchStringSafely(url.substring(0,
					url.indexOf("/", url.indexOf("://") + 3))
					+ html.substring(html.indexOf("href", start) + 6, html
							.indexOf("'", html.indexOf("href", start) + 6)));
			redirectedUrl = html.substring(html.lastIndexOf("frame src=") + 11,
					html.indexOf("\"", html.lastIndexOf("frame src=") + 11));

		} else if ((start = html.indexOf("fullTextAccess")) != -1) {
			start = html.indexOf("pdfUrl", start);
			if (start != -1) {
				int end = html.indexOf("\"", start + 9);
				if (end > start) {
					html = NetUtil
							.fetchStringSafely("http://ieeexplore.ieee.org"
									+ html.substring(start + 9, end));

					redirectedUrl = html.substring(
							html.lastIndexOf("frame src=") + 11, html.indexOf(
									"\"", html.lastIndexOf("frame src=") + 11));
				}
			}
		}
		return redirectedUrl.equals("") ? new UrlRedirection(url, false)
				: new UrlRedirection(redirectedUrl, false);
	}

	// **************** 公开方法

	// **************** 私有方法

}
