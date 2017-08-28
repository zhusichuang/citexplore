package citexplore.offlinedownload.downloader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.istack.internal.NotNull;

/**
 * 离线下载任务状态信息。
 *
 * @author Zhu, Sichuang
 */
public class JobStatus {

    // **************** 公开变量

    /**
     * 离线下载任务状态阶段枚举。
     */
    public JobStage stage = JobStage.readyToStart;

    /**
     * 当前进度。
     */
    public long progress = -1;

    /**
     * 总体进度.
     */
    public long totalProgress = -1;

    // **************** 私有变量

    /**
     * Log4j logger。
     */
    private static Logger logger = LogManager.getLogger(JobStatus.class);

    // **************** 继承方法

    // **************** 公开方法
    
    /**
     * 获得下载任务状态信息json。
     *
     * @param node 用于写入下载任务状态信息json的ObjectNode。
     * @return 是否写入了json。
     */
    public boolean json(ObjectNode node) {
        if (!JobStage.readyToStart.equals(stage)) {
            node.put("stage", stage.toInt());
        }
        if(totalProgress >= 0){
	    	node.put("progress", progress);
	    	node.put("totalProgress", totalProgress);
        }
        return true;
    }

    /**
     * 根据下载任务状态信息json填充资源信息对象。
     *
     * @param node 下载任务状态信息json。
     * @return 根据下载任务状态信息json填充得到的资源信息对象。
     */
    public static JobStatus formJson(@NotNull ObjectNode node) {
        JsonNode value = null;

        JobStatus status = new JobStatus();

        if ((value = node.get("stage")) != null) {
           status.stage = JobStage.intToType(value.asInt());
        }

        if ((value = node.get("progress")) != null) {
        	status.progress = value.asInt();
        }

        if ((value = node.get("totalProgress")) != null) {
        	status.totalProgress = value.asInt();
        }

        return status;
    }

    // **************** 私有方法

}
