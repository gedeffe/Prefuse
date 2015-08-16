package prefuse.action.distortion;

import java.util.Iterator;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import prefuse.action.layout.Layout;
import prefuse.visual.VisualItem;

/**
 * Abstract base class providing a structure for space-distortion techniques.
 *
 * @version 1.0
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public abstract class Distortion extends Layout {

	private final Point2D m_tmp = Point2D.ZERO;
	protected boolean m_distortSize = true;
	protected boolean m_distortX = true;
	protected boolean m_distortY = true;

	// ------------------------------------------------------------------------

	/**
	 * Create a new Distortion instance.
	 */
	public Distortion() {
		super();
	}

	/**
	 * Create a new Distortion instance that processes the given data group.
	 *
	 * @param group
	 *            the data group processed by this Distortion instance
	 */
	public Distortion(final String group) {
		super(group);
	}

	// ------------------------------------------------------------------------

	/**
	 * Controls whether item sizes are distorted along with the item locations.
	 *
	 * @param s
	 *            true to distort size, false to distort positions only
	 */
	public void setSizeDistorted(final boolean s) {
		this.m_distortSize = s;
	}

	/**
	 * Indicates whether the item sizes are distorted along with the item
	 * locations.
	 *
	 * @return true if item sizes are distorted by this action, false otherwise
	 */
	public boolean isSizeDistorted() {
		return this.m_distortSize;
	}

	// ------------------------------------------------------------------------

	/**
	 * @see prefuse.action.Action#run(double)
	 */
	@Override
	public void run(final double frac) {
		final Rectangle2D bounds = this.getLayoutBounds();
		final Point2D anchor = this.correct(this.m_anchor, bounds);

		final Iterator iter = this.getVisualization().visibleItems(this.m_group);

		while (iter.hasNext()) {
			final VisualItem item = (VisualItem) iter.next();
			if (item.isFixed()) {
				continue;
			}

			// reset distorted values
			// TODO - make this play nice with animation?
			item.setX(item.getEndX());
			item.setY(item.getEndY());
			item.setSize(item.getEndSize());

			// compute distortion if we have a distortion focus
			if (anchor != null) {
				final Bounds bbox = item.getBounds();
				double x = item.getX();
				double y = item.getY();

				// position distortion
				if (this.m_distortX) {
					item.setX(x = this.distortX(x, anchor, bounds));
				}
				if (this.m_distortY) {
					item.setY(y = this.distortY(y, anchor, bounds));
				}

				// size distortion
				if (this.m_distortSize) {
					final Rectangle2D originalBounds = new Rectangle2D(bbox.getMinX(), bbox.getMinY(), bbox.getWidth(),
							bbox.getHeight());
					final double sz = this.distortSize(originalBounds, x, y, anchor, bounds);
					item.setSize(sz * item.getSize());
				}
			}
		}
	}

	/**
	 * Corrects the anchor position, such that if the anchor is outside the
	 * layout bounds, the anchor is adjusted to be the nearest point on the edge
	 * of the bounds.
	 *
	 * @param anchor
	 *            the un-corrected anchor point
	 * @param bounds
	 *            the layout bounds
	 * @return the corrected anchor point
	 */
	protected Point2D correct(final Point2D anchor, final Rectangle2D bounds) {
		if (anchor == null) {
			return anchor;
		}
		double x = anchor.getX(), y = anchor.getY();
		final double x1 = bounds.getMinX(), y1 = bounds.getMinY();
		final double x2 = bounds.getMaxX(), y2 = bounds.getMaxY();
		x = (x < x1 ? x1 : (x > x2 ? x2 : x));
		y = (y < y1 ? y1 : (y > y2 ? y2 : y));

		return new Point2D(x, y);
	}

	/**
	 * Distorts an item's x-coordinate.
	 *
	 * @param x
	 *            the undistorted x coordinate
	 * @param anchor
	 *            the anchor or focus point of the display
	 * @param bounds
	 *            the layout bounds
	 * @return the distorted x-coordinate
	 */
	protected abstract double distortX(double x, Point2D anchor, Rectangle2D bounds);

	/**
	 * Distorts an item's y-coordinate.
	 *
	 * @param y
	 *            the undistorted y coordinate
	 * @param anchor
	 *            the anchor or focus point of the display
	 * @param bounds
	 *            the layout bounds
	 * @return the distorted y-coordinate
	 */
	protected abstract double distortY(double y, Point2D anchor, Rectangle2D bounds);

	/**
	 * Returns the scaling factor by which to transform the size of an item.
	 *
	 * @param bbox
	 *            the bounding box of the undistorted item
	 * @param x
	 *            the x-coordinate of the distorted item
	 * @param y
	 *            the y-coordinate of the distorted item
	 * @param anchor
	 *            the anchor or focus point of the display
	 * @param bounds
	 *            the layout bounds
	 * @return the scaling factor by which to change the size
	 */
	protected abstract double distortSize(Rectangle2D bbox, double x, double y, Point2D anchor, Rectangle2D bounds);

} // end of abstract class Distortion
