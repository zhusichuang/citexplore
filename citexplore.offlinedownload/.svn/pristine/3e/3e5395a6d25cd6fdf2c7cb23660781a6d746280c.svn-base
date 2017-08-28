package citexplore.offlinedownload.downloader;

import citexplore.foundation.Config;
import citexplore.foundation.DistributedOperation;
import citexplore.foundation.DistributedOperationException;
import citexplore.foundation.LockFactory;
import citexplore.foundation.util.NetUtil;
import citexplore.offlinedownload.FormalizedMime;
import citexplore.offlinedownload.Resource;
import citexplore.offlinedownload.ResourceStatus;
import citexplore.offlinedownload.ResourceStorage;
import com.sun.istack.internal.NotNull;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.net.MimeMessageBuilder;

import java.io.*;
import java.net.URI;
import java.net.HttpURLConnection;
import java.net.URLConnection;

/**
 * 离线下载任务。
 *
 * @author Zhu, Sichuang
 * @author Zhang, Yin
 */
public class DownloadJob {

    // **************** 公开变量

    /**
     * 离线下载任务状态信息。
     */
    public JobStatus status = new JobStatus();

    /**
     * Hdfs工作目录。
     */
    public static final String hdfsWorkingPath =
            "/opt/citexplore/offlinedownload/";

    /**
     * hdfs文件访问类。
     */
    protected static final FileSystem hdfs;

    /**
     * HDFS服务器配置项键。
     */
    public static final String HDFS_SERVERS = "cx.ofd.downloadjob.hdfsservers";

    // **************** 私有变量

    /**
     * 需要下载的资源url。
     */
    private String url = "";

    /**
     * 本地工作目录。
     */
    private String workingPath = "";

    /**
     * 下载线程。
     */
    private Thread thread = null;

    /**
     * Log4j logger。
     */
    private static Logger logger = LogManager.getLogger(DownloadJob.class);

    /**
     * 下载失败标志。
     */
    private boolean downloadFailed = true;

    /**
     * 离线下载对象回调。
     */
    private Downloader callback;

    /**
     * 离线下载任务构造函数。
     *
     * @param url         要下载的文件url。
     * @param workingPath 本地工作目录。
     * @param callback    离线下载对象回调。
     */
    public DownloadJob(@NotNull String url, @NotNull String workingPath,
                       @NotNull Downloader callback) {
        this.url = url;
        this.workingPath = workingPath;
        this.callback = callback;
    }

    /**
     * 开始下载。*异步方法*
     */
    public void start() {
        thread = new Thread(() -> {
            download();
        });
        thread.start();
    }

    // **************** 继承方法

    // **************** 公开方法

    // **************** 私有方法

