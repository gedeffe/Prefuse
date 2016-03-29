package prefuse.util.display;

import java.util.Iterator;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import prefuse.Display;
import prefuse.visual.VisualItem;

/**
 * Library routines pertaining to a prefuse Display.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class DisplayLib {

    private DisplayLib() {
        // don't instantiate
    }

    /**
     * Get a bounding rectangle of the VisualItems in the input iterator.
     *
     * @param iter
     *            an iterator of VisualItems
     * @param margin
     *            a margin to add on to the bounding rectangle
     * @return the bounding rectangle. A new Rectangle2D instance is allocated
     *         and returned.
     */
    public static Rectangle2D getBounds(final Iterator<VisualItem> iter, final double margin) {
        // use maximum space !
        Rectangle2D result = Rectangle2D.EMPTY;
        // TODO: synchronization?
        if (iter.hasNext()) {
            final VisualItem item = iter.next();
            final Bounds nb = item.getBounds();
            result = new Rectangle2D(nb.getMinX(), nb.getMinY(), nb.getWidth(), nb.getHeight());
        }
        while (iter.hasNext()) {
            final VisualItem item = iter.next();
            final Bounds nb = item.getBounds();
            final double x1 = (nb.getMinX() < result.getMinX() ? nb.getMinX() : result.getMinX());
            final double x2 = (nb.getMaxX() > result.getMaxX() ? nb.getMaxX() : result.getMaxX());
            final double y1 = (nb.getMinY() < result.getMinY() ? nb.getMinY() : result.getMinY());
            final double y2 = (nb.getMaxY() > result.getMaxY() ? nb.getMaxY() : result.getMaxY());
            result = new Rectangle2D(x1, y1, x2 - x1, y2 - y1);
        }
        result = new Rectangle2D(result.getMinX() - margin, result.getMinY() - margin, result.getWidth() + (2 * margin),
                result.getHeight() + (2 * margin));
        return result;
    }


    /**
     * Return the centroid (averaged location) of a group of items.
     *
     * @param iter
     *            an iterator of VisualItems
     * @return the centroid point. A new Point2D instance is allocated and
     *         returned.
     */
    public static Point2D getCentroid(final Iterator<VisualItem> iter) {
        double cx = 0, cy = 0;
        int count = 0;

        while (iter.hasNext()) {
            final VisualItem item = iter.next();
            final double x = item.getX(), y = item.getY();
            if (!(Double.isInfinite(x) || Double.isNaN(x)) && !(Double.isInfinite(y) || Double.isNaN(y))) {
                cx += x;
                cy += y;
                count++;
            }
        }
        if (count > 0) {
            cx /= count;
            cy /= count;
        }
        final Point2D result = new Point2D(cx, cy);
        return result;
    }


    /**
     * Set the display view such that the given bounds are within view.
     *
     * @param display
     *            the Display instance
     * @param bounds
     *            the bounds that should be visible in the Display view
     * @param duration
     *            the duration of an animated transition. A value of zero will
     *            result in an instantaneous change.
     */
    public static void fitViewToBounds(final Display display, final Rectangle2D bounds, final long duration) {
        DisplayLib.fitViewToBounds(display, bounds, null, duration);
    }

    /**
     * Set the display view such that the given bounds are within view, subject
     * to a given center point being maintained.
     *
     * @param display
     *            the Display instance
     * @param bounds
     *            the bounds that should be visible in the Display view
     * @param center
     *            the point that should be the center of the Display
     * @param duration
     *            the duration of an animated transition. A value of zero will
     *            result in an instantaneous change.
     */
    public static void fitViewToBounds(final Display display, final Rectangle2D bounds, Point2D center,
            final long duration) {
        // init variables
        final double w = display.getWidth(), h = display.getHeight();
        final double cx = (center == null ? /* bounds.getCenterX() */ (bounds.getMinX() + (bounds.getWidth() / 2))
                : center.getX());
        final double cy = (center == null ? /* bounds.getCenterY() */ (bounds.getMinY() + (bounds.getHeight() / 2))
                : center.getY());

        // compute half-widths of final bounding box around
        // the desired center point
        final double wb = Math.max(cx - bounds.getMinX(), bounds.getMaxX() - cx);
        final double hb = Math.max(cy - bounds.getMinY(), bounds.getMaxY() - cy);

        // compute scale factor
        // - figure out if z or y dimension takes priority
        // - then balance against the current scale factor
        final double scale = Math.min(w / (2 * wb), h / (2 * hb)) / display.getScale();

        // animate to new display settings
        if (center == null) {
            center = new Point2D(cx, cy);
        }
        if (duration > 0) {
            display.animatePanAndZoomToAbs(center, scale, duration);
        } else {
            display.panToAbs(center);
            display.zoomAbs(center, scale);
        }
    }

} // end of class DisplayLib
