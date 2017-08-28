package citexplore.content;

import citexplore.foundation.DistributedOperation;
import citexplore.foundation.LockFactory;
import citexplore.offlinedownload.Resource;
import com.sun.istack.internal.NotNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;

/**
 * Html内容提供者。
 *
 * @author Zhu, Sichuang
 * @author Zhang, Yin
 */
public class HtmlContentProvider implements ContentProvider {

    // **************** 公开变量

    // **************** 私有变量

    /**
     * 资源。
     */
    private Resource resource = null;

    /**
     * 内容文本。
     */
    private String content = "";

    /**
     * Log4j logger。
     */
    private static Logger logger = LogManager.getLogger(HtmlContentProvider
            .class);

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 保护的构造函数。
     *
     * @param resource 资源。
     */
    protected HtmlContentProvider(@NotNull Resource resource) {
        this.resource = resource;
    }

    /**
     * 获取内容文本。
     *
     * @return 内容文本。
     */
    @Override
    public String content() {
        if (!"".equals(content)) {
            return content;
        }

        if (HdfsUtil.instance.contentExists(resource)) {
            return HdfsUtil.instance.content(resource);
        }

        try {
            LockFactory.factory().produce("/htmlcontentprovider/" + Math.abs(
                    (short) resource.url.hashCode())).withLock(new DistributedOperation<Object>() {
                @Override
                public Object execute() {
                    if (!HdfsUtil.instance.contentExists(resource)) {
                        String fileData = HdfsUtil.instance.stringFile
                                (resource);
                        if ("".equals(fileData)) {
                            return null;
                        }

                        content = Jsoup.parse(fileData).text();
                        HdfsUtil.instance.write(resource, content);
                    } else {
                        content = HdfsUtil.instance.content(resource);
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

        return content;
    }

    // **************** 私有方法

}
