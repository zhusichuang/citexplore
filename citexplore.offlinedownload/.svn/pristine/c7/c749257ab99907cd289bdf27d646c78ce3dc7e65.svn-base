package citexplore.offlinedownload;

/**
 * 非正规化Mime类。
 *
 * @author Zhang, Yin
 */
public class NonFormalizedMime extends FormalizedMime {

    // **************** 公开变量

    // **************** 私有变量

    // **************** 继承方法

    /**
     * 获得正规化mime字符串。
     *
     * @return 正规化mime字符串。
     */
    @Override
    public String formalizedMime() {
        return "";
    }
    
    /**
     * 获得正规化mime文件扩展名。
     * @return 正规化mime文件扩展名。
     */
    @Override
    public String formalizedFileExtension() {
        return "";
    }

    /**
     * 匹配mime类型。
     *
     * @param mime 待匹配的mime字符串。
     * @return 匹配的mime类型。
     */
    @Override
    public boolean match(String mime) {
        return mime.toLowerCase().equals(originalMime);
    }


    // **************** 公开方法

    /**
     * 生成非正规化mime对象。
     *
     * @param mime mime字符串。
     * @return 非正规化mime对象。
     */
    public static NonFormalizedMime produce(String mime) {
        if (mime == null || "".equals(mime)) {
            return null;
        }

        return new NonFormalizedMime(mime);
    }

    // **************** 私有方法

    /**
     * 非正规化mime类构造函数。
     *
     * @param originalMime 原始mime字符串。
     */
    private NonFormalizedMime(String originalMime) {
        super(originalMime);
    }

}
