package citexplore.offlinedownload.downloader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 百度学术pdf url重定向测试类。
 *
 * @author Zhu, Sichuang
 */
public class BaiduXueshuPdfUrlRedirectorTest {

	// **************** 公开变量

	// **************** 私有变量

	/**
	 * Log4j logger。
	 */
	private static Logger logger = LogManager
			.getLogger(BaiduXueshuPdfUrlRedirectorTest.class);

	// **************** 继承方法

	// **************** 公开方法

	/**
	 * 测试redirectImpl函数。
	 */
	@Test
	public void testRedirectImpl() {
		BaiduXueshuPdfUrlRedirector baiduXueshuPdfUrlRedirector = new BaiduXueshuPdfUrlRedirector();

		UrlRedirection urlRedirection = baiduXueshuPdfUrlRedirector
				.redirectImpl("http://xueshu.baidu.com/s?wd=paperur"
						+ "i%3A%281503019fdc2c85e3d360d064df0cbeb"
						+ "f%29&filter=sc_long_sign&tn=SE_xueshusource"
						+ "_2kduw22v&sc_vurl=http%3A%2F%2F" + "www.springer"
						+ ".com%2Fcomputer%2Fbook%2F"
						+ "978-1-4020-3005-5&ie=utf-8&s"
						+ "c_us=8104385654433093923");
		Pattern pattern = Pattern.compile("^http://www.springer.com");
		Matcher matcher = pattern.matcher(urlRedirection.redirectedUrl);
		Assert.assertTrue(matcher.find());
		Assert.assertFalse(!urlRedirection.chain);

		urlRedirection = baiduXueshuPdfUrlRedirector
				.redirectImpl("http://xueshu.baidu.com/s?wd=paperuri%3A"
						+ "%287b511275e33a04961408a2439e8e04ea%29&filter=sc_lon"
						+ "g_sign&tn=SE_xueshusource_2kduw22v&sc_vurl=http%3A"
						+ "%2F" + "%2Fdl.acm.org%2Fcitation"
						+ ".cfm%3Fid%3D2071390&ie=utf-"
						+ "&sc_us=2791519033881063727");

		pattern = Pattern.compile("^http://dl.acm.org");
		matcher = pattern.matcher(urlRedirection.redirectedUrl);
		Assert.assertTrue(matcher.find());
		Assert.assertFalse(!urlRedirection.chain);

		urlRedirection = baiduXueshuPdfUrlRedirector
				.redirectImpl("http://xueshu.baidu.com/s?wd"
						+ "=paperuri%3A%285ad7ea5454a97ec"
						+ "3668344107aee830e%29&filter=sc_lon"
						+ "g_sign&tn=SE_xueshusource_2kduw22v&s"
						+ "c_vurl=http%3A%2F%2Fwww.sciencedirect."
						+ "com%2Fscience%2Farticle%2Fpii%2FS03064573"
						+ "00000169&ie=utf-8&sc_us=14077818803356856702");
		pattern = Pattern.compile("^http://www.sciencedirect.com");
		matcher = pattern.matcher(urlRedirection.redirectedUrl);
		Assert.assertTrue(matcher.find());
		Assert.assertFalse(!urlRedirection.chain);

		urlRedirection = baiduXueshuPdfUrlRedirector
				.redirectImpl("http://xueshu.baidu.com/s?wd=paperuri%3A%"
						+ "2814ff6b7380d120708d10505abf0944aa%29&filter"
						+ "=sc_long_sign&tn=SE_xueshusource_2kduw22v&sc"
						+ "_vurl=http%3A%2F%2Fieeexplore.ieee.org%2Fxpls"
						+ "%2Fabs_all.jsp%3Farnumber%3D623969&ie=utf-8"
						+ "&sc_us=14038227920206746036");
		pattern = Pattern.compile("^http://ieeexplore.ieee.org");
		matcher = pattern.matcher(urlRedirection.redirectedUrl);
		Assert.assertTrue(matcher.find());
		Assert.assertFalse(!urlRedirection.chain);

		urlRedirection = baiduXueshuPdfUrlRedirector
				.redirectImpl("http://xueshu.baidu.com/s?wd=paperuri%3A%"
						+ "28ef300a343ae0152629a66bebcac6d86f%29&filter=s"
						+ "c_long_sign&tn=SE_xueshusource_2kduw22v&sc_vur"
						+ "l=http%3A%2F%2Fonlinelibrary.wiley.com%2Fdoi%"
						+ "2F10.1002%2Fasi.22955%2Ffull&ie=utf-8&sc_us="
						+ "18217102857396828339");
		pattern = Pattern.compile("^http://onlinelibrary.wiley.com/");
		matcher = pattern.matcher(urlRedirection.redirectedUrl);
		Assert.assertTrue(matcher.find());
		Assert.assertFalse(!urlRedirection.chain);

	}
	// **************** 私有方法

}
