package citexplore.offlinedownload.downloader;

import citexplore.foundation.util.NetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Springer pdf url重定向类。
 *
 * @author Zhu, Sichuang
 * @author Zhang, Yin
 */
public class CnkiPdfUrlRedirector extends PdfUrlRedirector {

	// **************** 公开变量

	// **************** 私有变量

	/**
	 * Log4j logger。
	 */
	private static Logger logger = LogManager
			.getLogger(CnkiPdfUrlRedirector.class);

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
		
		int start;
		int end;
		
		if((start =html.indexOf("downArticle"))!=-1){
			int temp;
			temp = html.indexOf("data-fn", start);
			end = html.indexOf("\"", temp+9);
			String fn = html.substring(temp+9, end);
			temp = html.indexOf("data-dbcode", start);
			end = html.indexOf("\"", temp+13);
			String dbcode = html.substring(temp+13,end);
			temp = html.indexOf("data-year", start);
			end = html.indexOf("\"", temp+11);
			String year = html.substring(temp+11,end);
			temp = html.indexOf("data-fn", start);
			end = html.indexOf("\"", temp+9);
			String redirectUrl = "http://search.cnki.net/down/default.aspx?filename="+fn+"&dbcode="+dbcode+"&year="+year+"&dflag=pdfdown";
			//redirectUrl = getUrlLocation( getUrlLocation( getUrlLocation( getUrlLocation(redirectUrl))));
			return new UrlRedirection(redirectUrl, false);
		}
		
		return new UrlRedirection(url, false);
	}

	private String getUrlLocation(String url){
		logger.info(url);
		String location="";
		try {
			URLConnection urlConnection = NetUtil.urlConnection(url);
			for(Entry<String, List<String>>entry :urlConnection.getHeaderFields().entrySet()){
				//logger.info(entry.getKey());
				logger.info(entry.getValue());
			}
			
			location = (urlConnection).getHeaderField("null");
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
		}
		return location;
	}
	// **************** 公开方法

	// **************** 私有方法
	
}
