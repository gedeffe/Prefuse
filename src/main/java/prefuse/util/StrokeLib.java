package prefuse.util;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import prefuse.util.collections.IntObjectHashMap;

/**
 * Library maintaining a cache of drawing strokes and other useful stroke
 * computation routines.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class StrokeLib {

    private static final IntObjectHashMap strokeMap = new IntObjectHashMap();
    private static int misses = 0;
    private static int lookups = 0;

    /** Dash pattern for a dotted line */
    public static final float[] DOTS = new float[] { 1.0f, 2.0f };
    /** Dash pattern for regular uniform dashes */
    public static final float[] DASHES = new float[] { 5.0f, 5.0f };
    /** Dash pattern for longer uniform dashes */
    public static final float[] LONG_DASHES = new float[] { 10.0f, 10.0f };

    /**
     * Get a square capped, miter joined, non-dashed stroke of the given width.
     *
     * @param width
     *            the requested stroke width
     * @return the stroke
     */
    public static Shape getStroke(final float width) {
        return StrokeLib.getStroke(width, StrokeLineCap.SQUARE, StrokeLineJoin.MITER);
    }

    /**
     * Get a square capped, miter joined, dashed stroke with the given width and
     * dashing attributes.
     *
     * @param width
     *            the requested stroke width
     * @param dashes
     *            an array describing the alternation pattern of a dashed line.
     *            For example [5f, 3f] will create dashes of length 5 with
     *            spaces of length 3 between them. A null value indicates no
     *            dashing.
     * @return the stroke
     * @see java.awt.BasicStroke
     */
    public static Shape getStroke(final float width, final double[] dashes) {
        final int length = dashes.length;
        final List<Double> dashesList = new ArrayList<Double>(length);
        for (int i = 0; i < length; i++) {
            dashesList.add(dashes[i]);
        }
        return StrokeLib.getStroke(width, StrokeLineCap.SQUARE, StrokeLineJoin.MITER, 10.0f,
                FXCollections.observableList(dashesList), 0f);
    }

    /**
     * Get a non-dashed stroke of the given width, cap, and join
     *
     * @param width
     *            the requested stroke width
     * @param cap
     *            the requested cap type, one of {@link StrokeLineCap} values.
     * @param join
     *            the requested join type, one of {@link StrokeLineJoin} values.
     * @return the stroke
     */
    public static Shape getStroke(final float width, final StrokeLineCap cap, final StrokeLineJoin join) {
        return StrokeLib.getStroke(width, cap, join, 10.0f, null, 0f);
    }

    /**
     * Get a dashed stroke of the given width, cap, join, miter limit, and
     * dashing attributes.
     *
     * @param d
     *            the requested stroke width
     * @param strokeLineCap
     *            the requested cap type, one of {@link StrokeLineCap} values.
     * @param strokeLineJoin
     *            the requested join type, one of {@link StrokeLineJoin} values.
     * @param e
     *            the miter limit at which to bevel miter joins
     * @param dashes
     *            an array describing the alternation pattern of a dashed line.
     *            For example [5f, 3f] will create dashes of length 5 with
     *            spaces of length 3 between them. A null value indicates no
     *            dashing.
     * @param f
     *            the phase or offset from which to begin the dash pattern
     * @return the stroke
     * @see java.awt.BasicStroke
     */
    public static Shape getStroke(final double d, final StrokeLineCap strokeLineCap,
            final StrokeLineJoin strokeLineJoin, final double e, final ObservableList<Double> dashes, final double f) {
        final int key = StrokeLib.getStrokeKey(d, strokeLineCap, strokeLineJoin, e, dashes, f);
        Shape s = null;
        if ((s = (Shape) StrokeLib.strokeMap.get(key)) == null) {
            // we will use a basic Shape
            s = new Line();
            s.setStrokeWidth(d);
            s.setStrokeLineCap(strokeLineCap);
            s.setStrokeLineJoin(strokeLineJoin);
            s.setStrokeMiterLimit(e);
            s.getStrokeDashArray().addAll(dashes);
            s.setStrokeDashOffset(f);
            StrokeLib.strokeMap.put(key, s);
            ++StrokeLib.misses;
        }
        ++StrokeLib.lookups;
        return s;
    }

    /**
     * Compute a hash-key for stroke storage and lookup.
     */
    protected static int getStrokeKey(final double d, final StrokeLineCap strokeLineCap,
            final StrokeLineJoin strokeLineJoin, final double e, final ObservableList<Double> observableList,
            final double f) {
        int hash = Long.valueOf(Double.doubleToLongBits(d)).intValue();
        hash = (hash * 31) + strokeLineJoin.ordinal();
        hash = (hash * 31) + strokeLineCap.ordinal();
        hash = (hash * 31) + Long.valueOf(Double.doubleToLongBits(e)).intValue();
        if (observableList != null) {
            hash = (hash * 31) + Long.valueOf(Double.doubleToLongBits(f)).intValue();
            for (int i = 0; i < observableList.size(); i++) {
                hash = (hash * 31) + Long.valueOf(Double.doubleToLongBits(observableList.get(i))).intValue();
            }
        }
        return hash;
    }

    /**
     * Get a stroke with the same properties as the given stroke, but with a
     * modified width value.
     *
     * @param stroke
     *            the stroke to base the returned stroke on
     * @param width
     *            the desired width of the derived stroke
     * @return the derived Stroke
     */
    public static Shape getDerivedStroke(final Shape stroke, final float width) {
        if (stroke.getStrokeWidth() == width) {
            return stroke;
        } else {
            return StrokeLib.getStroke(width * stroke.getStrokeWidth(), stroke.getStrokeLineCap(),
                    stroke.getStrokeLineJoin(), stroke.getStrokeMiterLimit(), stroke.getStrokeDashArray(),
                    stroke.getStrokeDashOffset());
        }
    }

    /**
     * Get the number of cache misses to the Stroke object cache.
     *
     * @return the number of cache misses
     */
    public static int getCacheMissCount() {
        return StrokeLib.misses;
    }

    /**
     * Get the number of cache lookups to the Stroke object cache.
     *
     * @return the number of cache lookups
     */
    public static int getCacheLookupCount() {
        return StrokeLib.lookups;
    }

    /**
     * Clear the Stroke object cache.
     */
    public static void clearCache() {
        StrokeLib.strokeMap.clear();
    }

} // end of class StrokeLib
