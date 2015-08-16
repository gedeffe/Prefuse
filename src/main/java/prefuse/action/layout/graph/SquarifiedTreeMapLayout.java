package prefuse.action.layout.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import prefuse.data.Graph;
import prefuse.data.Schema;
import prefuse.data.tuple.TupleSet;
import prefuse.data.util.TreeNodeIterator;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 * <p>
 * TreeLayout instance computing a TreeMap layout that optimizes for low aspect
 * ratios of visualized tree nodes. TreeMaps are a form of space-filling layout
 * that represents nodes as boxes on the display, with children nodes
 * represented as boxes placed within their parent's box.
 * </p>
 * <p>
 * This particular algorithm is taken from Bruls, D.M., C. Huizing, and J.J. van
 * Wijk, "Squarified Treemaps" In <i>Data Visualization 2000, Proceedings of the
 * Joint Eurographics and IEEE TCVG Sumposium on Visualization</i>, 2000, pp.
 * 33-42. Available online at:
 * <a href="http://www.win.tue.nl/~vanwijk/stm.pdf"> http://www.win.tue.nl/~
 * vanwijk/stm.pdf</a>.
 * </p>
 * <p>
 * For more information on TreeMaps in general, see
 * <a href="http://www.cs.umd.edu/hcil/treemap-history/"> http://www.cs.umd.edu/
 * hcil/treemap-history/</a>.
 * </p>
 *
 * @version 1.0
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class SquarifiedTreeMapLayout extends TreeLayout {

	// column value in which layout stores area information
	public static final String AREA = "_area";
	public static final Schema AREA_SCHEMA = new Schema();

	static {
		AREA_SCHEMA.addColumn(AREA, double.class);
	}

	private static Comparator s_cmp = new Comparator() {
		@Override
		public int compare(final Object o1, final Object o2) {
			final double s1 = ((VisualItem) o1).getDouble(AREA);
			final double s2 = ((VisualItem) o2).getDouble(AREA);
			return (s1 > s2 ? 1 : (s1 < s2 ? -1 : 0));
		}
	};
	private final ArrayList m_kids = new ArrayList();
	private final ArrayList m_row = new ArrayList();
	private Rectangle2D m_r = Rectangle2D.EMPTY;

	private double m_frame; // space between parents border and children

	/**
	 * Creates a new SquarifiedTreeMapLayout with no spacing between parent
	 * areas and their enclosed children.
	 *
	 * @param group
	 *            the data group to layout. Must resolve to a Graph instance.
	 */
	public SquarifiedTreeMapLayout(final String group) {
		this(group, 0);
	}

	/**
	 * Creates a new SquarifiedTreeMapLayout with the specified spacing between
	 * parent areas and their enclosed children.
	 *
	 * @param frame
	 *            the amount of desired framing space between parent areas and
	 *            their enclosed children.
	 * @param group
	 *            the data group to layout. Must resolve to a Graph instance.
	 */
	public SquarifiedTreeMapLayout(final String group, final double frame) {
		super(group);
		this.setFrameWidth(frame);
	}

	/**
	 * Sets the amount of desired framing space between parent rectangles and
	 * their enclosed children. Use a value of 0 to remove frames altogether. If
	 * you adjust the frame value, you must re-run the layout to see the change
	 * reflected. Negative frame values are not allowed and will result in an
	 * IllegalArgumentException.
	 *
	 * @param frame
	 *            the frame width, 0 for no frames
	 */
	public void setFrameWidth(final double frame) {
		if (frame < 0) {
			throw new IllegalArgumentException("Frame value must be greater than or equal to 0.");
		}
		this.m_frame = frame;
	}

	/**
	 * Gets the amount of desired framing space, in pixels, between parent
	 * rectangles and their enclosed children.
	 *
	 * @return the frame width
	 */
	public double getFrameWidth() {
		return this.m_frame;
	}

	/**
	 * @see prefuse.action.Action#run(double)
	 */
	@Override
	public void run(final double frac) {
		// setup
		final NodeItem root = this.getLayoutRoot();
		final Rectangle2D b = this.getLayoutBounds();
		this.m_r = new Rectangle2D(b.getMinX(), b.getMinY(), b.getWidth() - 1, b.getHeight() - 1);

		// process size values
		this.computeAreas(root);

		// layout root node
		this.setX(root, null, 0);
		this.setY(root, null, 0);
		root.setBounds(0, 0, this.m_r.getWidth(), this.m_r.getHeight());

		// layout the tree
		this.m_r = this.updateArea(root);
		this.layout(root, this.m_r);
	}

	/**
	 * Compute the pixel areas of nodes based on their size values.
	 */
	private void computeAreas(final NodeItem root) {
		int leafCount = 0;

		// ensure area data column exists
		final Graph g = (Graph) this.m_vis.getGroup(this.m_group);
		final TupleSet nodes = g.getNodes();
		nodes.addColumns(AREA_SCHEMA);

		// reset all sizes to zero
		Iterator iter = new TreeNodeIterator(root);
		while (iter.hasNext()) {
			final NodeItem n = (NodeItem) iter.next();
			n.setDouble(AREA, 0);
		}

		// set raw sizes, compute leaf count
		iter = new TreeNodeIterator(root, false);
		while (iter.hasNext()) {
			final NodeItem n = (NodeItem) iter.next();
			double area = 0;
			if (n.getChildCount() == 0) {
				area = n.getSize();
				++leafCount;
			} else if (n.isExpanded()) {
				NodeItem c = (NodeItem) n.getFirstChild();
				for (; c != null; c = (NodeItem) c.getNextSibling()) {
					area += c.getDouble(AREA);
					++leafCount;
				}
			}
			n.setDouble(AREA, area);

		}

		// scale sizes by display area factor
		final Rectangle2D b = this.getLayoutBounds();
		final double area = (b.getWidth() - 1) * (b.getHeight() - 1);
		final double scale = area / root.getDouble(AREA);
		iter = new TreeNodeIterator(root);
		while (iter.hasNext()) {
			final NodeItem n = (NodeItem) iter.next();
			n.setDouble(AREA, n.getDouble(AREA) * scale);
		}
	}

	/**
	 * Compute the tree map layout.
	 */
	private void layout(final NodeItem p, Rectangle2D r) {
		// create sorted list of children
		Iterator childIter = p.children();
		while (childIter.hasNext()) {
			this.m_kids.add(childIter.next());
		}
		Collections.sort(this.m_kids, s_cmp);

		// do squarified layout of siblings
		final double w = Math.min(r.getWidth(), r.getHeight());
		this.squarify(this.m_kids, this.m_row, w, r);
		this.m_kids.clear(); // clear m_kids

		// recurse
		childIter = p.children();
		while (childIter.hasNext()) {
			final NodeItem c = (NodeItem) childIter.next();
			if ((c.getChildCount() > 0) && (c.getDouble(AREA) > 0)) {
				r = this.updateArea(c);
				this.layout(c, r);
			}
		}
	}

	private Rectangle2D updateArea(final NodeItem n) {
		Rectangle2D result;
		final Bounds b = n.getBounds();
		if (this.m_frame == 0.0) {
			// if no framing, simply update bounding rectangle
			result = new Rectangle2D(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());
			return result;
		}

		// compute area loss due to frame
		final double dA = 2 * this.m_frame * ((b.getWidth() + b.getHeight()) - (2 * this.m_frame));
		final double A = n.getDouble(AREA) - dA;

		// compute renormalization factor
		double s = 0;
		Iterator childIter = n.children();
		while (childIter.hasNext()) {
			s += ((NodeItem) childIter.next()).getDouble(AREA);
		}
		final double t = A / s;

		// re-normalize children areas
		childIter = n.children();
		while (childIter.hasNext()) {
			final NodeItem c = (NodeItem) childIter.next();
			c.setDouble(AREA, c.getDouble(AREA) * t);
		}

		// set bounding rectangle and return
		result = new Rectangle2D(b.getMinX() + this.m_frame, b.getMinY() + this.m_frame,
				b.getWidth() - (2 * this.m_frame), b.getHeight() - (2 * this.m_frame));
		return result;
	}

	private void squarify(final List c, final List row, double w, Rectangle2D r) {
		double worst = Double.MAX_VALUE, nworst;
		int len;

		while ((len = c.size()) > 0) {
			// add item to the row list, ignore if negative area
			final VisualItem item = (VisualItem) c.get(len - 1);
			final double a = item.getDouble(AREA);
			if (a <= 0.0) {
				c.remove(len - 1);
				continue;
			}
			row.add(item);

			nworst = this.worst(row, w);
			if (nworst <= worst) {
				c.remove(len - 1);
				worst = nworst;
			} else {
				row.remove(row.size() - 1); // remove the latest addition
				r = this.layoutRow(row, w, r); // layout the current row
				w = Math.min(r.getWidth(), r.getHeight()); // recompute w
				row.clear(); // clear the row
				worst = Double.MAX_VALUE;
			}
		}
		if (row.size() > 0) {
			r = this.layoutRow(row, w, r); // layout the current row
			row.clear(); // clear the row
		}
	}

	private double worst(final List rlist, double w) {
		double rmax = Double.MIN_VALUE, rmin = Double.MAX_VALUE, s = 0.0;
		final Iterator iter = rlist.iterator();
		while (iter.hasNext()) {
			final double r = ((VisualItem) iter.next()).getDouble(AREA);
			rmin = Math.min(rmin, r);
			rmax = Math.max(rmax, r);
			s += r;
		}
		s = s * s;
		w = w * w;
		return Math.max((w * rmax) / s, s / (w * rmin));
	}

	private Rectangle2D layoutRow(final List row, final double w, final Rectangle2D r) {
		double s = 0; // sum of row areas
		Iterator rowIter = row.iterator();
		while (rowIter.hasNext()) {
			s += ((VisualItem) rowIter.next()).getDouble(AREA);
		}
		final double x = r.getMinX(), y = r.getMinY();
		double d = 0;
		final double h = w == 0 ? 0 : s / w;
		final boolean horiz = (w == r.getWidth());

		// set node positions and dimensions
		rowIter = row.iterator();
		while (rowIter.hasNext()) {
			final NodeItem n = (NodeItem) rowIter.next();
			final NodeItem p = (NodeItem) n.getParent();
			if (horiz) {
				this.setX(n, p, x + d);
				this.setY(n, p, y);
			} else {
				this.setX(n, p, x);
				this.setY(n, p, y + d);
			}
			final double nw = n.getDouble(AREA) / h;
			if (horiz) {
				this.setNodeDimensions(n, nw, h);
				d += nw;
			} else {
				this.setNodeDimensions(n, h, nw);
				d += nw;
			}
		}
		// update space available in rectangle r
		Rectangle2D result;
		if (horiz) {
			result = new Rectangle2D(x, y + h, r.getWidth(), r.getHeight() - h);
		} else {
			result = new Rectangle2D(x + h, y, r.getWidth() - h, r.getHeight());
		}
		return result;
	}

	private void setNodeDimensions(final NodeItem n, final double w, final double h) {
		n.setBounds(n.getX(), n.getY(), w, h);
	}

} // end of class SquarifiedTreeMapLayout
