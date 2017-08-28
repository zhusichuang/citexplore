package citexplore.offlinedownload.downloader;

import citexplore.foundation.util.NetUntilTest;
import citexplore.foundation.util.NetUtil;
import sun.nio.ch.Net;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Springer pdf url重定向测试类。
 *
 * @author Zhu, Sichuang
 */
public class CnkiPdfUrlRedirectorTest {

    // **************** 公开变量

    // **************** 私有变量

    /**
     * Log4j logger。
     */
    private static Logger logger = LogManager.getLogger(CnkiPdfUrlRedirectorTest.class);

    // **************** 继承方法

    // **************** 公开方法

    /**
     * 测试redirectImpl函数。
     */
    @Test
    public void testRedirectImpl() {

        	//new article view
        	CnkiPdfUrlRedirector cnkiPdfUrlRedirector = new CnkiPdfUrlRedirector();
            UrlRedirection urlRedirection = cnkiPdfUrlRedirector
					.redirectImpl("http://www.cnki.com.cn/Article/CJFDTotal-CSGH200704011.htm");
           
            String cookie="";
            String url1="";
            String result = "";  
            BufferedReader in = null;  
            try {  
                String urlName = "http://epub.cnki.net/kns/logindigital.aspx?ParentLocation=http://www.cnki.net";
                URL realUrl = new URL(urlName);  
                // 打开和URL之间的连接  
                URLConnection conn = realUrl.openConnection();  
                // 设置通用的请求属性 
                conn.setRequestProperty("accept", "*/*");  
                conn.setRequestProperty("Upgrade-Insecure-Requests", "1");  
                conn.setRequestProperty("connection", "Keep-Alive");  
                conn.setRequestProperty("user-agent",  
                        "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:50.0) Gecko/20100101 Firefox/50.0");  
                conn.setRequestProperty("cookie",  
                        "Ecp_ClientId=2161204215402331679");  
                // 建立实际的连接  
                conn.connect();  
                // 获取所有响应头字段  
                Map<String, List<String>> map = conn.getHeaderFields();  
//              Map<String, List<String>> map = conn.getHeaderFields();  
//              // 遍历所有的响应头字段  
             for (String key : map.keySet()) {  
                  System.out.println(key + "--->" + map.get(key));  
              }  
                // 遍历所有的响应头字段  
            //    cookie = map.get("Set-Cookie").toString();
                cookie = conn.getHeaderField("set-cookie");
                // 定义BufferedReader输入流来读取URL的响应  
                in = new BufferedReader(  
                        new InputStreamReader(conn.getInputStream()));  
                String line;  
                while ((line = in.readLine()) != null) {  
                    result += "/n" + line;  
                }  
          //      url1 = conn.getURL().toString();
            } catch (Exception e) {  
                System.out.println("发送GET请求出现异常！" + e);  
                e.printStackTrace();  
            }  
            // 使用finally块来关闭输入流  
            finally {  
                try {  
                    if (in != null) {  
                        in.close();  
                    }  
                } catch (IOException ex) {  
                    ex.printStackTrace();  
                }  
            }  
       //     cookie = cookie.substring(1, cookie.length()-1);
            logger.info(cookie);
            logger.info(result);
            Pattern pattern = Pattern.compile("<input type=[\"']hidden[\"'] name=[\"']__VIEWSTATE[\"'] id=[\"']__VIEWSTATE[\"'] value=[\"']([^\"']+)[\"']");
            Matcher matcher = pattern.matcher(result);
            String view="";
            if(matcher.find()){
            	view = matcher.group(1);
            }
            try {
				view = URLEncoder.encode(view,"utf-8");
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

          PrintWriter out = null;  
          in = null;  
          result = "";  
          try {  
              URL realUrl = new URL("http://epub.cnki.net/kns/logindigital.aspx?ParentLocation=http%3a%2f%2fwww.cnki.net");  
              // 打开和URL之间的连接  
              URLConnection conn = realUrl.openConnection();  
              // 设置通用的请求属性  
              conn.setRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");  
              conn.setRequestProperty("Host", "www.cnki.net");  
              conn.setRequestProperty("connection", "Keep-Alive");  
              conn.setRequestProperty("Upgrade-Insecure-Requests", "1");  
              conn.setRequestProperty("Accept-Encoding", "gzip, deflate");  
              conn.setRequestProperty("user-agent",  
                      "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:50.0) Gecko/20100101 Firefox/50.0");  
              conn.setRequestProperty("referer",  
              		url1);  
              conn.setRequestProperty("Accept-Language","zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");  
              conn.setRequestProperty("cookie",  
            		  "ASP.NET_SessionId=ds0wif1wu0rog43ktxlpatrw; Ecp_ClientId=2161204215402331679; LID=WEEvREcwSlJHSldRa1FhcEE0L01SRzYzSStKaW9UbnZSWlYyWnN2eE5XQT0=$9A4hF_YAuvQ5obgVAqNKPCYcEjKensW4ggI8Fm4gTkoUKaID8j8gFw!!");  
              ((HttpURLConnection)conn).setInstanceFollowRedirects(false);
              conn.setRequestProperty("Content-Type",  
              		"application/x-www-form-urlencoded");  
              //conn.setRequestProperty("cookie"," Ecp_ClientId=3161203203902364985; kc_cnki_net_uid=9e274a19-daa1-e336-3cb8-2eb68756d0e3; UserSeesKcms=%u539F%u7269%u8FD4%u8FD8%u8BF7%u6C42%u6743%3A%u7269%u4E0A%u8BF7%u6C42%u6743%u6291%u6216%u4FB5%u6743%u8D23%u4EFB%u65B9%u5F0F%21cjfq%21cjfd2014%21fxja201401009%7C; UserDownLoadsKcms=%u539F%u7269%u8FD4%u8FD8%u8BF7%u6C42%u6743%3A%u7269%u4E0A%u8BF7%u6C42%u6743%u6291%u6216%u4FB5%u6743%u8D23%u4EFB%u65B9%u5F0F%21cjfq%21cjfd2014%21fxja201401009%7C; ASP.NET_SessionId=oxjhnzj3ie2nll55khiqrgbv; SID_kcms=202119; LID=WEEvREcwSlJHSldRa1Fhb09jeVVYS2k5RWdUQW55OVNKd3h2OFFjSGh4WT0=$9A4hF_YAuvQ5obgVAqNKPCYcEjKensW4ggI8Fm4gTkoUKaID8j8gFw!!");
        //      conn.setRequestProperty("Content-Length","19");  
              // 发送POST请求必须设置如下两行  
              conn.setDoOutput(true);  
              conn.setDoInput(true);  
              // 获取URLConnection对象对应的输出流  
              out = new PrintWriter(conn.getOutputStream());  
              // 发送请求参数 
              String param = "__VIEWSTATE="+view+"&username=&password=&iplogin=";
              logger.info(param);


              out.print(param);  
              // flush输出流的缓冲  
              out.flush();  
              Map<String, List<String>> map = conn.getHeaderFields();  
           //   Map<String, List<String>> map = conn.getHeaderFields();  
              // 遍历所有的响应头字段  
              for (String key : map.keySet()) {  
                  System.out.println(key + "--->" + map.get(key));  
              }  
              // 遍历所有的响应头字段 
              cookie = map.get("Set-Cookie").toString();
              cookie = cookie.substring(1, cookie.length()-1);
              logger.info(cookie);
              // 定义BufferedReader输入流来读取URL的响应  
              in = new BufferedReader(  
                      new InputStreamReader(conn.getInputStream()));  
              String line;  
              while ((line = in.readLine()) != null) {  
                  result += "/n" + line;  
              }  
              logger.info(conn.getURL().toString());
          } catch (Exception e) {  
              System.out.println("发送POST请求出现异常！" + e);  
              e.printStackTrace();  
          }  
          // 使用finally块来关闭输出流、输入流  
          finally {  
              try {  
                  if (out != null) {  
                      out.close();  
                  }  
                  if (in != null) {  
                      in.close();  
                  }  
              } catch (IOException ex) {  
                  ex.printStackTrace();  
              }  
          }  
          
          
          
          
          
          

        in = null;  
        result = "";   
            try {  
                String urlName = urlRedirection.redirectedUrl;
                URL realUrl = new URL(urlName);  
                // 打开和URL之间的连接  
                URLConnection conn = realUrl.openConnection();  
                // 设置通用的请求属性  
                conn.setRequestProperty("accept", "*/*");  
                conn.setRequestProperty("connection", "Keep-Alive");  
                conn.setRequestProperty("user-agent",  
                        "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");  
                conn.setRequestProperty("referer",  
                		"http://www.cnki.com.cn/Article/CJFDTotal-CSGH200704011.htm");  
                conn.setRequestProperty("cookie",  
                		cookie);  
                // 建立实际的连接  
                conn.connect();  
                // 获取所有响应头字段  
                Map<String, List<String>> map = conn.getHeaderFields();  
                // 遍历所有的响应头字段  
                for (String key : map.keySet()) {  
                    System.out.println(key + "--->" + map.get(key));  
                }  
                // 定义BufferedReader输入流来读取URL的响应  
                in = new BufferedReader(  
                        new InputStreamReader(conn.getInputStream()));  
                String line;  
                while ((line = in.readLine()) != null) {  
                    result += "/n" + line;  
                }  
                url1 = conn.getURL().toString();
            } catch (Exception e) {  
                System.out.println("发送GET请求出现异常！" + e);  
                e.printStackTrace();  
            }  
            // 使用finally块来关闭输入流  
            finally {  
                try {  
                    if (in != null) {  
                        in.close();  
                    }  
                } catch (IOException ex) {  
                    ex.printStackTrace();  
                }  
            }  
            logger.info(result);
//            
//            logger.info(url1);
//            PrintWriter out = null;  
//            in = null;  
//            result = "";  
//            try {  
//                URL realUrl = new URL(url1);  
//                // 打开和URL之间的连接  
//                URLConnection conn = realUrl.openConnection();  
//                // 设置通用的请求属性  
//                conn.setRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");  
//                conn.setRequestProperty("Host", "www.cnki.net");  
//                conn.setRequestProperty("connection", "Keep-Alive");  
//                conn.setRequestProperty("Upgrade-Insecure-Requests", "1");  
//                conn.setRequestProperty("Accept-Encoding", "gzip, deflate");  
//                conn.setRequestProperty("user-agent",  
//                        "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:50.0) Gecko/20100101 Firefox/50.0");  
//                conn.setRequestProperty("referer",  
//                		url1);  
//                conn.setRequestProperty("Accept-Language","zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");  
//                
//                conn.setRequestProperty("Content-Type",  
//                		"application/x-www-form-urlencoded");  
//                conn.setRequestProperty("cookie"," Ecp_ClientId=3161203203902364985; kc_cnki_net_uid=9e274a19-daa1-e336-3cb8-2eb68756d0e3; UserSeesKcms=%u539F%u7269%u8FD4%u8FD8%u8BF7%u6C42%u6743%3A%u7269%u4E0A%u8BF7%u6C42%u6743%u6291%u6216%u4FB5%u6743%u8D23%u4EFB%u65B9%u5F0F%21cjfq%21cjfd2014%21fxja201401009%7C; UserDownLoadsKcms=%u539F%u7269%u8FD4%u8FD8%u8BF7%u6C42%u6743%3A%u7269%u4E0A%u8BF7%u6C42%u6743%u6291%u6216%u4FB5%u6743%u8D23%u4EFB%u65B9%u5F0F%21cjfq%21cjfd2014%21fxja201401009%7C; ASP.NET_SessionId=oxjhnzj3ie2nll55khiqrgbv; SID_kcms=202119; LID=WEEvREcwSlJHSldRa1Fhb09jeVVYS2k5RWdUQW55OVNKd3h2OFFjSGh4WT0=$9A4hF_YAuvQ5obgVAqNKPCYcEjKensW4ggI8Fm4gTkoUKaID8j8gFw!!");
//                conn.setRequestProperty("Content-Length","19");  
//                // 发送POST请求必须设置如下两行  
//                conn.setDoOutput(true);  
//                conn.setDoInput(true);  
//                // 获取URLConnection对象对应的输出流  
//                out = new PrintWriter(conn.getOutputStream());  
//                // 发送请求参数 
//                String param = "username=&password=";
//
//
//
//                out.print(param);  
//                // flush输出流的缓冲  
//                out.flush();  
//                
//                Map<String, List<String>> map = conn.getHeaderFields();  
//                // 遍历所有的响应头字段  
//                for (String key : map.keySet()) {  
//                    System.out.println(key + "--->" + map.get(key));  
//                }  
//                // 定义BufferedReader输入流来读取URL的响应  
//                in = new BufferedReader(  
//                        new InputStreamReader(conn.getInputStream()));  
//                String line;  
//                while ((line = in.readLine()) != null) {  
//                    result += "/n" + line;  
//                }  
//                logger.info(conn.getURL().toString());
//            } catch (Exception e) {  
//                System.out.println("发送POST请求出现异常！" + e);  
//                e.printStackTrace();  
//            }  
//            // 使用finally块来关闭输出流、输入流  
//            finally {  
//                try {  
//                    if (out != null) {  
//                        out.close();  
//                    }  
//                    if (in != null) {  
//                        in.close();  
//                    }  
//                } catch (IOException ex) {  
//                    ex.printStackTrace();  
//                }  
//            }  
//            logger.info(result);
           

    }
    // **************** 私有方法

}
