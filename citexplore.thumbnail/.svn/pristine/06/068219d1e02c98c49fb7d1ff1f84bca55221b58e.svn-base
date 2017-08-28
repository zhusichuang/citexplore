package citexplore.thumbnail;

import java.io.File;
import java.io.IOException;
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
           
/**
 * 缩略图提供测试类。
 * 
 * @author Zhu,Sichuang
 */
public class ThumbnailProviderTest {

	// **************** 公开变量

	// **************** 私有变量

	/**
	 * Log4j logger。
	 */
	private static Logger logger = LogManager
			.getLogger(ThumbnailProviderTest.class);

	// **************** 继承方法

	// **************** 公开方法

	/**
	 * 清理riak、hdfs和已经生成的缩略图。
	 */
	@BeforeClass
	public static void cleanAllBefore() {
		CleanHdfs();
		cleanRiak();
		cleanIamge();
	}

	/**
	 * 清理riak、hdfs和已经生成的缩略图。
	 */
	@After
	public void cleanAllAfter() {
		CleanHdfs();
		cleanRiak();
		cleanIamge();
	}

	/**
	 * 删除生成的图片。
	 */
	public static void cleanIamge() {
		logger.info("Delete iamge...");

		File dir = new File(Config.getFolder(ThumbnailProvider.WORKING_PATH)
				+ "test/resource");
		dir.mkdirs();
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				new File(dir, children[i]).delete();
			}
		}
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
	 * 测试pdf缩略图生成。
	 */
	@Test
	public void testPdfThumbnail() {
		Path src = new Path(
				"test/citexplore/thumbnail/pdf/pdfContentProviderTest.pdf");
		Resource resource = new Resource("ddsdsddsd");
		resource.relativePath = "test/resource/"
				+ Math.abs((short) resource.url.hashCode());
		Path dst = new Path(HdfsUtil.hdfsWorkingPath + resource.relativePath);
		try {
			HdfsUtil.instance.hdfs.copyFromLocalFile(src, dst);
			Assert.assertTrue(
					new File(ThumbnailProvider.instance.pdfThumbnail(resource))
							.exists());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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

		Resource resource2 = new Resource(
				"http://news.ifeng.com/a/20160516/48779462_0.shtml");
		resource2.mime = "text/html";
		resource2.relativePath = "test/resource/"
				+ Math.abs((short) resource2.url.hashCode());
		resource2.status = ResourceStatus.finished;

		ResourceStorage.instance.put(resource2);

		try {
			HdfsUtil.instance.hdfs.copyFromLocalFile(src, dst);
			Assert.assertTrue(
					new File(ThumbnailProvider.instance.thumbnail("ddsdsd"))
							.exists());
			Assert.assertTrue(new File(ThumbnailProvider.instance.thumbnail(
					"http://news.ifeng.com/a/20160516/48779462_0.shtml"))
							.exists());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// **************** 私有方法

}
