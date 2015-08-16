package prefuse.action.layout;

import java.util.Iterator;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import prefuse.Constants;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.StartVisiblePredicate;

/**
 * Layout Action that sets the positions for newly collapsed or newly expanded
 * nodes of a tree. This action updates positions such that nodes flow out from
 * their parents or collapse back into their parents upon animated transitions.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class CollapsedSubtreeLayout extends Layout {

	private int m_orientation;
	private Point2D m_point = Point2D.ZERO;

	/**
	 * Create a new CollapsedSubtreeLayout. By default, nodes will collapse to
	 * the center point of their parents.
	 *
	 * @param group
	 *            the data group to layout (only newly collapsed or newly
	 *            expanded items will be considered, as determined by their
	 *            current visibility settings).
	 */
	public CollapsedSubtreeLayout(final String group) {
		this(group, Constants.ORIENT_CENTER);
	}

	/**
	 * Create a new CollapsedSubtreeLayout.
	 *
	 * @param group
	 *            the data group to layout (only newly collapsed or newly
	 *            expanded items will be considered, as determined by their
	 *            current visibility settings).
	 * @param orientation
	 *            the layout orientation, determining which point nodes will
	 *            collapse/expand from. Valid values are
	 *            {@link prefuse.Constants#ORIENT_CENTER},
	 *            {@link prefuse.Constants#ORIENT_LEFT_RIGHT},
	 *            {@link prefuse.Constants#ORIENT_RIGHT_LEFT},
	 *            {@link prefuse.Constants#ORIENT_TOP_BOTTOM}, and
	 *            {@link prefuse.Constants#ORIENT_BOTTOM_TOP}.
	 */
	public CollapsedSubtreeLayout(final String group, final int orientation) {
		super(group);
		this.m_orientation = orientation;
	}

	// ------------------------------------------------------------------------

	/**
	 * Get the layout orientation, determining which point nodes will collapse
	 * or exapnd from. Valid values are {@link prefuse.Constants#ORIENT_CENTER},
	 * {@link prefuse.Constants#ORIENT_LEFT_RIGHT},
	 * {@link prefuse.Constants#ORIENT_RIGHT_LEFT},
	 * {@link prefuse.Constants#ORIENT_TOP_BOTTOM}, and
	 * {@link prefuse.Constants#ORIENT_BOTTOM_TOP}.
	 *
	 * @return the layout orientation
	 */
	public int getOrientation() {
		return this.m_orientation;
	}

	/**
	 * Set the layout orientation, determining which point nodes will collapse
	 * or exapnd from. Valid values are {@link prefuse.Constants#ORIENT_CENTER},
	 * {@link prefuse.Constants#ORIENT_LEFT_RIGHT},
	 * {@link prefuse.Constants#ORIENT_RIGHT_LEFT},
	 * {@link prefuse.Constants#ORIENT_TOP_BOTTOM}, and
	 * {@link prefuse.Constants#ORIENT_BOTTOM_TOP}.
	 *
	 * @return the layout orientation to use
	 */
	public void setOrientation(final int orientation) {
		if ((orientation < 0) || (orientation >= Constants.ORIENTATION_COUNT)) {
			throw new IllegalArgumentException("Unrecognized orientation value: " + orientation);
		}
		this.m_orientation = orientation;
	}

	// ------------------------------------------------------------------------

	/**
	 * @see prefuse.action.Action#run(double)
	 */
	@Override
	public void run(final double frac) {
		// handle newly expanded subtrees - ensure they emerge from
		// a visible ancestor node
		Iterator items = this.m_vis.visibleItems(this.m_group);
		while (items.hasNext()) {
			final VisualItem item = (VisualItem) items.next();
			if ((item instanceof NodeItem) && !item.isStartVisible()) {
				final NodeItem n = (NodeItem) item;
				final Point2D p = this.getPoint(n, true);
				n.setStartX(p.getX());
				n.setStartY(p.getY());
			}
		}

		// handle newly collapsed nodes - ensure they collapse to
		// the greatest visible ancestor node
		items = this.m_vis.items(this.m_group, StartVisiblePredicate.TRUE);
		while (items.hasNext()) {
			final VisualItem item = (VisualItem) items.next();
			if ((item instanceof NodeItem) && !item.isEndVisible()) {
				final NodeItem n = (NodeItem) item;
				final Point2D p = this.getPoint(n, false);
				n.setStartX(n.getEndX());
				n.setStartY(n.getEndY());
				n.setEndX(p.getX());
				n.setEndY(p.getY());
			}
		}

	}

	private Point2D getPoint(final NodeItem n, final boolean start) {
		// find the visible ancestor
		NodeItem p = (NodeItem) n.getParent();
		if (start) {
			for (; (p != null) && !p.isStartVisible(); p = (NodeItem) p.getParent()) {
				;
			}
		} else {
			for (; (p != null) && !p.isEndVisible(); p = (NodeItem) p.getParent()) {
				;
			}
		}
		if (p == null) {
			this.m_point = new Point2D(n.getX(), n.getY());
			return this.m_point;
		}

		// get the vanishing/appearing point
		final double x = start ? p.getStartX() : p.getEndX();
		final double y = start ? p.getStartY() : p.getEndY();
		final Bounds b = p.getBounds();
		switch (this.m_orientation) {
		case Constants.ORIENT_LEFT_RIGHT:
			this.m_point = new Point2D(x + b.getWidth(), y);
			break;
		case Constants.ORIENT_RIGHT_LEFT:
			this.m_point = new Point2D(x - b.getWidth(), y);
			break;
		case Constants.ORIENT_TOP_BOTTOM:
			this.m_point = new Point2D(x, y + b.getHeight());
			break;
		case Constants.ORIENT_BOTTOM_TOP:
			this.m_point = new Point2D(x, y - b.getHeight());
			break;
		case Constants.ORIENT_CENTER:
			this.m_point = new Point2D(x, y);
			break;
		}
		return this.m_point;
	}

} // end of class CollapsedSubtreeLayout
