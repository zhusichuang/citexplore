package citexplore.offlinedownload.downloader;

import citexplore.foundation.Config;
import citexplore.offlinedownload.Resource;
import citexplore.offlinedownload.ResourceStatus;
import citexplore.offlinedownload.ResourceStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

/**
 * 离线下载工具类。
 *
 * @author Zhu, Sichuang
 * @author Zhang, Yin
 */
public class Downloader {

	// **************** 公开变量

	/**
	 * 离线下载工作路径配置项键。
	 */
	public static final String WORKING_PATH = "cx.ofd.downloader.workingpath";

	/**
	 * 全局唯一的离线下载工具对象。
	 */
	public static final Downloader instance = new Downloader();

	// **************** 私有变量

	/**
	 * 工作路径。
	 */
	private String workingPath = "";

	/**
	 * Log4j logger。
	 */
	private static Logger logger = LogManager.getLogger(Downloader.class);

	/**
	 * 下载对象hashMap。
	 */
	protected HashMap<String, DownloadJob> jobMap = new HashMap<>();

	/**
	 * 下载对象hashMap锁。
	 */
	private Object downloadingMapLock = new Object();

	// **************** 继承方法

	// **************** 公开方法

	/**
	 * 保护的构造函数。
	 */
	protected Downloader() {
		workingPath = Config.getFolder(WORKING_PATH);
	}

	/**
	 * 下载链接。*线程安全*
	 *
	 * @param url 需要被下载的链接。
	 */
	public void download(String url) {
		logger.info("Download requested for url: " + url);
		if (downloaded(url)) {
			return;

		} else if (jobMap.containsKey(url)) {
			logger.info("Already downloading " + url);
			return;
		} else {
			synchronized (downloadingMapLock) {
				if (!jobMap.containsKey(url)) {
					logger.info("Starting download job for url: " + url);
					DownloadJob downloadJob = new DownloadJob(url,
							workingPath, this);
					jobMap.put(url, downloadJob);
					downloadJob.start();
				} else {
					logger.info("Already downloading " + url);
					return;
				}
			}
		}
	}

	/**
	 * 判断链接是否已经是下载成功状态。
	 *
	 * @param url 链接。
	 * @return url是否已经是下载成功状态。
	 */
	public boolean downloaded(String url) {
		Resource resource;
		return null != (resource = ResourceStorage.instance.get(url)) &&
				ResourceStatus.finished == resource.status;
	}

	/**
	 * 获取链接的下载状态信息。
	 * @param url 链接。
	 * @return 链接的下载状态信息。
     */
	public JobStatus status(String url) {
		DownloadJob downloadJob;
		if (null != (downloadJob = jobMap.get(url))) {
			return downloadJob.status;
		} else {
			JobStatus jobStatus = new JobStatus();
			Resource resource;
			if (null != (resource = ResourceStorage.instance.get(url))) {
				jobStatus.stage = JobStage.intToType(resource.status.toInt());
			} else {
				jobStatus.stage = JobStage.unknown;
			}
			return jobStatus;
		}
	}

	/**
	 * 下载完成回调函数。
	 *
	 * @param url 链接。
	 */
	public void downloadCompleted(String url) {
		synchronized (downloadingMapLock) {
			jobMap.remove(url);
		}
	}

	// **************** 私有方法

}
