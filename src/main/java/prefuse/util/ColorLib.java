package prefuse.util;

import javafx.scene.paint.Color;
import prefuse.util.collections.IntObjectHashMap;

/**
 * For this lib, we should have switched to new representation of rgba (using
 * double[]) in JavaFX. However, it implies modification of current cache
 * mechanism, and also modification of mechanism to handle color flag in
 * VisualItem (from int to double[])
 *
 * <p>
 * Library routines for processing color values. The standard color
 * representation used by prefuse is to store each color as single primitive
 * integer value, using 32 bits to represent 4 8-bit color channels: red, green,
 * blue, and alpha (transparency). An alpha value of 0 indicates complete
 * transparency while a maximum value (255) indicated complete opacity. The
 * layout of the bit is as follows, moving from most significant bit on the left
 * to least significant bit on the right:
 * </p>
 *
 * <pre>
 * AAAAAAAARRRRRRRRGGGGGGGBBBBBBBB
 * </pre>
 *
 * <p>
 * This class also maintains methods for mapping these values to actual Java
 * {@link java.awt.Color} instances; a cache is maintained for quick-lookups,
 * avoiding the need to continually allocate new Color instances.
 * </p>
 *
 * <p>
 * Finally, this class also contains routine for creating color palettes for use
 * in visualization.
 * </p>
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class ColorLib {

    public static final char HEX_PREFIX = '#';

    private static final IntObjectHashMap colorMap = new IntObjectHashMap();
    private static int misses = 0;
    private static int lookups = 0;

    // ------------------------------------------------------------------------
    // Color Code Methods

    /**
     * Get the color code for the given red, green, and blue values.
     *
     * @param r
     *            the red color component (in the range 0-255)
     * @param g
     *            the green color component (in the range 0-255)
     * @param b
     *            the blue color component (in the range 0-255)
     * @return the integer color code
     */
    public static int rgb(final int r, final int g, final int b) {
        return ColorLib.rgba(r, g, b, 255);
    }

    /**
     * Get the color code for the given grayscale value.
     *
     * @param v
     *            the grayscale value (in the range 0-255, 0 is black and 255 is
     *            white)
     * @return the integer color code
     */
    public static int gray(final int v) {
        return ColorLib.rgba(v, v, v, 255);
    }

    /**
     * Get the color code for the given grayscale value.
     *
     * @param v
     *            the grayscale value (in the range 0-255, 0 is black and 255 is
     *            white)
     * @param a
     *            the alpha (transparency) value (in the range 0-255)
     * @return the integer color code
     */
    public static int gray(final int v, final int a) {
        return ColorLib.rgba(v, v, v, a);
    }

    /**
     * Parse a hexadecimal String as a color code. The color convention is the
     * same as that used in webpages, with two-decimal hexadecimal numbers
     * representing RGB color values in the range 0-255. A single '#' character
     * may be included at the beginning of the String, but is not required. For
     * example '#000000' is black, 'FFFFFF' is white, '0000FF' is blue, and
     * '#FFFF00' is orange. Color values may also include transparency (alpha)
     * values, ranging from 00 (fully transparent) to FF (fully opaque). If
     * included, alpha values should come first in the string. For example,
     * "#770000FF" is a translucent blue.
     *
     * @param hex
     *            the color code value as a hexadecimal String
     * @return the integer color code for the input String
     */
    public static int hex(String hex) {
        if (hex.charAt(0) == ColorLib.HEX_PREFIX) {
            hex = hex.substring(1);
        }

        if (hex.length() > 6) {
            // break up number, as Integer will puke on a large unsigned int
            final int rgb = Integer.parseInt(hex.substring(2), 16);
            final int alpha = Integer.parseInt(hex.substring(0, 2), 16);
            return ColorLib.setAlpha(rgb, alpha);
        } else {
            return ColorLib.setAlpha(Integer.parseInt(hex, 16), 255);
        }
    }

    /**
     * Get the color code for the given hue, saturation, and brightness values,
     * translating from HSB color space to RGB color space.
     *
     * @param h
     *            the hue value (in the range 0-1.0). This represents the actual
     *            color hue (blue, green, purple, etc).
     * @param s
     *            the saturation value (in the range 0-1.0). This represents
     *            "how much" of the color is included. Lower values can result
     *            in more grayed out or pastel colors.
     * @param b
     *            the brightness value (in the range 0-1.0). This represents how
     *            dark or light the color is.
     * @return the integer color code
     */
    public static int hsb(final float h, final float s, final float b) {
        final Color hsbColor = Color.hsb(h, s, b);
        return ColorLib.color(hsbColor);
    }

    /**
     * Get the color code for the given hue, saturation, and brightness values,
     * translating from HSB color space to RGB color space.
     *
     * @param h
     *            the hue value (in the range 0-1.0). This represents the actual
     *            color hue (blue, green, purple, etc).
     * @param s
     *            the saturation value (in the range 0-1.0). This represents
     *            "how much" of the color is included. Lower values can result
     *            in more grayed out or pastel colors.
     * @param b
     *            the brightness value (in the range 0-1.0). This represents how
     *            dark or light the color is.
     * @param a
     *            the alpha value (in the range 0-1.0). This represents the
     *            transparency of the color.
     * @return the integer color code
     */
    public static int hsba(final float h, final float s, final float b, final float a) {
        return ColorLib.setAlpha(ColorLib.hsb(h, s, b), (int) ((a * 255) + 0.5) & 0xFF);
    }

    /**
     * Get the color code for the given red, green, blue, and alpha values.
     *
     * @param r
     *            the red color component (in the range 0-255)
     * @param g
     *            the green color component (in the range 0-255)
     * @param b
     *            the blue color component (in the range 0-255)
     * @param a
     *            the alpha (transparency) component (in the range 0-255)
     * @return the integer color code
     */
    public static int rgba(final int r, final int g, final int b, final int a) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
    }

    /**
     * Get the color code for the given red, green, blue, and alpha values as
     * floating point numbers in the range 0-1.0.
     *
     * @param r
     *            the red color component (in the range 0-1.0)
     * @param g
     *            the green color component (in the range 0-1.0)
     * @param b
     *            the blue color component (in the range 0-1.0)
     * @param a
     *            the alpha (transparency) component (in the range 0-1.0)
     * @return the integer color code
     */
    public static int rgba(final double r, final double g, final double b, final double a) {
        return ((((int) ((a * 255) + 0.5)) & 0xFF) << 24) | ((((int) ((r * 255) + 0.5)) & 0xFF) << 16)
                | ((((int) ((g * 255) + 0.5)) & 0xFF) << 8) | (((int) ((b * 255) + 0.5)) & 0xFF);
    }

    /**
     * Get the color code for the given Color instance.
     *
     * @param c
     *            the Java Color instance
     * @return the integer color code
     */
    public static int color(final Color c) {
        return ColorLib.rgba(c.getRed(), c.getGreen(), c.getBlue(), c.getOpacity());
    }

    /**
     * Get the red component of the given color.
     *
     * @param color
     *            the color code
     * @return the red component of the color (in the range 0-255)
     */
    public static int red(final int color) {
        return (color >> 16) & 0xFF;
    }

    /**
     * Get the green component of the given color.
     *
     * @param color
     *            the color code
     * @return the green component of the color (in the range 0-255)
     */
    public static int green(final int color) {
        return (color >> 8) & 0xFF;
    }

    /**
     * Get the blue component of the given color.
     *
     * @param color
     *            the color code
     * @return the blue component of the color (in the range 0-255)
     */
    public static int blue(final int color) {
        return color & 0xFF;
    }

	/**
     * Get the alpha component of the given color. Yet with JavaFX, opacity must
     * be in the range of [0.0;1.0]. <br>
     * It means that we will have to convert old value in the range 0-255 to a
     * new value in the range [0.0;1.0].
     *
     * @param color
     *            the color code
     * @return the alpha component of the color (in the range 0-1)
     */
    public static double alpha(final int color) {
        final double oldOpacity = (color >> 24) & 0xFF;
        final double newOpacity = oldOpacity / 255;
        return newOpacity;
    }

    /**
     * Set the alpha component of the given color.
     *
     * @param c
     *            the color code
     * @param alpha
     *            the alpha value to set
     * @return the new color with updated alpha channel
     */
    public static int setAlpha(final int c, final int alpha) {
        return ColorLib.rgba(ColorLib.red(c), ColorLib.green(c), ColorLib.blue(c), alpha);
    }

    // ------------------------------------------------------------------------
    // java.awt.Color Lookup Methods

    /**
     * Get a Java Color object for the given red, green, blue, and alpha values
     * as floating point numbers in the range 0-1.0.
     *
     * @param r
     *            the red color component (in the range 0-1.0)
     * @param g
     *            the green color component (in the range 0-1.0)
     * @param b
     *            the blue color component (in the range 0-1.0)
     * @param a
     *            the alpha (transparency) component (in the range 0-1.0)
     * @return a Java Color object
     */
    public static Color getColor(final float r, final float g, final float b, final float a) {
        return ColorLib.getColor(ColorLib.rgba(r, g, b, a));
    }

    /**
     * Get a Java Color object for the given red, green, and blue values as
     * floating point numbers in the range 0-1.0.
     *
     * @param r
     *            the red color component (in the range 0-1.0)
     * @param g
     *            the green color component (in the range 0-1.0)
     * @param b
     *            the blue color component (in the range 0-1.0)
     * @return a Java Color object
     */
    public static Color getColor(final float r, final float g, final float b) {
        return ColorLib.getColor(r, g, b, 1.0f);
    }

    /**
     * Get a Java Color object for the given red, green, and blue values.
     *
     * @param r
     *            the red color component (in the range 0-255)
     * @param g
     *            the green color component (in the range 0-255)
     * @param b
     *            the blue color component (in the range 0-255)
     * @param a
     *            the alpa (transparency) component (in the range 0-255)
     * @return a Java Color object
     */
    public static Color getColor(final int r, final int g, final int b, final int a) {
        return ColorLib.getColor(ColorLib.rgba(r, g, b, a));
    }

    /**
     * Get a Java Color object for the given red, green, and blue values.
     *
     * @param r
     *            the red color component (in the range 0-255)
     * @param g
     *            the green color component (in the range 0-255)
     * @param b
     *            the blue color component (in the range 0-255)
     * @return a Java Color object
     */
    public static Color getColor(final int r, final int g, final int b) {
        return ColorLib.getColor(r, g, b, 255);
    }

    /**
     * Get a Java Color object for the given grayscale value.
     *
     * @param v
     *            the grayscale value (in the range 0-255, 0 is black and 255 is
     *            white)
     * @return a Java Color object
     */
    public static Color getGrayscale(final int v) {
        return ColorLib.getColor(v, v, v, 255);
    }

    /**
     * Get a Java Color object for the given color code value.
     *
     * @param rgba
     *            the integer color code containing red, green, blue, and alpha
     *            channel information
     * @return a Java Color object
     */
    public static Color getColor(final int rgba) {
        Color c = null;
        if ((c = (Color) ColorLib.colorMap.get(rgba)) == null) {
            c = Color.rgb(ColorLib.red(rgba), ColorLib.green(rgba), ColorLib.blue(rgba), ColorLib.alpha(rgba));
            ColorLib.colorMap.put(rgba, c);
            ColorLib.misses++;
        }
        ColorLib.lookups++;
        return c;
    }

    // ------------------------------------------------------------------------
    // ColorLib Statistics and Cache Management

    /**
     * Get the number of cache misses to the Color object cache.
     *
     * @return the number of cache misses
     */
    public static int getCacheMissCount() {
        return ColorLib.misses;
    }

    /**
     * Get the number of cache lookups to the Color object cache.
     *
     * @return the number of cache lookups
     */
    public static int getCacheLookupCount() {
        return ColorLib.lookups;
    }

    /**
     * Clear the Color object cache.
     */
    public static void clearCache() {
        ColorLib.colorMap.clear();
    }

    // ------------------------------------------------------------------------
    // Color Calculations

    private static final float scale = 0.7f;

    /**
     * Interpolate between two color values by the given mixing proportion. A
     * mixing fraction of 0 will result in c1, a value of 1.0 will result in c2,
     * and value of 0.5 will result in the color mid-way between the two in RGB
     * color space.
     *
     * @param c1
     *            the starting color
     * @param c2
     *            the target color
     * @param frac
     *            a fraction between 0 and 1.0 controlling the interpolation
     *            amount.
     * @return the interpolated color code
     */
    public static int interp(final int c1, final int c2, final double frac) {
        final double ifrac = 1 - frac;
        return ColorLib.rgba((int) Math.round((frac * ColorLib.red(c2)) + (ifrac * ColorLib.red(c1))),
                (int) Math.round((frac * ColorLib.green(c2)) + (ifrac * ColorLib.green(c1))),
                (int) Math.round((frac * ColorLib.blue(c2)) + (ifrac * ColorLib.blue(c1))),
                (int) Math.round((frac * ColorLib.alpha(c2)) + (ifrac * ColorLib.alpha(c1))));
    }

    /**
     * Get a darker shade of an input color.
     *
     * @param c
     *            a color code
     * @return a darkened color code
     */
    public static int darker(final int c) {
        return ColorLib.rgba(Math.max(0, (int) (ColorLib.scale * ColorLib.red(c))), Math.max(0, (int) (ColorLib.scale * ColorLib.green(c))),
                Math.max(0, (int) (ColorLib.scale * ColorLib.blue(c))), ColorLib.alpha(c));
    }

    /**
     * Get a brighter shade of an input color.
     *
     * @param c
     *            a color code
     * @return a brighter color code
     */
    public static int brighter(final int c) {
        int r = ColorLib.red(c), g = ColorLib.green(c), b = ColorLib.blue(c);
        final int i = (int) (1.0 / (1.0 - ColorLib.scale));
        if ((r == 0) && (g == 0) && (b == 0)) {
            return ColorLib.rgba(i, i, i, ColorLib.alpha(c));
        }
        if ((r > 0) && (r < i)) {
            r = i;
        }
        if ((g > 0) && (g < i)) {
            g = i;
        }
        if ((b > 0) && (b < i)) {
            b = i;
        }

        return ColorLib.rgba(Math.min(255, (int) (r / ColorLib.scale)), Math.min(255, (int) (g / ColorLib.scale)),
                Math.min(255, (int) (b / ColorLib.scale)), ColorLib.alpha(c));
    }

    /**
     * Get a desaturated shade of an input color.
     *
     * @param c
     *            a color code
     * @return a desaturated color code
     */
    public static int desaturate(final int c) {
        final int a = c & 0xff000000;
        float r = ((c & 0xff0000) >> 16);
        float g = ((c & 0x00ff00) >> 8);
        float b = (c & 0x0000ff);

        r *= 0.2125f; // red band weight
        g *= 0.7154f; // green band weight
        b *= 0.0721f; // blue band weight

        final int gray = Math.min(((int) (r + g + b)), 0xff) & 0xff;
        return a | (gray << 16) | (gray << 8) | gray;
    }

    /**
     * Set the saturation of an input color.
     *
     * @param c
     *            a color code
     * @param saturation
     *            the new saturation value
     * @return a saturated color code
     */
    public static int saturate(final int c, final float saturation) {
        Color color = ColorLib.getColor(c);
        color = color.deriveColor(0, saturation, 1, 1);
        return ColorLib.color(color);
    }

    // ------------------------------------------------------------------------
    // Color Palettes

    /**
     * Default palette of category hues.
     */
    public static final float[] CATEGORY_HUES = { 0f, 1f / 12f, 1f / 6f, 1f / 3f, 1f / 2f, 7f / 12f, 2f / 3f,
            /* 3f/4f, */ 5f / 6f, 11f / 12f };

    /**
     * The default length of a color palette if its size is not otherwise
     * specified.
     */
    public static final int DEFAULT_MAP_SIZE = 64;

    /**
     * Returns a color palette that uses a "cool", blue-heavy color scheme.
     *
     * @param size
     *            the size of the color palette
     * @return the color palette
     */
    public static int[] getCoolPalette(final int size) {
        final int[] cm = new int[size];
        for (int i = 0; i < size; i++) {
            final float r = i / Math.max(size - 1, 1.f);
            cm[i] = ColorLib.rgba(r, 1 - r, 1.f, 1.f);
        }
        return cm;
    }

    /**
     * Returns a color palette of default size that uses a "cool", blue-heavy
     * color scheme.
     *
     * @return the color palette
     */
    public static int[] getCoolPalette() {
        return ColorLib.getCoolPalette(ColorLib.DEFAULT_MAP_SIZE);
    }

    /**
     * Returns a color map that moves from black to red to yellow to white.
     *
     * @param size
     *            the size of the color palette
     * @return the color palette
     */
    public static int[] getHotPalette(final int size) {
        final int[] cm = new int[size];
        for (int i = 0; i < size; i++) {
            final int n = (3 * size) / 8;
            final float r = (i < n ? ((float) (i + 1)) / n : 1.f);
            final float g = (i < n ? 0.f : (i < (2 * n) ? ((float) (i - n)) / n : 1.f));
            final float b = (i < (2 * n) ? 0.f : ((float) (i - (2 * n))) / (size - (2 * n)));
            cm[i] = ColorLib.rgba(r, g, b, 1.0f);
        }
        return cm;
    }

    /**
     * Returns a color map of default size that moves from black to red to
     * yellow to white.
     *
     * @return the color palette
     */
    public static int[] getHotPalette() {
        return ColorLib.getHotPalette(ColorLib.DEFAULT_MAP_SIZE);
    }

    /**
     * Returns a color palette of given size tries to provide colors appropriate
     * as category labels. There are 12 basic color hues (red, orange, yellow,
     * olive, green, cyan, blue, purple, magenta, and pink). If the size is
     * greater than 12, these colors will be continually repeated, but with
     * varying saturation levels.
     *
     * @param size
     *            the size of the color palette
     * @param s1
     *            the initial saturation to use
     * @param s2
     *            the final (most distant) saturation to use
     * @param b
     *            the brightness value to use
     * @param a
     *            the alpha value to use
     */
    public static int[] getCategoryPalette(final int size, final float s1, final float s2, final float b,
            final float a) {
        final int[] cm = new int[size];
        float s = s1;
        for (int i = 0; i < size; i++) {
            final int j = i % ColorLib.CATEGORY_HUES.length;
            if (j == 0) {
                s = s1 + ((((float) i) / size) * (s2 - s1));
            }
            cm[i] = ColorLib.hsba(ColorLib.CATEGORY_HUES[j], s, b, a);
        }
        return cm;
    }

    /**
     * Returns a color palette of given size tries to provide colors appropriate
     * as category labels. There are 12 basic color hues (red, orange, yellow,
     * olive, green, cyan, blue, purple, magenta, and pink). If the size is
     * greater than 12, these colors will be continually repeated, but with
     * varying saturation levels.
     *
     * @param size
     *            the size of the color palette
     */
    public static int[] getCategoryPalette(final int size) {
        return ColorLib.getCategoryPalette(size, 1.f, 0.4f, 1.f, 1.0f);
    }

    /**
     * Returns a color palette of given size that cycles through the hues of the
     * HSB (Hue/Saturation/Brightness) color space.
     *
     * @param size
     *            the size of the color palette
     * @param s
     *            the saturation value to use
     * @param b
     *            the brightness value to use
     * @return the color palette
     */
    public static int[] getHSBPalette(final int size, final float s, final float b) {
        final int[] cm = new int[size];
        for (int i = 0; i < size; i++) {
            final float h = ((float) i) / (size - 1);
            cm[i] = ColorLib.hsb(h, s, b);
        }
        return cm;
    }

    /**
     * Returns a color palette of default size that cycles through the hues of
     * the HSB (Hue/Saturation/Brightness) color space at full saturation and
     * brightness.
     *
     * @return the color palette
     */
    public static int[] getHSBPalette() {
        return ColorLib.getHSBPalette(ColorLib.DEFAULT_MAP_SIZE, 1.f, 1.f);
    }

    /**
     * Returns a color palette of given size that ranges from one given color to
     * the other.
     *
     * @param size
     *            the size of the color palette
     * @param c1
     *            the initial color in the color map
     * @param c2
     *            the final color in the color map
     * @return the color palette
     */
    public static int[] getInterpolatedPalette(final int size, final int c1, final int c2) {
        final int[] cm = new int[size];
        for (int i = 0; i < size; i++) {
            final float f = ((float) i) / (size - 1);
            cm[i] = ColorLib.interp(c1, c2, f);
        }
        return cm;
    }

    /**
     * Returns a color palette of default size that ranges from one given color
     * to the other.
     *
     * @param c1
     *            the initial color in the color map
     * @param c2
     *            the final color in the color map
     * @return the color palette
     */
    public static int[] getInterpolatedPalette(final int c1, final int c2) {
        return ColorLib.getInterpolatedPalette(ColorLib.DEFAULT_MAP_SIZE, c1, c2);
    }

    /**
     * Returns a color palette of specified size that ranges from white to black
     * through shades of gray.
     *
     * @param size
     *            the size of the color palette
     * @return the color palette
     */
    public static int[] getGrayscalePalette(final int size) {
        final int[] cm = new int[size];
        for (int i = 0, g; i < size; i++) {
            g = Math.round(255 * (0.2f + ((0.6f * (i)) / (size - 1))));
            cm[size - i - 1] = ColorLib.gray(g);
        }
        return cm;
    }

    /**
     * Returns a color palette of default size that ranges from white to black
     * through shades of gray.
     *
     * @return the color palette
     */
    public static int[] getGrayscalePalette() {
        return ColorLib.getGrayscalePalette(ColorLib.DEFAULT_MAP_SIZE);
    }

} // end of class ColorLib
