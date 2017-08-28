package citexplore.offlinedownload.downloader;

import citexplore.foundation.util.NetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Springer pdf url重定向类。
 *
 * @author Zhu, Sichuang
 * @author Zhang, Yin
 */
public class WileyPdfUrlRedirector extends PdfUrlRedirector {

	// **************** 公开变量

	// **************** 私有变量

	/**
	 * Log4j logger。
	 */
	private static Logger logger = LogManager
			.getLogger(WileyPdfUrlRedirector.class);

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
		String html = NetUtil.fetchStringSafely(url);
		Pattern pattern = null;
		Matcher matcher = null;
		if(url.contains("pdf")){
			pattern = Pattern.compile(
					"<\\s*iframe\\s+id\\s*=\\s*[\"']?pdfDocument[\"']?\\s"
							+ "+src\\s*=\\s*[\"']?([^\"']+)[\"']?");
			matcher = pattern.matcher(html);
			if (matcher.find()) {
				return new UrlRedirection(matcher.group(1), false);
			}
		}
		pattern = Pattern.compile(
				"name\\s*=\\s*[\"']?citation_pdf_url[\"']?\\s"
						+ "+content\\s*=\\s*[\"']?([^\"']+)[\"']?");
		matcher = pattern.matcher(html);
		if (matcher.find()) {
			return new UrlRedirection(matcher.group(1), true);
		}

		return new UrlRedirection(url, false);
	}

	// **************** 公开方法

	// **************** 私有方法

}
