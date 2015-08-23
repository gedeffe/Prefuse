package prefuse.render;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javafx.scene.image.Image;
import prefuse.data.Tuple;
import prefuse.util.io.IOLib;

/**
 * <p>
 * Utility class that manages loading and storing images. Includes a
 * configurable LRU cache for managing loaded images. Also supports optional
 * image scaling of loaded images to cut down on memory and visualization
 * operation costs.
 * </p>
 *
 * <p>
 * By default images are loaded upon first request. Use the
 * {@link #preloadImages(Iterator, String)} method to load images before they
 * are requested for rendering.
 * </p>
 *
 * @author alan newberger
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class ImageFactory {

	protected int m_imageCacheSize = 3000;
	protected int m_maxImageWidth = 100;
	protected int m_maxImageHeight = 100;
	protected boolean m_asynch = true;

	// a nice LRU cache courtesy of java 1.4
	protected Map<String, Image> imageCache = new LinkedHashMap<String, Image>(
			(int) (this.m_imageCacheSize + (1 / .75F)), .75F, true) {
		@Override
		public boolean removeEldestEntry(final Map.Entry<String, Image> eldest) {
			return this.size() > ImageFactory.this.m_imageCacheSize;
		}
	};
	protected Map<String, LoadMapEntry> loadMap = new HashMap<String, LoadMapEntry>(50);

	protected int nextTrackerID = 0;

	/**
	 * Create a new ImageFactory. Assumes no scaling of loaded images.
	 */
	public ImageFactory() {
		this(-1, -1);
	}

	/**
	 * Create a new ImageFactory. This instance will scale loaded images if they
	 * exceed the threshold arguments.
	 *
	 * @param maxImageWidth
	 *            the maximum width of input images (-1 means no limit)
	 * @param maxImageHeight
	 *            the maximum height of input images (-1 means no limit)
	 */
	public ImageFactory(final int maxImageWidth, final int maxImageHeight) {
		this.setMaxImageDimensions(maxImageWidth, maxImageHeight);
	}

	/**
	 * Indicates if this ImageFactory loads images asynchronously (true by
	 * default)
	 *
	 * @return true for asynchronous (background) loading, false for synchronous
	 *         (blocking) loading
	 */
	public boolean isAsynchronous() {
		return this.m_asynch;
	}

	/**
	 * Sets if this ImageFactory loads images asynchronously.
	 *
	 * @param b
	 *            true for asynchronous (background) loading, false for
	 *            synchronous (blocking) loading
	 */
	public void setAsynchronous(final boolean b) {
		this.m_asynch = b;
	}

	/**
	 * Sets the maximum image dimensions of loaded images, images larger than
	 * these limits will be scaled to fit within bounds.
	 *
	 * @param width
	 *            the maximum width of input images (-1 means no limit)
	 * @param height
	 *            the maximum height of input images (-1 means no limit)
	 */
	public void setMaxImageDimensions(final int width, final int height) {
		this.m_maxImageWidth = width;
		this.m_maxImageHeight = height;
	}

	/**
	 * Sets the capacity of this factory's image cache
	 *
	 * @param size
	 *            the new size of the image cache
	 */
	public void setImageCacheSize(final int size) {
		this.m_imageCacheSize = size;
	}

	/**
	 * Indicates if the given string location corresponds to an image currently
	 * stored in this ImageFactory's cache.
	 *
	 * @param imageLocation
	 *            the image location string
	 * @return true if the location is a key for a currently cached image, false
	 *         otherwise.
	 */
	public boolean isInCache(final String imageLocation) {
		return this.imageCache.containsKey(imageLocation);
	}

	/**
	 * <p>
	 * Get the image associated with the given location string. If the image has
	 * already been loaded, it simply will return the image, otherwise it will
	 * load it from the specified location.
	 * </p>
	 *
	 * <p>
	 * The imageLocation argument must be a valid resource string pointing to
	 * either (a) a valid URL, (b) a file on the classpath, or (c) a file on the
	 * local filesystem. The location will be resolved in that order.
	 * </p>
	 *
	 * @param imageLocation
	 *            the image location as a resource string.
	 * @return the corresponding image, if available
	 */
	public Image getImage(final String imageLocation) {
		Image image = this.imageCache.get(imageLocation);
		if ((image == null) && !this.loadMap.containsKey(imageLocation)) {
			final URL imageURL = IOLib.urlFromString(imageLocation);
			if (imageURL == null) {
				System.err.println("Null image: " + imageLocation);
				return null;
			}

			// if set for synchronous mode, block for image to load.
			if (!this.m_asynch) {
				image = new Image(imageURL.toString());
				this.waitForImage(image);
				this.addImage(imageLocation, image);
			} else {
				image = new Image(imageURL.toString(), this.m_asynch);
				final int id = ++this.nextTrackerID;
				this.loadMap.put(imageLocation, new LoadMapEntry(id, image));
			}
		} else if ((image == null) && this.loadMap.containsKey(imageLocation)) {
			final LoadMapEntry entry = this.loadMap.get(imageLocation);
			this.addImage(imageLocation, entry.image);
			this.loadMap.remove(imageLocation);
		} else {
			return image;
		}
		return this.imageCache.get(imageLocation);
	}

	/**
	 * Adds an image associated with a location string to this factory's cache.
	 * The image will be scaled as dictated by this current factory settings.
	 *
	 * @param location
	 *            the location string uniquely identifying the image
	 * @param image
	 *            the actual image
	 * @return the final image added to the cache. This may be a scaled version
	 *         of the original input image.
	 */
	public Image addImage(final String location, Image image) {
		if ((this.m_maxImageWidth > -1) || (this.m_maxImageHeight > -1)) {
			image = this.getScaledImage(image);
			image.getWidth(); // trigger image load
		}
		this.imageCache.put(location, image);
		return image;
	}

	/**
	 * Wait for an image to load.
	 *
	 * @param image
	 *            the image to wait for
	 */
	protected void waitForImage(final Image image) {
		if (image.isBackgroundLoading()) {
			while (image.getProgress() != 1) {
				Thread.yield();
			}
		} // it is a synchronous load so instantiating the image has already
			// perform the wait for.
	}

	/**
	 * Scales an image to fit within the current size thresholds.
	 *
	 * @param img
	 *            the image to scale
	 * @return the scaled image
	 */
	protected Image getScaledImage(final Image img) {
		// resize image, if necessary, to conserve memory
		// and reduce future scaling time
		final double w = img.getWidth() - this.m_maxImageWidth;
		final double h = img.getHeight() - this.m_maxImageHeight;

		final boolean preserveRatio = false;
		final boolean smooth = true;
		if ((w > h) && (w > 0) && (this.m_maxImageWidth > -1)) {
			final Image scaled = new Image(img.impl_getUrl(), this.m_maxImageWidth, img.getHeight(), preserveRatio,
					smooth);
			// img.flush(); //waitForImage(scaled);
			return scaled;
		} else if ((h > 0) && (this.m_maxImageHeight > -1)) {
			final Image scaled = new Image(img.impl_getUrl(), img.getWidth(), this.m_maxImageHeight, preserveRatio,
					smooth);
			// img.flush(); //waitForImage(scaled);
			return scaled;
		} else {
			return img;
		}
	}

	/**
	 * <p>
	 * Preloads images for use in a visualization. Images to load are determined
	 * by taking objects from the given iterator and retrieving the value of the
	 * specified field. The items in the iterator must be instances of the
	 * {@link prefuse.data.Tuple} class.
	 * </p>
	 *
	 * <p>
	 * Images are loaded in the order specified by the iterator until the the
	 * iterator is empty or the maximum image cache size is met. Thus higher
	 * priority images should appear sooner in the iteration.
	 * </p>
	 *
	 * @param iter
	 *            an Iterator of {@link prefuse.data.Tuple} instances
	 * @param field
	 *            the data field that contains the image location
	 */
	public void preloadImages(final Iterator iter, final String field) {
		final boolean synch = this.m_asynch;
		this.m_asynch = false;

		String loc = null;
		while (iter.hasNext() && (this.imageCache.size() <= this.m_imageCacheSize)) {
			// get the string describing the image location
			final Tuple t = (Tuple) iter.next();
			loc = t.getString(field);
			if (loc != null) {
				this.getImage(loc);
			}
		}
		this.m_asynch = synch;
	}

	/**
	 * Helper class for storing an id/image pair.
	 */
	private class LoadMapEntry {
		public int id;
		public Image image;

		public LoadMapEntry(final int id, final Image image) {
			this.id = id;
			this.image = image;
		}
	}

} // end of class ImageFactory
