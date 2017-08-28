package citexplore.offlinedownload.downloader;

import org.apache.commons.lang.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 无跳转pdf url跳转工具。
 *
 * @author Zhang, Yin
 */
public class NoRedirectPdfUrlRedirector extends PdfUrlRedirector {

	// **************** 公开变量

	// **************** 私有变量

	/**
	 * url。
	 */
	private String url = "";

	/**
	 * Log4j logger。
	 */
	private static Logger logger = LogManager
			.getLogger(NoRedirectPdfUrlRedirector.class);

	// **************** 继承方法

	/**
	 * 重定向url。
	 *
	 * @param url
	 *            url。
	 * @return 重定向的Url。
	 */
	@Override
	public String redirect(String url) {
		return this.url;
	}

	/**
	 * 重定向url。
	 *
	 * @param url
	 *            url。
	 * @return 包含重定向信息的UrlRedirection对象。
	 */
	@Override
	public UrlRedirection redirectImpl(String url) {
		throw new NotImplementedException();
	}

	// **************** 公开方法

	/**
	 * 无跳转pdf url跳转工具构造函数。
	 *
	 * @param url
	 *            url。
	 */
	public NoRedirectPdfUrlRedirector(String url) {
		this.url = url;
	}

	// **************** 私有方法

}
