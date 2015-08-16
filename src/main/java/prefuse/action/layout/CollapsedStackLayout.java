package prefuse.action.layout;

import java.util.Iterator;

import javafx.geometry.Rectangle2D;
import prefuse.Constants;
import prefuse.data.Table;
import prefuse.render.PolygonRenderer;
import prefuse.visual.VisualItem;

/**
 * Layout Action that updates the outlines of polygons in a stacked line chart,
 * properly setting the coordinates of "collapsed" stacks.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class CollapsedStackLayout extends Layout {

	private final String m_polyField;
	private int m_orientation = Constants.ORIENT_BOTTOM_TOP;
	private boolean m_horiz = false;
	private boolean m_top = false;

	/**
	 * Create a new CollapsedStackLayout. The polygon field is assumed to be
	 * {@link prefuse.render.PolygonRenderer#POLYGON}.
	 * 
	 * @param group
	 *            the data group to layout
	 */
	public CollapsedStackLayout(final String group) {
		this(group, PolygonRenderer.POLYGON);
	}

	/**
	 * Create a new CollapsedStackLayout.
	 * 
	 * @param group
	 *            the data group to layout
	 * @param field
	 *            the data field from which to lookup the polygons
	 */
	public CollapsedStackLayout(final String group, final String field) {
		super(group);
		this.m_polyField = field;
	}

	/**
	 * Returns the orientation of this layout. One of
	 * {@link Constants#ORIENT_BOTTOM_TOP} (to grow bottom-up),
	 * {@link Constants#ORIENT_TOP_BOTTOM} (to grow top-down),
	 * {@link Constants#ORIENT_LEFT_RIGHT} (to grow left-right), or
	 * {@link Constants#ORIENT_RIGHT_LEFT} (to grow right-left).
	 * 
	 * @return the orientation of this layout
	 */
	public int getOrientation() {
		return this.m_orientation;
	}

	/**
	 * Sets the orientation of this layout. Must be one of
	 * {@link Constants#ORIENT_BOTTOM_TOP} (to grow bottom-up),
	 * {@link Constants#ORIENT_TOP_BOTTOM} (to grow top-down),
	 * {@link Constants#ORIENT_LEFT_RIGHT} (to grow left-right), or
	 * {@link Constants#ORIENT_RIGHT_LEFT} (to grow right-left).
	 * 
	 * @param orient
	 *            the desired orientation of this layout
	 * @throws IllegalArgumentException
	 *             if the orientation value is not a valid value
	 */
	public void setOrientation(final int orient) {
		if ((orient != Constants.ORIENT_TOP_BOTTOM) && (orient != Constants.ORIENT_BOTTOM_TOP)
				&& (orient != Constants.ORIENT_LEFT_RIGHT) && (orient != Constants.ORIENT_RIGHT_LEFT)) {
			throw new IllegalArgumentException("Invalid orientation value: " + orient);
		}
		this.m_orientation = orient;
		this.m_horiz = ((this.m_orientation == Constants.ORIENT_LEFT_RIGHT)
				|| (this.m_orientation == Constants.ORIENT_RIGHT_LEFT));
		this.m_top = ((this.m_orientation == Constants.ORIENT_TOP_BOTTOM)
				|| (this.m_orientation == Constants.ORIENT_LEFT_RIGHT));
	}

	/**
	 * @see prefuse.action.Action#run(double)
	 */
	@Override
	public void run(final double frac) {
		VisualItem lastItem = null;

		final Rectangle2D bounds = this.getLayoutBounds();
		final float floor = (float) (this.m_horiz ? (this.m_top ? bounds.getMaxX() : bounds.getMinX())
				: (this.m_top ? bounds.getMinY() : bounds.getMaxY()));
		final int bias = (this.m_horiz ? 0 : 1);

		// TODO: generalize this -- we want tuplesReversed available for general
		// sets
		final Iterator iter = ((Table) this.m_vis.getGroup(this.m_group)).tuplesReversed();
		while (iter.hasNext()) {
			final VisualItem item = (VisualItem) iter.next();
			final boolean prev = item.isStartVisible();
			final boolean cur = item.isVisible();

			if (!prev && cur) {
				// newly visible, update contour
				final float[] f = (float[]) item.get(this.m_polyField);
				if (f == null) {
					continue;
				}

				if (lastItem == null) {
					// no previous items, smash values to the floor
					for (int i = 0; i < f.length; i += 2) {
						f[i + bias] = floor;
					}
				} else {
					// previous visible item, smash values to the
					// visible item's contour
					final float[] l = (float[]) lastItem.get(this.m_polyField);
					for (int i = 0; i < (f.length / 2); i += 2) {
						f[i + bias] = f[(f.length - 2 - i) + bias] = l[i + bias];
					}
				}
			} else if (prev && cur) {
				// this item was previously visible, remember it
				lastItem = item;
			}
		}
	}

} // end of class CollapsedStackAction
