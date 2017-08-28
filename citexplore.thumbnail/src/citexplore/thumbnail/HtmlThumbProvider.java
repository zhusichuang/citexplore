package citexplore.thumbnail;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import com.sun.istack.internal.NotNull;
import chrriis.dj.nativeswing.swtimpl.NativeComponent;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserEvent;
import citexplore.offlinedownload.Resource;

/**
 * html缩略图生成类。
 *
 * @author Zhu,Sichuang
 */
public class HtmlThumbProvider {

	// **************** 公开变量

	/**
	 * 行分隔符
	 */
	public final static String LS = System.getProperty("line.separator", "\n");

	/**
	 * 文件分割符
	 */
	public final static String FS = System.getProperty("file.separator", "\\");

	// **************** 私有变量

	/**
	 * 缩略图路径
	 */
	private String thumbnailPath = "";

	/**
	 * 生成缩略图完成标志位。
	 */
	private boolean executeComplete = false;

	/**
	 * JS执行串、
	 */
	private final static StringBuffer jsDimension;

	/**
	 * Log4j logger。
	 */
	private static Logger logger = LogManager
			.getLogger(HtmlThumbProvider.class);

	/**
	 * 生成缩略图锁。
	 */
	private Object generateLock = new Object();

	// **************** 继承方法

	// **************** 公开方法

	/**
	 * 生成缩略图。
	 * 
	 * @param resource
	 *            资源对象。
	 * @param width
	 *            缩略图的宽度。
	 * @param height
	 *            缩略图的高度。
	 * @param workingPath
	 *            工作路径。
	 * @return 缩略图存储路径。
	 */
	public String thumbnail(@NotNull final Resource resource,
			@NotNull int width, @NotNull int height,
			@NotNull String workingPath) {

		if (!"".equals(thumbnailPath) && true == executeComplete) {
			return thumbnailPath;
		}

		executeComplete = false;

		try {
			Runnable runnable = new Runnable() {
				public void run() {
					generate(resource, width, height, workingPath);
				}
			};
			SwingUtilities.invokeAndWait(runnable);

			while (!executeComplete) {
				Thread.sleep(100);
			}
			return thumbnailPath;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	// **************** 私有方法

	/**
	 * 生成缩略图。
	 * 
	 * @param resource
	 *            资源对象。
	 * @param width
	 *            缩略图的宽度。
	 * @param height
	 *            缩略图的高度。
	 * @param workingPath
	 *            工作路径。
	 * @return 缩略图存储路径。
	 */
	private void generate(@NotNull final Resource resource, @NotNull int width,
			@NotNull int height, @NotNull String workingPath) {

		JFrame frame = new JFrame("网页缩略图");
		JPanel jPanel = new JPanel(new BorderLayout());
		frame.setSize(1280, 2560);
		frame.setVisible(true);
		frame.add(jPanel, BorderLayout.CENTER);
		JPanel webBrowserPanel = new JPanel(new BorderLayout());
		String pathName = workingPath + resource.relativePath + "." + "1."
				+ width + "x" + height + ".png";
		final JWebBrowser webBrowser = new JWebBrowser();
		webBrowser.setBarsVisible(false);
		webBrowser.navigate(resource.url);
		webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
		jPanel.add(webBrowserPanel, BorderLayout.CENTER);

		webBrowser.addWebBrowserListener(new WebBrowserAdapter() {
			public void loadingProgressChanged(WebBrowserEvent e) {

				if (true == executeComplete) {
					logger.info("Thumbnail has been generated!");
					return;
				}
				if (e.getWebBrowser().getLoadingProgress() == 100) {
					synchronized (generateLock) {
						if (true == executeComplete) {
							logger.info("Thumbnail has been generated!");
							return;
						}
						String result = (String) webBrowser
								.executeJavascriptWithResult(
										jsDimension.toString());
						if( null == result){
							return;
						}
						int index = result.indexOf(":");
						NativeComponent nativeComponent = webBrowser
								.getNativeComponent();
						Dimension originalSize = nativeComponent.getSize();
						Dimension imageSize = new Dimension(
								Integer.parseInt(result.substring(0, index)),
								Integer.parseInt(result.substring(index + 1)));
						imageSize.width = Math.max(originalSize.width,
								imageSize.width + 50);
						imageSize.height = Math.max(originalSize.height,
								imageSize.height + 50);
						nativeComponent.setSize(imageSize);
						BufferedImage image = new BufferedImage(imageSize.width,
								imageSize.height, BufferedImage.TYPE_INT_RGB);
						nativeComponent.paintComponent(image);

						double ratio = 1.0 * imageSize.width / width;
						int ratioHegith = (int) (ratio * height);

						if (ratioHegith < imageSize.height) {
							image = image.getSubimage(0, 0, imageSize.width,
									ratioHegith);
						}

						BufferedImage bim = new BufferedImage(width, height,
								BufferedImage.TYPE_INT_RGB);

						bim.getGraphics()
								.drawImage(
										image.getScaledInstance(width, height,
												Image.SCALE_SMOOTH),
										0, 0, null);

						try {
							ImageIOUtil.writeImage(bim, pathName, 300);
							thumbnailPath = pathName;
						} catch (IOException e1) {
							logger.error(e1.getMessage(), e1);
							throw new RuntimeException(e1);
						}
						frame.dispose();
						executeComplete = true;
					}
				}
			}
		});
	}

	/**
	 * 静态代码块。
	 */
	static {
		jsDimension = new StringBuffer();
		jsDimension.append("var width = 0;").append(LS);
		jsDimension.append("var height = 0;").append(LS);
		jsDimension.append("if(document.documentElement) {").append(LS);
		jsDimension
				.append("  width = Math.max(width, document.documentElement.scrollWidth);")
				.append(LS);
		jsDimension
				.append("  height = Math.max(height, document.documentElement.scrollHeight);")
				.append(LS);
		jsDimension.append("}").append(LS);
		jsDimension.append("if(self.innerWidth) {").append(LS);
		jsDimension.append("  width = Math.max(width, self.innerWidth);")
				.append(LS);
		jsDimension.append("  height = Math.max(height, self.innerHeight);")
				.append(LS);
		jsDimension.append("}").append(LS);
		jsDimension.append("if(document.body.scrollWidth) {").append(LS);
		jsDimension
				.append("  width = Math.max(width, document.body.scrollWidth);")
				.append(LS);
		jsDimension
				.append("  height = Math.max(height, document.body.scrollHeight);")
				.append(LS);
		jsDimension.append("}").append(LS);
		jsDimension.append("return width + ':' + height;");
		new Thread() {
			@Override
			public void run() {
				NativeInterface.open();
				NativeInterface.runEventPump();
			}
		}.start();
	}

}
