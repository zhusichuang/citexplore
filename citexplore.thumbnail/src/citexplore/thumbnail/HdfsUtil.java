package citexplore.thumbnail;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.istack.internal.NotNull;

import citexplore.foundation.Config;
import citexplore.offlinedownload.Resource;

/**
 * Hdfs访问工具类。
 *
 * @author Zhu,Sichuang
 */
public class HdfsUtil {

	// **************** 公开变量

	/**
	 * 全局唯一Hdfs访问工具类实例，
	 */
	public static final HdfsUtil instance = new HdfsUtil();

	/**
	 * Hdfs访问对象。
	 */
	protected FileSystem hdfs;

	/**
	 * HDFS服务器配置项键。
	 */
	public static final String HDFS_SERVERS = "cx.tmb.hdfsutil.hdfsservers";

	/**
	 * hdfs工作路径。
	 */
	public static String hdfsWorkingPath = "/opt/citexplore/offlinedownload/";

	// **************** 私有变量

	/**
	 * Log4j logger。
	 */
	private static Logger logger = LogManager.getLogger(HdfsUtil.class);

	// **************** 继承方法

	// **************** 公开方法
	
	/**
	 * 得到资源的输入流。
	 * 
	 * @param resource 资源。
	 * @return 输入流。
	 */
	public InputStream inputStream(@NotNull Resource resource) {
		Path path = new Path(hdfsWorkingPath + resource.relativePath);
		
		try {
			if (hdfs.exists(path)) {
				return hdfs.open(path);
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e); 
		}
		
		return null;
	}

	/**
	 * 判断原文件是否存在于hdfs上。
	 * 
	 * @param resource
	 *            资源对象。
	 * @return 是否存在。
	 */
	public boolean fileExists(@NotNull Resource resource) {
		Path path = new Path(hdfsWorkingPath + resource.relativePath);
		try {
			return hdfs.exists(path);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}
	
	// **************** 私有方法

	/**
	 * 私有的构造函数。
	 */
	private HdfsUtil() {
		try {
			hdfs = FileSystem.get(URI.create(
					Config.getNotNull(HDFS_SERVERS)),
					new Configuration());
		} catch (IOException e) {
			logger.fatal((e));
			throw new RuntimeException(e);
		}
	}

}
