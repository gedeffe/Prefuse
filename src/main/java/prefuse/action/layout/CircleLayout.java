package prefuse.action.layout;

import java.util.Iterator;

import javafx.geometry.Rectangle2D;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.VisualItem;

/**
 * Layout action that positions visual items along a circle. By default, items
 * are sorted in the order in which they iterated over.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class CircleLayout extends Layout {

	private double m_radius; // radius of the circle layout

	/**
	 * Create a CircleLayout; the radius of the circle layout will be computed
	 * automatically based on the display size.
	 *
	 * @param group
	 *            the data group to layout
	 */
	public CircleLayout(final String group) {
		super(group);
	}

	/**
	 * Create a CircleLayout; use the specified radius for the the circle
	 * layout, regardless of the display size.
	 *
	 * @param group
	 *            the data group to layout
	 * @param radius
	 *            the radius of the circle layout.
	 */
	public CircleLayout(final String group, final double radius) {
		super(group);
		this.m_radius = radius;
	}

	/**
	 * Return the radius of the layout circle.
	 *
	 * @return the circle radius
	 */
	public double getRadius() {
		return this.m_radius;
	}

	/**
	 * Set the radius of the layout circle.
	 *
	 * @param radius
	 *            the circle radius to use
	 */
	public void setRadius(final double radius) {
		this.m_radius = radius;
	}

	/**
	 * @see prefuse.action.Action#run(double)
	 */
	@Override
	public void run(final double frac) {
		final TupleSet ts = this.m_vis.getGroup(this.m_group);

		final int nn = ts.getTupleCount();

		final Rectangle2D r = this.getLayoutBounds();
		final double height = r.getHeight();
		final double width = r.getWidth();
		final double cx = (r.getMinX() + r.getWidth()) / 2;
		final double cy = (r.getMinY() + r.getHeight()) / 2;

		double radius = this.m_radius;
		if (radius <= 0) {
			radius = 0.45 * (height < width ? height : width);
		}

		final Iterator items = ts.tuples();
		for (int i = 0; items.hasNext(); i++) {
			final VisualItem n = (VisualItem) items.next();
			final double angle = (2 * Math.PI * i) / nn;
			final double x = (Math.cos(angle) * radius) + cx;
			final double y = (Math.sin(angle) * radius) + cy;
			this.setX(n, null, x);
			this.setY(n, null, y);
		}
	}

} // end of class CircleLayout
