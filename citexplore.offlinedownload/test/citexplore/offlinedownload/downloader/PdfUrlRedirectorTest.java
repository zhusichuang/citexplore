package citexplore.offlinedownload.downloader;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.commands.kv.FetchValue;
import com.basho.riak.client.api.commands.kv.ListKeys;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import citexplore.foundation.Config;
import citexplore.foundation.util.NetUtil;
import citexplore.offlinedownload.ResourceStorage;

/**
 * Pdf url重定向类跳转工具测试类。
 *
 * @author Zhu,Sichuang
 */
public class PdfUrlRedirectorTest {

	// **************** 公开变量

	// **************** 私有变量

	/**
	 * Log4j logger。
	 */
	private static Logger logger = LogManager
			.getLogger(PdfUrlRedirectorTest.class);

	// **************** 继承方法

	// **************** 公开方法

	/**
	 * 清理Riak。
	 */
	@BeforeClass
	public static void beforeClass() {
		RedirectionStorageTest.cleanRiak();
	}

	/**
	 * 清理Riak。
	 */
	@After
	public void after() {
		RedirectionStorageTest.cleanRiak();
	}

	/**
	 * 测试redirect函数。
	 */
	@Test
	public void testRedirectWithBaiduUrlRedirectTOSpringer() {
		RiakClient client = null;
		try {

			String url = "http://xueshu.baidu.com/s?wd=paperur"
					+ "i%3A%281503019fdc2c85e3d360d064df0cbeb"
					+ "f%29&filter=sc_long_sign&tn=SE_xueshusource"
					+ "_2kduw22v&sc_vurl=http%3A%2F%2F"
					+ "www.springer.com%2Fcomputer%2Fbook%2F"
					+ "978-1-4020-3005-5&ie=utf-8&s"
					+ "c_us=8104385654433093923";
			String redirectedUrl = PdfUrlRedirector.produce(url).redirect(url);
			Assert.assertEquals("application/pdf",
					NetUtil.urlConnection(redirectedUrl).getContentType());

			client = ResourceStorage.clientFromRiakServers(
					Config.getNotNull(RedirectionStorage.RIAK_SERVERS));
			Namespace namespace = new Namespace(RedirectionStorage.RIAK_BUCKET);
			ListKeys lk = new ListKeys.Builder(namespace).build();

			ListKeys.Response response = client.execute(lk);
			Map<String, UrlRedirection> map = new HashMap();
			for (Location location : response) {
				FetchValue fv = new FetchValue.Builder(location).build();
				String url1 = location.getKeyAsString();
				UrlRedirection urlRedirection = UrlRedirection
						.fromJson((ObjectNode) new ObjectMapper().readTree(
								client.execute(fv).getValue(String.class)));
				map.put(url1, urlRedirection);
			}
			String url2 = url;
			while (!map.get(url2).redirectedUrl.equals(redirectedUrl)) {

				Assert.assertFalse(!map.get(url2).chain);
				url2 = map.get(url2).redirectedUrl;
			}

			Assert.assertTrue(!map.get(url2).chain);

		} catch (Exception e) {
			logger.info(e);
			e.printStackTrace();
		} finally {
			if (client != null)
				client.shutdown();
		}

	}

	/**
	 * 测试redirect函数。
	 */
	@Test
	public void testRedirectWithBaiduUrlRedirectToAcm() {
		RiakClient client = null;
		try {

			String url = "http://xueshu.baidu.com/s?wd=paperuri%3A"
					+ "%287b511275e33a04961408a2439e8e04ea%29&filter=sc_lon"
					+ "g_sign&tn=SE_xueshusource_2kduw22v&sc_vurl=http%3A%2F"
					+ "%2Fdl.acm.org%2Fcitation.cfm%3Fid%3D2071390&ie=utf-"
					+ "&sc_us=2791519033881063727";
			String redirectedUrl = PdfUrlRedirector.produce(url).redirect(url);
			Assert.assertEquals("application/pdf",
					NetUtil.urlConnection(redirectedUrl).getContentType());

			client = ResourceStorage.clientFromRiakServers(
					Config.getNotNull(RedirectionStorage.RIAK_SERVERS));
			Namespace namespace = new Namespace(RedirectionStorage.RIAK_BUCKET);
			ListKeys lk = new ListKeys.Builder(namespace).build();

			ListKeys.Response response = client.execute(lk);
			Map<String, UrlRedirection> map = new HashMap();
			for (Location location : response) {
				FetchValue fv = new FetchValue.Builder(location).build();
				String url1 = location.getKeyAsString();
				UrlRedirection urlRedirection = UrlRedirection
						.fromJson((ObjectNode) new ObjectMapper().readTree(
								client.execute(fv).getValue(String.class)));
				map.put(url1, urlRedirection);
			}
			String url2 = url;
			while (!map.get(url2).redirectedUrl.equals(redirectedUrl)) {

				Assert.assertFalse(!map.get(url2).chain);
				url2 = map.get(url2).redirectedUrl;
			}

			Assert.assertTrue(!map.get(url2).chain);

		} catch (Exception e) {
			logger.info(e);
			e.printStackTrace();
		} finally {
			if (client != null)
				client.shutdown();
		}

	}

