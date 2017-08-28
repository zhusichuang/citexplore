package citexplore.offlinedownload.manager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 离线下载管理器模块启动类。
 *
 * @author Zhang, Yin
 */
public class MainClass {

    // **************** 公开变量

    // **************** 私有变量

    /**
     * Log4j logger。
     */
    private static Logger logger = LogManager.getLogger(MainClass.class);

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 主函数。
     */
    public static void main(String[] args) {
        Manager instance = Manager.instance;
        instance.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                logger.info("Closing Manager...");
                Manager.instance.close();
            }
        });
    }

    // **************** 私有方法

}
