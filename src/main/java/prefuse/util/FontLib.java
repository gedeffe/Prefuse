package prefuse.util;

import javafx.scene.text.Font;
import prefuse.util.collections.IntObjectHashMap;

/**
 * Library maintaining a cache of fonts and other useful font computation
 * routines.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class FontLib {

	private static final IntObjectHashMap fontMap = new IntObjectHashMap();
	private static int misses = 0;
	private static int lookups = 0;

	/**
	 * Get a Font instance with the given font family name, style, and size
	 *
	 * @param family
	 *            the font name. Any font installed on your system should be
	 *            valid. Common examples include "Arial", "Verdana", "Tahoma",
	 *            "Times New Roman", "Georgia", and "Courier New".
	 * @param style
	 *            the font style, such as bold or italics. This field uses the
	 *            same style values as the Java {@link javafx.scene.text.Font}
	 *            class.
	 * @param size
	 *            the size, in points, of the font
	 * @return the requested Font instance
	 */
	public static Font getFont(final String family, final double size) {
		final int key = (family.hashCode() << 8) + ((int) Math.floor(size) << 2);
		Font f = null;
		if ((f = (Font) fontMap.get(key)) == null) {
			f = Font.font(family, size);
			fontMap.put(key, f);
			misses++;
		}
		lookups++;
		return f;
	}

	/**
	 * Get the number of cache misses to the Font object cache.
	 *
	 * @return the number of cache misses
	 */
	public static int getCacheMissCount() {
		return misses;
	}

	/**
	 * Get the number of cache lookups to the Font object cache.
	 *
	 * @return the number of cache lookups
	 */
	public static int getCacheLookupCount() {
		return lookups;
	}

	/**
	 * Clear the Font object cache.
	 */
	public static void clearCache() {
		fontMap.clear();
	}

	/**
	 * Interpolate between two font instances. Font sizes are interpolated
	 * linearly. If the interpolation fraction is under 0.5, the face and style
	 * of the starting font are used, otherwise the face and style of the second
	 * font are applied.
	 *
	 * @param f1
	 *            the starting font
	 * @param f2
	 *            the target font
	 * @param frac
	 *            a fraction between 0 and 1.0 controlling the interpolation
	 *            amount.
	 * @return an interpolated Font instance
	 */
	public static Font getIntermediateFont(final Font f1, final Font f2, final double frac) {
		String name;
		double size;
		if (frac < 0.5) {
			name = f1.getName();
		} else {
			name = f2.getName();
		}
		size = (frac * f2.getSize()) + ((1 - frac) * f1.getSize());
		return getFont(name, size);
	}

} // end of class FontLib
