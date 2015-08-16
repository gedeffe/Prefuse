package prefuse.util.display;

import javafx.geometry.Rectangle2D;
import java.util.Iterator;

import javafx.geometry.Point2D;
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
	 * @param b
	 *            the Rectangle instance in which to store the result
	 * @return the bounding rectangle. This is the same object as the parameter
	 *         <code>b</code>.
	 */
	public static Rectangle2D getBounds(final Iterator iter, final double margin, final Rectangle2D b) {
		b.setFrame(Double.NaN, Double.NaN, Double.NaN, Double.NaN);
		// TODO: synchronization?
		if (iter.hasNext()) {
			final VisualItem item = (VisualItem) iter.next();
			final Rectangle2D nb = item.getBounds();
			b.setFrame(nb);
		}
		while (iter.hasNext()) {
			final VisualItem item = (VisualItem) iter.next();
			final Rectangle2D nb = item.getBounds();
			final double x1 = (nb.getMinX() < b.getMinX() ? nb.getMinX() : b.getMinX());
			final double x2 = (nb.getMaxX() > b.getMaxX() ? nb.getMaxX() : b.getMaxX());
			final double y1 = (nb.getMinY() < b.getMinY() ? nb.getMinY() : b.getMinY());
			final double y2 = (nb.getMaxY() > b.getMaxY() ? nb.getMaxY() : b.getMaxY());
			b.setFrame(x1, y1, x2 - x1, y2 - y1);
		}
		b.setFrame(b.getMinX() - margin, b.getMinY() - margin, b.getWidth() + (2 * margin),
				b.getHeight() + (2 * margin));
		return b;
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
	public static Rectangle2D getBounds(final Iterator iter, final double margin) {
		final Rectangle2D b = new Rectangle2D.Double();
		return getBounds(iter, margin, b);
	}

	/**
	 * Return the centroid (averaged location) of a group of items.
	 * 
	 * @param iter
	 *            an iterator of VisualItems
	 * @param p
	 *            a Point2D instance in which to store the result
	 * @return the centroid point. This is the same object as the parameter
	 *         <code>p</code>.
	 */
	public static Point2D getCentroid(final Iterator iter, final Point2D p) {
		double cx = 0, cy = 0;
		int count = 0;

		while (iter.hasNext()) {
			final VisualItem item = (VisualItem) iter.next();
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
		p.setLocation(cx, cy);
		return p;
	}

	/**
	 * Return the centroid (averaged location) of a group of items.
	 * 
	 * @param iter
	 *            an iterator of VisualItems
	 * @return the centroid point. A new Point2D instance is allocated and
	 *         returned.
	 */
	public static Point2D getCentroid(final Iterator iter) {
		return getCentroid(iter, Point2D.ZERO);
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
		fitViewToBounds(display, bounds, null, duration);
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
		final double cx = (center == null ? bounds.getCenterX() : center.getX());
		final double cy = (center == null ? bounds.getCenterY() : center.getY());

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
