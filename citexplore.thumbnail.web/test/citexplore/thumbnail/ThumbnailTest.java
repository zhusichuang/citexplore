package citexplore.thumbnail;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.hadoop.fs.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.commands.kv.DeleteValue;
import com.basho.riak.client.api.commands.kv.ListKeys;
import com.basho.riak.client.core.RiakCluster;
import com.basho.riak.client.core.RiakNode;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import com.sun.istack.internal.NotNull;
import citexplore.foundation.Config;
import citexplore.offlinedownload.Resource;
import citexplore.offlinedownload.ResourceStatus;
import citexplore.offlinedownload.ResourceStorage;

public class ThumbnailTest {

	// **************** 公开变量

	// **************** 私有变量

	/**
	 * Log4j logger。
	 */
	private static Logger logger = LogManager.getLogger(ThumbnailTest.class);

	// **************** 继承方法

	// **************** 公开方法
	/**
	 * 清理hdfs、riak和已经生成的图片。
	 */
	@BeforeClass
	public static void cleanAllBefore() {
		CleanHdfs();
		CleanIamge();
		cleanRiak();
	}

	/**
	 * 清理hdfs、riak和已经生成的图片。
	 */
	@After
	public void cleanAllAfter() {
		CleanHdfs();
		CleanIamge();
		cleanRiak();
	}

	/**
	 * 清理hdfs。
	 */
	public static void CleanHdfs() {
		logger.info("Cleaning hdfs...");
		Path path = new Path(HdfsUtil.hdfsWorkingPath + "test");
		try {
			HdfsUtil.instance.hdfs.delete(path, true);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 删除生成的图片。
	 */
	public static void CleanIamge() {
		logger.info("Delete iamge...");

		File dir = new File( Config.getFolder(ThumbnailProvider.WORKING_PATH)
				+ "test/resource");
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				new File(dir, children[i]).delete();
			}
		}
	}

	/**
	 * 清理Riak。
	 */
	public static void cleanRiak() {
		logger.info("Cleaning riak...");
		RiakClient client = clientFromRiakServers(
				Config.getNotNull(ResourceStorage.RIAK_SERVERS));
		Namespace namespace = new Namespace(ResourceStorage.RIAK_BUCKET);
		ListKeys lk = new ListKeys.Builder(namespace).build();
		try {
			ListKeys.Response response = client.execute(lk);
			for (Location location : response) {
				DeleteValue dv = new DeleteValue.Builder(location).build();
				client.execute(dv);
			}
		} catch (Exception e) {
			logger.info(e);
			e.printStackTrace();
		}
		client.shutdown();
	}

	/**
	 * 根据资源信息存储Riak服务器获得资源信息存储RiakClient。
	 *
	 * @param riakServers
	 *            资源信息存储Riak服务器。
	 * @return 资源信息存储RiakClient。
	 */
	protected static RiakClient clientFromRiakServers(
			@NotNull String riakServers) {
		RiakCluster cluster;
		RiakNode.Builder template = new RiakNode.Builder();
		try {
			cluster = RiakCluster.builder(
					(ArrayList<RiakNode>) Arrays.stream(riakServers.split(";"))
							.map(server -> server.split(":"))
							.collect(ArrayList<RiakNode>::new, (list, pair) -> {
								try {
									list.add(template.withRemoteAddress(pair[0])
											.withRemotePort(
													Integer.parseInt(pair[1]))
											.build());
								} catch (Exception e) {
									logger.error(e.getMessage(), e);
									throw new RuntimeException(e);
								}
							}, ArrayList::addAll))
					.build();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}

		cluster.start();
		return new RiakClient(cluster);
	}

	/**
	 * 测试缩略图生成功能。
	 */
	@Test
	public void testThumbnail() {
		Path src = new Path(
				"test/citexplore/thumbnail/pdf/pdfContentProviderTest.pdf");
		Resource resource = new Resource("ddsdsd");
		resource.relativePath = "test/resource/"
				+ Math.abs((short) resource.url.hashCode());
		resource.mime = "application/pdf";
		resource.status = ResourceStatus.finished;
		ResourceStorage.instance.put(resource);
		Path dst = new Path(HdfsUtil.hdfsWorkingPath + resource.relativePath);

		String url = "https://www.baidu.com/s?ie=utf-8&f=3&rsv_bp=1&tn=monline_3_dg&wd=%E6%B1%89%E5%AD%97%E8%BD%ACurl%E7%BC%96%E7%A0%81%20java&oq=%E6%B1%89%E5%AD%97%E8%BD%ACurl%E7%BC%96%E7%A0%81&rsv_pq=8b83023d0001a3c5&rsv_t=aee7qL9MmcED817rDL8RYptwMNdQDmMufpx30edJilz0S6fK8AxhT1Jt5YL44QiBaYF3&rsv_enter=1&inputT=1335&rsv_sug3=11&rsv_sug1=8&rsv_sug7=100&rsv_sug2=0&prefixsug=%E6%B1%89%E5%AD%97%E8%BD%ACurl%E7%BC%96%E7%A0%81%20java&rsp=0&rsv_sug4=3464";
		String urle = "";
		try {
			urle = URLEncoder.encode(url, "utf-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		Resource resource2 = new Resource(url);
		resource2.mime = "text/html";
		resource2.relativePath = "test/resource/"
				+ Math.abs((short) resource2.url.hashCode());
		resource2.status = ResourceStatus.finished;

		ResourceStorage.instance.put(resource2);

		try {
			HdfsUtil.instance.hdfs.copyFromLocalFile(src, dst);

			URL realUrl = new URL(
					"http://localhost:8080/citexplore.thumbnail.web/thumbnail.jsp?url="
							+ resource.url);
			URLConnection conn = realUrl.openConnection();
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			conn.setDoInput(true);
			conn.getInputStream();

			Assert.assertTrue(
					new File( Config.getFolder(ThumbnailProvider.WORKING_PATH)
							+ resource.relativePath + ".1."
							+ Integer.parseInt(Config.getNotNull(
									ThumbnailProvider.THUMBNAIL_WIDTH))
							+ "x"
							+ Integer.parseInt(Config.getNotNull(
									ThumbnailProvider.THUMBNAIL_HEIGHT))
							+ ".png").exists());

			URL realUrl1 = new URL(
					"http://localhost:8080/citexplore.thumbnail.web/thumbnail.jsp?url="
							+ urle);
			URLConnection conn1 = realUrl1.openConnection();
			conn1.setRequestProperty("accept", "*/*");
			conn1.setRequestProperty("connection", "Keep-Alive");
			conn1.setRequestProperty("user-agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			conn1.setDoInput(true);
			conn1.getInputStream();

			Assert.assertTrue(
					new File(Config.getFolder(ThumbnailProvider.WORKING_PATH)
							+ resource2.relativePath + ".1."
							+ Integer.parseInt(Config.getNotNull(
									ThumbnailProvider.THUMBNAIL_WIDTH))
							+ "x"
							+ Integer.parseInt(Config.getNotNull(
									ThumbnailProvider.THUMBNAIL_HEIGHT))
							+ ".png").exists());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
}