    /**
     * 执行下载。
     */
    protected void download() {
        logger.info("Trying to download " + url);
        Resource resource = ResourceStorage.instance.get(url);

        if (resource == null) {
            logger.error("Resource not found: " + url);
            return;
        }
        if (resource.status.equals(ResourceStatus.finished)) {
            logger.info("Already downloaded: " + url);
            status.stage = JobStage.finished;
            JobFinishedInformer.instance.inform(url, status.stage == JobStage
                    .finished);
            return;
        }

        String redirecedtUrl = PdfUrlRedirector.produce(url).redirect(url);

        String filePath = workingPath + resource.relativePath;
        File fileFolder = new File(filePath.substring(0, filePath.lastIndexOf
                ("/")));
        if (!fileFolder.exists()) {
            fileFolder.mkdirs();
        }

        URLConnection connection;
        for (int retry = 3; retry > 0; retry--) {
            status.stage = JobStage.testingUrl;
            try {
                connection = NetUtil.urlConnection(redirecedtUrl);
                if (((HttpURLConnection) connection).getResponseCode() == 301) {
                    connection = NetUtil.urlConnection(connection
                            .getHeaderField("Location").replace("[", "")
                            .replace("]", ""));
                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.info(e.getMessage(), e);
                continue;
            }

            String contentType = connection.getContentType();
            if (null == contentType || "".equals(contentType)) {
                logger.info("Null or empty mime for " + url);
                break;
            } else if ("".equals(resource.mime.originalMime())) {
                resource.mime = FormalizedMime.produce(contentType);
            } else if (!resource.mime.match(FormalizedMime.extractMime
                    (contentType))) {
                logger.info("Mime mismatch for " + url + ", expecting " + (""
                        .equals(resource.mime.formalizedMime()) ? resource
                        .mime.originalMime() : resource.mime.formalizedMime()
                ) + " but was " + contentType);
                break;
            }

            ResourceVerifier verifier = ResourceVerifier.verifier(resource
                    .mime);
            if (null == verifier) {
                continue;
            }
            status.stage = JobStage.downloading;
            status.progress = 0;
            status.totalProgress = connection.getContentLengthLong();
            OutputStream os = null;
            InputStream is = null;

            try {
                os = new FileOutputStream(filePath);
                is = connection.getInputStream();
                byte[] bytes = new byte[1024];
                int len;
                while ((len = is.read(bytes)) != -1) {
                    os.write(bytes, 0, len);
                    status.progress = status.progress + len;
                }
                is.close();
                os.close();
            } catch (Exception e) {
                logger.info("Error downloading url " + url + ": " + e
                        .getMessage(), e);
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception e2) {
                    }
                }

                if (os != null) {
                    try {
                        os.close();
                    } catch (Exception e2) {
                    }
                }
                new File(filePath).delete();
                continue;
            }

            if (new File(filePath).exists()) {
                status.stage = JobStage.verifying;
                if (!verifier.verify(filePath)) {
                    new File(filePath).delete();
                } else {
                    downloadFailed = false;
                    break;
                }
            }
        }

        if (ResourceStatus.finished != ResourceStorage.instance.get(url)
                .status) {
            try {
                LockFactory.factory().produce("/downloadjob/" + Math.abs(
                        (short) url.hashCode())).withLock(new DistributedOperation<Object>() {
                    @Override
                    public Object execute() throws
                            DistributedOperationException {
                        if (ResourceStorage.instance.get(url).status !=
                                ResourceStatus.finished) {
                            if (downloadFailed) {
                                resource.status = ResourceStatus.downloadFailed;
                                status.stage = JobStage.downloadFailed;
                            } else {
                                logger.info("Trying to upload url " + url);
                                status.stage = JobStage.almostDone;

                                boolean succeeded = false;
                                try {
                                    DownloadJob.hdfs.copyFromLocalFile(new
                                            Path(filePath), new Path
                                            (DownloadJob.hdfsWorkingPath +
                                                    resource.relativePath));
                                    succeeded = true;
                                } catch (Exception e) {
                                    logger.error(e);
                                }

                                if (succeeded) {
                                    resource.status = ResourceStatus.finished;
                                    status.stage = JobStage.finished;
                                } else {
                                    resource.status = ResourceStatus
                                            .downloadFailed;
                                    status.stage = JobStage.downloadFailed;
                                }
                            }
                            ResourceStorage.instance.put(resource);
                            callback.downloadCompleted(url);
                        } else {
                            logger.info("Already downloaded: " + url);
                            status.stage = JobStage.finished;
                        }
                        return null;
                    }
                });
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }

            new File(filePath).delete();
        } else {
            logger.info("Already downloaded: " + url);
            status.stage = JobStage.finished;
        }

        JobFinishedInformer.instance.inform(url, status.stage == JobStage
                .finished);

    }

    /**
     * 静态代码块。
     */
    static {
        try {
            hdfs = FileSystem.get(URI.create(Config.getNotNull(HDFS_SERVERS))
                    , new Configuration());
        } catch (IOException e) {
            logger.fatal((e));
            throw new RuntimeException(e);
        }
    }

}