	/**
	 * 测试redirect函数。
	 */
	@Test
	public void testRedirectWithBaiduUrlRedirectToScienceDirect() {
		RiakClient client = null;
		try {

			String url = "http://xueshu.baidu.com/s?wd"
					+ "=paperuri%3A%285ad7ea5454a97ec"
					+ "3668344107aee830e%29&filter=sc_lon"
					+ "g_sign&tn=SE_xueshusource_2kduw22v&s"
					+ "c_vurl=http%3A%2F%2Fwww.sciencedirect."
					+ "com%2Fscience%2Farticle%2Fpii%2FS03064573"
					+ "00000169&ie=utf-8&sc_us=14077818803356856702";
			String redirectedUrl = PdfUrlRedirector.produce(url).redirect(url);
			Assert.assertEquals("application/pdf",
					NetUtil.urlConnection(redirectedUrl).getContentType());

			client = ResourceStorage.clientFromRiakServers(
					Config.getNotNull(RedirectionStorage.RIAK_SERVERS));
			Namespace namespace = new Namespace(RedirectionStorage.RIAK_BUCKET);
			ListKeys lk = new ListKeys.Builder(namespace).build();

			ListKeys.Response response = client.execute(lk);
			Map<String, UrlRedirection> map = new HashMap();
			for (Location location : response) {
				FetchValue fv = new FetchValue.Builder(location).build();
				String url1 = location.getKeyAsString();

				logger.info((client.execute(fv).getValue(String.class)));
				UrlRedirection urlRedirection = UrlRedirection
						.fromJson((ObjectNode) new ObjectMapper().readTree(
								client.execute(fv).getValue(String.class)));
				map.put(url1, urlRedirection);
			}
			String url2 = url;
			while (!map.get(url2).redirectedUrl.equals(redirectedUrl)) {

				Assert.assertFalse(!map.get(url2).chain);
				url2 = map.get(url2).redirectedUrl;
			}

			Assert.assertTrue(!map.get(url2).chain);

		} catch (Exception e) {
			logger.info(e);
			e.printStackTrace();
		} finally {
			if (client != null)
				client.shutdown();
		}

	}

	/**
	 * 测试redirect函数。
	 */
	@Test
	public void testRedirectWithBaiduUrlRedirectToIeee() {
		RiakClient client = null;
		try {

			String url = "http://xueshu.baidu.com/s?wd=paperuri%3A%"
					+ "2814ff6b7380d120708d10505abf0944aa%29&filter"
					+ "=sc_long_sign&tn=SE_xueshusource_2kduw22v&sc"
					+ "_vurl=http%3A%2F%2Fieeexplore.ieee.org%2Fxpls"
					+ "%2Fabs_all.jsp%3Farnumber%3D623969&ie=utf-8"
					+ "&sc_us=14038227920206746036";
			String redirectedUrl = PdfUrlRedirector.produce(url).redirect(url);
			Assert.assertEquals("application/pdf",
					NetUtil.urlConnection(redirectedUrl).getContentType());

			client = ResourceStorage.clientFromRiakServers(
					Config.getNotNull(RedirectionStorage.RIAK_SERVERS));
			Namespace namespace = new Namespace(RedirectionStorage.RIAK_BUCKET);
			ListKeys lk = new ListKeys.Builder(namespace).build();

			ListKeys.Response response = client.execute(lk);
			Map<String, UrlRedirection> map = new HashMap();
			for (Location location : response) {
				FetchValue fv = new FetchValue.Builder(location).build();
				String url1 = location.getKeyAsString();
				UrlRedirection urlRedirection = UrlRedirection
						.fromJson((ObjectNode) new ObjectMapper().readTree(
								client.execute(fv).getValue(String.class)));
				map.put(url1, urlRedirection);
			}
			String url2 = url;
			while (!map.get(url2).redirectedUrl.equals(redirectedUrl)) {

				Assert.assertFalse(!map.get(url2).chain);
				url2 = map.get(url2).redirectedUrl;
			}

			Assert.assertTrue(!map.get(url2).chain);

		} catch (Exception e) {
			logger.info(e);
			e.printStackTrace();
		} finally {
			if (client != null)
				client.shutdown();
		}

	}

	/**
	 * 测试从缓存中读取。
	 */
	@Test
	public void testGetFromCache() {
		String url = "http://xueshu.baidu.com/s?wd=paperur";
		UrlRedirection urlRedirection = new UrlRedirection(
				"http://xueshu.baidu.com/s?wd=paperur2323", true);
		UrlRedirection urlRedirection2 = new UrlRedirection(
				"http://xueshu.baidu.com/s?wd=p43aperu3434r", false);
		RedirectionStorage.instance.put(url, urlRedirection);
		RedirectionStorage.instance.put(urlRedirection.redirectedUrl,
				urlRedirection2);

		String url2 = PdfUrlRedirector.produce(url).redirect(url);

		Assert.assertEquals(urlRedirection2.redirectedUrl, url2);

	}
	// **************** 私有方法

}
