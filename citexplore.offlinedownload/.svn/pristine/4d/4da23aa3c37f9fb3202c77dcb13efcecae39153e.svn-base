package citexplore.offlinedownload.downloader;

import citexplore.offlinedownload.FormalizedMime;

/**
 * 资源认证管理对象。
 *
 * @author Zhu, Sichuang
 */
public abstract class ResourceVerifier {

    // **************** 公开变量

    public abstract boolean verify(String path);

    // **************** 私有变量

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 根据mime类型返回验证对象。
     *
     * @param mime mime类型。
     * @return 验证对象。
     */
    public static ResourceVerifier verifier(FormalizedMime mime) {
        switch (mime.formalizedMime()) {
            default:
                return new NoVerifier();
        }
    }

    // **************** 私有方法

}
