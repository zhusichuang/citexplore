package citexplore.offlinedownload;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正规化Mime类。
 *
 * @author Zhang, Yin
 */
public abstract class FormalizedMime {

    // **************** 公开变量

    // **************** 私有变量

    /**
     * 原始mime字符串。
     */
    protected final String originalMime;

    /**
     * Log4j logger。
     */
    private static Logger logger = LogManager.getLogger(FormalizedMime.class);

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 正规化mime类构造函数。
     *
     * @param originalMime 原始mime字符串。
     */
    protected FormalizedMime(String originalMime) {
        this.originalMime = originalMime.toLowerCase();
    }

    /**
     * 获得正规化mime字符串。
     *
     * @return 正规化mime字符串。
     */
    public abstract String formalizedMime();

    /**
     * 获得原始mime字符串。
     */
    public String originalMime() {
        return originalMime;
    }
    
    /**
     * 获得正规化mime文件扩展名。
     * @return 正规化mime文件扩展名。
     */
    public abstract String formalizedFileExtension();

    /**
     * 匹配mime类型。
     *
     * @param mime 待匹配的mime字符串。
     * @return 匹配的mime类型。
     */
    public abstract boolean match(String mime);

    /**
     * 依据mime字符串生成正规化mime对象。
     *
     * @param mime mime字符串。
     * @return 依据mime字符串生成的正规化mime对象。
     */
    public static FormalizedMime produce(String mime) {
        mime = extractMime(mime);

        Class<FormalizedMime>[] mimeClasses = new Class[]{ApplicationPdf
                .class, TextHtml.class, NonFormalizedMime.class, EmptyMime
                .class};

        FormalizedMime ret = null;

        for (Class<FormalizedMime> mimeClass : mimeClasses) {
            try {
                ret = (FormalizedMime) mimeClass.getMethod("produce", String
                        .class).invoke(null, mime);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }

            if (ret != null) {
                break;
            }
        }

        return ret;
    }

    /**
     * 从原始mime中匹配mime字符串。
     *
     * @param originalMime 原始mime。
     * @return 原始mime中匹配的mime字符串。
     */
    public static String extractMime(String originalMime) {
        if (originalMime == null) {
            return "";
        }

        Matcher matcher = Pattern.compile("[a-zA-Z0-9!#$%^&\\\\*_" +
                "\\\\-\\\\+{}\\\\|'" + "" + "" + "" + "" + "" + "" + "" + ""
                + ".`~]+/[a-zA-Z0-9!#$%^&\\\\*_\\\\-\\\\+{}\\\\|'.`~]+")
                .matcher(originalMime.toLowerCase());

        return matcher.find() ? matcher.group() : "";
    }

    // **************** 私有方法

}
