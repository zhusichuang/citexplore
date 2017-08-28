package citexplore.content;

import citexplore.foundation.Config;
import citexplore.offlinedownload.Resource;
import citexplore.offlinedownload.downloader.DownloadJob;
import com.sun.istack.internal.NotNull;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;

/**
 * Hdfs访问工具。
 *
 * @author Zhu, Sichuang
 * @author Zhang, Yin
 */
public class HdfsUtil {

	// **************** 公开变量

	/**
	 * 全局唯一Hdfs访问工具。
	 */
	public static final HdfsUtil instance = new HdfsUtil();

	/**
	 * Hdfs访问对象。
	 */
	protected FileSystem hdfs = null;

	/**
	 * 工作目录。
	 */
	public static final String hdfsWorkingPath = "/opt/citexplore/content/";

	/**
	 * Hdfs服务器配置项键。
	 */
	public static final String HDFS_SERVERS = "cx.cnt.hdfsutil.hdfsservers";

	// **************** 私有变量

	/**
	 * Log4j logger。
	 */
	private static Logger logger = LogManager.getLogger(HdfsUtil.class);

	// **************** 继承方法

	// **************** 公开方法

	/**
	 * 将提取的资源正文内容写入hdfs。
	 *
	 * @param resource
	 *            资源。
	 * @param content
	 *            提取的资源正文内容。
	 */
	public void write(@NotNull Resource resource, String content) {
		logger.info("Writing content to hdfs: " + resource.url);

		FSDataOutputStream out = null;
		Path path = new Path(hdfsWorkingPath + resource.relativePath);
		BufferedWriter bufferedWriter = null;
		try {
			out = hdfs.create(path);
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(out));
			bufferedWriter.write(content);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		} finally {
			if (null != bufferedWriter) {
				try {
					bufferedWriter.close();
				} catch (IOException e) {
				}
			}
			if (null != out) {
				try {
					bufferedWriter.close();
					out.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * 判断提取的资源正文内容是否已经存在于hdfs。
	 *
	 * @param resource
	 *            资源。
	 * @return 提取的资源正文内容是否已经存在于hdfs。
	 */
	public boolean contentExists(@NotNull Resource resource) {
		return exists(resource, hdfsWorkingPath);
	}

	/**
	 * 判断资源原始文件是否存在于hdfs上。
	 *
	 * @param resource
	 *            资源。
	 * @return 资源原始文件是否存在于hdfs上。
	 */
	public boolean fileExists(@NotNull Resource resource) {
		return exists(resource, DownloadJob.hdfsWorkingPath);
	}

	/**
	 * 从hdfs上获取提取的资源正文内容。
	 *
	 * @param resource
	 *            资源。
	 * @return 提取的资源正文内容。
	 */
	public String content(@NotNull Resource resource) {
		logger.info("Reading content from hdfs: " + resource.url);
		return read(resource, hdfsWorkingPath);
	}

	/**
	 * 得到资源原始文件内容。
	 *
	 * @param resource
	 *            资源。
	 * @return 资源原始文件内容。
	 */
	public String stringFile(@NotNull Resource resource) {
		logger.info("Reading string file from hdfs: " + resource.url);
		return read(resource, DownloadJob.hdfsWorkingPath);
	}

	// **************** 私有方法

	/**
	 * 私有的构造函数。
	 */
	private HdfsUtil() {
		try {
			hdfs = FileSystem.get(URI.create(Config.getNotNull(HDFS_SERVERS)),
					new Configuration());
		} catch (IOException e) {
			logger.fatal((e));
			throw new RuntimeException(e);
		}
	}

	/**
	 * 判断hdfs上是否存在某个文件。
	 *
	 * @param resource
	 *            资源。
	 * @param workingPath
	 *            工作目录。
	 * @return 文件是否存在。
	 */
	private boolean exists(@NotNull Resource resource, String workingPath) {
		Path path = new Path(workingPath + resource.relativePath);
		try {
			return hdfs.exists(path);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 从hdfs读取文件内容。
	 *
	 * @param resource
	 *            资源。
	 * @param workingPath
	 *            工作目录。
	 * @return 文件内容。
	 */
	private String read(@NotNull Resource resource, String workingPath) {
		String content = "";

		if (exists(resource, workingPath)) {
			Path path = new Path(workingPath + resource.relativePath);
			FSDataInputStream in = null;
			BufferedReader bufferedReader = null;
			try {
				in = hdfs.open(path);
				bufferedReader = new BufferedReader(new InputStreamReader(in));
				String temp;
				while ((temp = bufferedReader.readLine()) != null) {
					content += temp;
				}
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				throw new RuntimeException(e);
			} finally {
				if (null != bufferedReader) {
					try {
						bufferedReader.close();
					} catch (IOException e) {
					}
				}
				if (null != in) {
					try {
						in.close();
					} catch (IOException e) {
					}
				}
			}
		}

		return content;
	}

}
