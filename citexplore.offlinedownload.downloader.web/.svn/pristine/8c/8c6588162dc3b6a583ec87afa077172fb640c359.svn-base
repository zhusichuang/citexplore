package citexplore.offlinedownload.downloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import org.junit.Assert;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import citexplore.offlinedownload.manager.NodeStatus;
import net.javacrumbs.jsonunit.JsonAssert;

/**
 * heartbeat.jsp测试类。
 * 
 * @author Zhu, Sichuang
 */
public class testHeartbeatJsp {

    // **************** 公开变量

    // **************** 私有变量

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 发送错误参数测试心跳功能。
     */
    @Test
    public void testHeartWithRightParam() {
        String url = "http://localhost:8080/citexplore.offlinedownload.downloader.web/heartbeat.jsp";
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        String param = "command={\"command\":\"heartbeat\"}";
        try {
            URL realUrl = new URL(url);
            URLConnection conn = realUrl.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            out = new PrintWriter(conn.getOutputStream());
            out.print(param);
            out.flush();
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            in.close();
            out.close();
            Assert.assertEquals(NodeStatus.alive.toInt(),
                    new ObjectMapper().readTree(result).get("status").asInt());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 发送正确参数测试心跳功能。
     */
    @Test
    public void testHeartbeatWithWrongParam() {

        String url = "http://localhost:8080/citexplore.offlinedownload.downloader.web/heartbeat.jsp";
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        String param = "command={\"command\":\"hearadasd\"}";
        try {
            URL realUrl = new URL(url);
            URLConnection conn = realUrl.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            out = new PrintWriter(conn.getOutputStream());
            out.print(param);
            out.flush();
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }

            in.close();
            out.close();
            JsonAssert.assertJsonEquals(
                    new ObjectMapper().readTree(new File(getClass()
                            .getResource(
                                    "/json/TestDownloadWithWrongParam.json")
                            .getFile())),
                    new ObjectMapper().readTree(result));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // **************** 私有方法

}