package citexplore.thumbnail;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import com.sun.istack.internal.NotNull;

import citexplore.foundation.Config;
import citexplore.offlinedownload.Resource;
import citexplore.offlinedownload.ResourceStatus;
import citexplore.offlinedownload.ResourceStorage;

/**
 * 缩略图提供者。
 *
 * @author Zhu,Sichuang
 */
public class ThumbnailProvider {

	/**
	 * 图片路径额锁对象。
	 *
	 * @author Zhu,Sichuang
	 */
	public class PathLock {

		// **************** 公开变量

		/**
		 * 图片所在路径。
		 */
		public String relativePath = "";

		/**
		 * png生成锁。
		 */
		public final Object lock = new Object();

		// **************** 私有变量

		// **************** 继承方法

		// **************** 公开方法

		// **************** 私有方法

	}

	// **************** 公开变量

	/**
	 * 全局唯一的探索图提供者工厂。
	 */
	public static ThumbnailProvider instance = new ThumbnailProvider();

	/**
	 * 缩略图宽度配置项键。
	 */
	public static final String THUMBNAIL_WIDTH = "cx.tmb.thumbnailproviderfactory.thumbnailwidth";

	/**
	 * 缩略图高度配置项键。
	 */
	public static final String THUMBNAIL_HEIGHT = "cx.tmb.thumbnailproviderfactory.thumbnailheight";

	/**
	 * 缩略图工作路径。
	 */
	public static final String WORKING_PATH = "cx.tmb.thumbnailprovider.workingpath";

	// **************** 私有变量

	/**
	 * 图片路径哈希表。
	 */
	private HashMap<String, PathLock> pathMap = new HashMap<>();

	/**
	 * 图片路径哈希表锁。
	 */
	private final Object pathMapLock = new Object();

	/**
	 * 图片工作路径。
	 */
	private  String workingPath ="";

	/**
	 * 图片宽度。
	 */
	private  int thumbnailWidth = 105;

	/**
	 * 图片高度。
	 */
	private  int thumbnailHeight = 152;
	
	/**
	 * Log4j logger.
	 */
	private static Logger logger = LogManager.getLogger(ThumbnailProvider.class);

	// **************** 继承方法

	// **************** 公开方法

	/**
	 * 得到缩略图。
	 * 
	 * @param url
	 *            需要生成缩略图提供者对象的链接。
	 * @return 缩略图路径。
	 */
	public String thumbnail(String url) {
		PathLock pathLock;
		if ((pathLock = pathMap.get(url)) == null) {
			synchronized (pathMapLock) {
				if (!pathMap.containsKey(url)) {
					pathLock = new PathLock();
					pathMap.put(url, pathLock);
				}
			}
		}
		
		if ("".equals(pathLock.relativePath)) {
			synchronized (pathLock.lock) {
				if ("".equals(pathLock.relativePath)) {
					pathLock.relativePath = generate(url);
				}
			}
		}
		return pathLock.relativePath;
	}

	// **************** 私有方法

	/**
	 * 根据url生成缩略图。
	 * 
	 * @param url
	 *            需要生成缩略图提供者对象的链接。
	 * @return 缩略图路径。
	 */
	private String generate(String url) {
		Resource resource = ResourceStorage.instance.get(url);
		if(null == resource || ResourceStatus.finished != resource.status){
			logger.info("Can't find resource or resource has not downloaded completed!");
			return "";
		}
		
		int splitIndex = resource.relativePath.lastIndexOf("/");
		String dir = "";
		
		if (splitIndex > 0) {
			dir = resource.relativePath.substring(0, splitIndex);
		}
		
		
		new File(workingPath + dir).mkdirs();
		switch (resource.mime.toLowerCase()) {
		case "application/pdf":
			return pdfThumbnail(resource);
		case "text/html":
			HtmlThumbProvider htmlThumbProvider = new HtmlThumbProvider();
			return htmlThumbProvider.thumbnail(resource, thumbnailWidth,
					thumbnailHeight, workingPath);
		default:
			return "";
		}
	}

	/**
	 * pdf缩略图生成。
	 * 
	 * @param resource
	 *            资源对象。
	 * @return 缩略图路径。
	 */
	protected String pdfThumbnail(@NotNull Resource resource) {
		String pathName = workingPath + resource.relativePath + "." + "1."
				+ thumbnailWidth + "x" + thumbnailHeight + ".png";
		try {
			PDDocument document = PDDocument
					.load(HdfsUtil.instance.inputStream(resource));
			PDFRenderer pdfRenderer = new PDFRenderer(document);
			BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 300,
					ImageType.RGB);

			int width = bim.getWidth();
			int height = bim.getHeight();
			double ratio;
			if (1.0 * width / height >= 1.0 * thumbnailWidth
					/ thumbnailHeight) {
				ratio = 1.0 * thumbnailWidth / width;
			} else {
				ratio = 1.0 * thumbnailHeight / height;
			}

			height = (int) (ratio * height);
			width = (int) (ratio * width);
			BufferedImage bufferedImage = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_RGB);
			bufferedImage.getGraphics().drawImage(
					bim.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0,
					0, null);

			ImageIOUtil.writeImage(bufferedImage, pathName, 300);
			document.close();
			return pathName;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 私有构造函数。
	 */
	private ThumbnailProvider() {
		workingPath = Config.getFolder(WORKING_PATH);
		thumbnailWidth = Integer.parseInt(Config.getNotNull(THUMBNAIL_WIDTH));
		thumbnailHeight = Integer.parseInt(Config.getNotNull(THUMBNAIL_HEIGHT));
	}

}
