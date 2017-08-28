package citexplore.offlinedownload;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.istack.internal.NotNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Timestamp;

/**
 * 资源信息。
 *
 * @author Zhu, Sichuang
 */
public class Resource {

    // **************** 公开变量

    /**
     * 资源url,作为资源唯一标识。
     */
    public final String url;

    /**
     * 资源状态时间。
     */
    public Timestamp time = new Timestamp(0);

    /**
     * 资源mime类型。
     */
    public FormalizedMime mime = EmptyMime.produce("");

    /**
     * 资源相对路径。
     */
    public String relativePath = "";

    /**
     * 资源状态。
     */
    public ResourceStatus status = ResourceStatus.unknown;


    // **************** 私有变量

    /**
     * Log4j logger。
     */
    private static Logger logger = LogManager.getLogger(Resource.class);

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 资源信息构造函数。
     *
     * @param url 资源唯一标识。
     */
    public Resource(@NotNull String url) {
        this.url = url;
    }

    /**
     * 获得资源信息json。
     *
     * @param node 用于写入资源信息json的ObjectNode。
     * @return 是否写入了json。
     */
    public boolean json(ObjectNode node) {
        if ("".equals(url)) {
            return false;
        }
        node.put("url", url);

        if (time.getTime() != 0) {
            node.put("time", time.getTime());
        }

        if (!"".equals(mime.formalizedMime())) {
            node.put("mime", mime.formalizedMime());
        } else {
            node.put("mime", mime.originalMime());
        }

        if (!"".equals(relativePath)) {
            node.put("relativePath", relativePath);
        }

        if (!ResourceStatus.unknown.equals(status)) {
            node.put("status", status.toInt());
        }
        return true;
    }

    /**
     * 根据资源信息json填充资源信息对象。
     *
     * @param node 资源信息json。
     * @return 是否填充了对象。
     */
    public static Resource formJson(@NotNull ObjectNode node) {
        JsonNode value = null;
        if ((value = node.get("url")) == null) {
            return null;
        }

        Resource resource = new Resource(value.asText());

        if ((value = node.get("time")) != null) {
            resource.time = new Timestamp(value.asLong());
        }

        resource.mime = FormalizedMime.produce((value = node.get("mime")) ==
                null ? null : value.asText());

        if ((value = node.get("relativePath")) != null) {
            resource.relativePath = value.asText();
        }

        if ((value = node.get("status")) != null) {
            resource.status = ResourceStatus.intToType(value.asInt());
        }

        return resource;
    }

    // **************** 私有方法

}
