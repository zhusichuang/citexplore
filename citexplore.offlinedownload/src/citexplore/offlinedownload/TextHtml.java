package citexplore.offlinedownload;

/**
 * text/html mime类型。
 *
 * @author Zhang, Yin
 */
public class TextHtml extends FormalizedMime {

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
        return "text/html";
    }
    
    /**
     * 获得正规化mime文件扩展名。
     * @return 正规化mime文件扩展名。
     */
    @Override
    public String formalizedFileExtension() {
        return ".htm";
    }

    /**
     * 匹配mime类型。
     *
     * @param mime 待匹配的mime字符串。
     * @return 匹配的mime类型。
     */
    @Override
    public boolean match(String mime) {
        return matchMime(mime);
    }

    /**
     * 生成application/pdf mime类型对象。
     *
     * @param mime mime字符串。
     * @return application/pdf mime类型对象。
     */
    public static TextHtml produce(String mime) {
        return matchMime(mime) ? new TextHtml(mime) : null;
    }

    // **************** 公开方法

    // **************** 私有方法

    /**
     * 私有的构造函数。
     *
     * @param originalMime application/pdf mime类型构造函数。
     */
    private TextHtml(String originalMime) {
        super(originalMime);
    }

    /**
     * 匹配mime字符串
     * @param mime mime字符串。
     * @return mime字符串是否匹配。
     */
    private static boolean matchMime(String mime) {
        switch (mime) {
            case "application/vnd.sealedmedia.softseal.html":
            case "application/x-asap":
            case "application/x-asp":
            case "application/x-httpd-eperl":
            case "application/x-httpd-fcgi":
            case "application/x-httpd-php":
            case "application/x-httpd-php-source":
            case "application/x-httpd-php3-source":
            case "application/x-web-iPerl":
            case "audio/mod":
            case "audio/x-mod":
            case "text/asp":
            case "text/plain":
            case "wwwserver/html-ssi":
            case "text/html":
                return true;
            default:
                return false;
        }
    }

}
