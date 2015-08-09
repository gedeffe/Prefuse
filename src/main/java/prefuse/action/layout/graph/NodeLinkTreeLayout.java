package prefuse.action.layout.graph;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import javafx.geometry.Point2D;
import prefuse.Constants;
import prefuse.Display;
import prefuse.data.Graph;
import prefuse.data.Schema;
import prefuse.data.tuple.TupleSet;
import prefuse.util.ArrayLib;
import prefuse.visual.NodeItem;

/**
 * <p>
 * TreeLayout that computes a tidy layout of a node-link tree diagram. This
 * algorithm lays out a rooted tree such that each depth level of the tree is on
 * a shared line. The orientation of the tree can be set such that the tree goes
 * left-to-right (default), right-to-left, top-to-bottom, or bottom-to-top.
 * </p>
 *
 * <p>
 * The algorithm used is that of Christoph Buchheim, Michael Juenger, and
 * Sebastian Leipert from their research paper
 * <a href="http://citeseer.ist.psu.edu/buchheim02improving.html"> Improving
 * Walker's Algorithm to Run in Linear Time</a>, Graph Drawing 2002. This
 * algorithm corrects performance issues in Walker's algorithm, which
 * generalizes Reingold and Tilford's method for tidy drawings of trees to
 * support trees with an arbitrary number of children at any given node.
 * </p>
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class NodeLinkTreeLayout extends TreeLayout {

	private int m_orientation; // the orientation of the tree
	private double m_bspace = 5; // the spacing between sibling nodes
	private double m_tspace = 25; // the spacing between subtrees
	private double m_dspace = 50; // the spacing between depth levels
	private double m_offset = 50; // pixel offset for root node position

	private double[] m_depths = new double[10];
	private int m_maxDepth = 0;

	private double m_ax, m_ay; // for holding anchor co-ordinates

	/**
	 * Create a new NodeLinkTreeLayout. A left-to-right orientation is assumed.
	 *
	 * @param group
	 *            the data group to layout. Must resolve to a Graph instance.
	 */
	public NodeLinkTreeLayout(final String group) {
		super(group);
		this.m_orientation = Constants.ORIENT_LEFT_RIGHT;
	}

	/**
	 * Create a new NodeLinkTreeLayout.
	 *
	 * @param group
	 *            the data group to layout. Must resolve to a Graph instance.
	 * @param orientation
	 *            the orientation of the tree layout. One of
	 *            {@link prefuse.Constants#ORIENT_LEFT_RIGHT},
	 *            {@link prefuse.Constants#ORIENT_RIGHT_LEFT},
	 *            {@link prefuse.Constants#ORIENT_TOP_BOTTOM}, or
	 *            {@link prefuse.Constants#ORIENT_BOTTOM_TOP}.
	 * @param dspace
	 *            the spacing to maintain between depth levels of the tree
	 * @param bspace
	 *            the spacing to maintain between sibling nodes
	 * @param tspace
	 *            the spacing to maintain between neighboring subtrees
	 */
	public NodeLinkTreeLayout(final String group, final int orientation, final double dspace, final double bspace,
			final double tspace) {
		super(group);
		this.m_orientation = orientation;
		this.m_dspace = dspace;
		this.m_bspace = bspace;
		this.m_tspace = tspace;
	}

	// ------------------------------------------------------------------------

	/**
	 * Set the orientation of the tree layout.
	 *
	 * @param orientation
	 *            the orientation value. One of
	 *            {@link prefuse.Constants#ORIENT_LEFT_RIGHT},
	 *            {@link prefuse.Constants#ORIENT_RIGHT_LEFT},
	 *            {@link prefuse.Constants#ORIENT_TOP_BOTTOM}, or
	 *            {@link prefuse.Constants#ORIENT_BOTTOM_TOP}.
	 */
	public void setOrientation(final int orientation) {
		if ((orientation < 0) || (orientation >= Constants.ORIENTATION_COUNT)
				|| (orientation == Constants.ORIENT_CENTER)) {
			throw new IllegalArgumentException("Unsupported orientation value: " + orientation);
		}
		this.m_orientation = orientation;
	}

	/**
	 * Get the orientation of the tree layout.
	 *
	 * @return the orientation value. One of
	 *         {@link prefuse.Constants#ORIENT_LEFT_RIGHT},
	 *         {@link prefuse.Constants#ORIENT_RIGHT_LEFT},
	 *         {@link prefuse.Constants#ORIENT_TOP_BOTTOM}, or
	 *         {@link prefuse.Constants#ORIENT_BOTTOM_TOP}.
	 */
	public int getOrientation() {
		return this.m_orientation;
	}

	/**
	 * Set the spacing between depth levels.
	 *
	 * @param d
	 *            the depth spacing to use
	 */
	public void setDepthSpacing(final double d) {
		this.m_dspace = d;
	}

	/**
	 * Get the spacing between depth levels.
	 *
	 * @return the depth spacing
	 */
	public double getDepthSpacing() {
		return this.m_dspace;
	}

	/**
	 * Set the spacing between neighbor nodes.
	 *
	 * @param b
	 *            the breadth spacing to use
	 */
	public void setBreadthSpacing(final double b) {
		this.m_bspace = b;
	}

	/**
	 * Get the spacing between neighbor nodes.
	 *
	 * @return the breadth spacing
	 */
	public double getBreadthSpacing() {
		return this.m_bspace;
	}

	/**
	 * Set the spacing between neighboring subtrees.
	 *
	 * @param s
	 *            the subtree spacing to use
	 */
	public void setSubtreeSpacing(final double s) {
		this.m_tspace = s;
	}

	/**
	 * Get the spacing between neighboring subtrees.
	 *
	 * @return the subtree spacing
	 */
	public double getSubtreeSpacing() {
		return this.m_tspace;
	}

	/**
	 * Set the offset value for placing the root node of the tree. The dimension
	 * in which this offset is applied is dependent upon the orientation of the
	 * tree. For example, in a left-to-right orientation, the offset will a
	 * horizontal offset from the left edge of the layout bounds.
	 *
	 * @param o
	 *            the value by which to offset the root node of the tree
	 */
	public void setRootNodeOffset(final double o) {
		this.m_offset = o;
	}

	/**
	 * Get the offset value for placing the root node of the tree.
	 *
	 * @return the value by which the root node of the tree is offset
	 */
	public double getRootNodeOffset() {
		return this.m_offset;
	}

	// ------------------------------------------------------------------------

	/**
	 * @see prefuse.action.layout.Layout#getLayoutAnchor()
	 */
	@Override
	public Point2D getLayoutAnchor() {
		if (this.m_anchor != null) {
			return this.m_anchor;
		}

		this.m_tmpa.setLocation(0, 0);
		if (this.m_vis != null) {
			final Display d = this.m_vis.getDisplay(0);
			final Rectangle2D b = this.getLayoutBounds();
			switch (this.m_orientation) {
			case Constants.ORIENT_LEFT_RIGHT:
				this.m_tmpa.setLocation(this.m_offset, d.getHeight() / 2.0);
				break;
			case Constants.ORIENT_RIGHT_LEFT:
				this.m_tmpa.setLocation(b.getMaxX() - this.m_offset, d.getHeight() / 2.0);
				break;
			case Constants.ORIENT_TOP_BOTTOM:
				this.m_tmpa.setLocation(d.getWidth() / 2.0, this.m_offset);
				break;
			case Constants.ORIENT_BOTTOM_TOP:
				this.m_tmpa.setLocation(d.getWidth() / 2.0, b.getMaxY() - this.m_offset);
				break;
			}
			d.getInverseTransform().transform(this.m_tmpa, this.m_tmpa);
		}
		return this.m_tmpa;
	}

	private double spacing(final NodeItem l, final NodeItem r, final boolean siblings) {
		final boolean w = ((this.m_orientation == Constants.ORIENT_TOP_BOTTOM)
				|| (this.m_orientation == Constants.ORIENT_BOTTOM_TOP));
		return (siblings ? this.m_bspace : this.m_tspace)
				+ (0.5 * (w ? l.getBounds().getWidth() + r.getBounds().getWidth()
						: l.getBounds().getHeight() + r.getBounds().getHeight()));
	}

	private void updateDepths(final int depth, final NodeItem item) {
		final boolean v = ((this.m_orientation == Constants.ORIENT_TOP_BOTTOM)
				|| (this.m_orientation == Constants.ORIENT_BOTTOM_TOP));
		final double d = (v ? item.getBounds().getHeight() : item.getBounds().getWidth());
		if (this.m_depths.length <= depth) {
			this.m_depths = ArrayLib.resize(this.m_depths, (3 * depth) / 2);
		}
		this.m_depths[depth] = Math.max(this.m_depths[depth], d);
		this.m_maxDepth = Math.max(this.m_maxDepth, depth);
	}

	private void determineDepths() {
		for (int i = 1; i < this.m_maxDepth; ++i) {
			this.m_depths[i] += this.m_depths[i - 1] + this.m_dspace;
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * @see prefuse.action.Action#run(double)
	 */
	@Override
	public void run(final double frac) {
		final Graph g = (Graph) this.m_vis.getGroup(this.m_group);
		this.initSchema(g.getNodes());

		Arrays.fill(this.m_depths, 0);
		this.m_maxDepth = 0;

		final Point2D a = this.getLayoutAnchor();
		this.m_ax = a.getX();
		this.m_ay = a.getY();

		final NodeItem root = this.getLayoutRoot();
		final Params rp = this.getParams(root);

		g.getSpanningTree(root);

		// do first pass - compute breadth information, collect depth info
		this.firstWalk(root, 0, 1);

		// sum up the depth info
		this.determineDepths();

		// do second pass - assign layout positions
		this.secondWalk(root, null, -rp.prelim, 0);
	}

	private void firstWalk(final NodeItem n, final int num, final int depth) {
		final Params np = this.getParams(n);
		np.number = num;
		this.updateDepths(depth, n);

		final boolean expanded = n.isExpanded();
		if ((n.getChildCount() == 0) || !expanded) // is leaf
		{
			final NodeItem l = (NodeItem) n.getPreviousSibling();
			if (l == null) {
				np.prelim = 0;
			} else {
				np.prelim = this.getParams(l).prelim + this.spacing(l, n, true);
			}
		} else if (expanded) {
			final NodeItem leftMost = (NodeItem) n.getFirstChild();
			final NodeItem rightMost = (NodeItem) n.getLastChild();
			NodeItem defaultAncestor = leftMost;
			NodeItem c = leftMost;
			for (int i = 0; c != null; ++i, c = (NodeItem) c.getNextSibling()) {
				this.firstWalk(c, i, depth + 1);
				defaultAncestor = this.apportion(c, defaultAncestor);
			}

			this.executeShifts(n);

			final double midpoint = 0.5 * (this.getParams(leftMost).prelim + this.getParams(rightMost).prelim);

			final NodeItem left = (NodeItem) n.getPreviousSibling();
			if (left != null) {
				np.prelim = this.getParams(left).prelim + this.spacing(left, n, true);
				np.mod = np.prelim - midpoint;
			} else {
				np.prelim = midpoint;
			}
		}
	}

	private NodeItem apportion(final NodeItem v, NodeItem a) {
		final NodeItem w = (NodeItem) v.getPreviousSibling();
		if (w != null) {
			NodeItem vip, vim, vop, vom;
			double sip, sim, sop, som;

			vip = vop = v;
			vim = w;
			vom = (NodeItem) vip.getParent().getFirstChild();

			sip = this.getParams(vip).mod;
			sop = this.getParams(vop).mod;
			sim = this.getParams(vim).mod;
			som = this.getParams(vom).mod;

			NodeItem nr = this.nextRight(vim);
			NodeItem nl = this.nextLeft(vip);
			while ((nr != null) && (nl != null)) {
				vim = nr;
				vip = nl;
				vom = this.nextLeft(vom);
				vop = this.nextRight(vop);
				this.getParams(vop).ancestor = v;
				final double shift = ((this.getParams(vim).prelim + sim) - (this.getParams(vip).prelim + sip))
						+ this.spacing(vim, vip, false);
				if (shift > 0) {
					this.moveSubtree(this.ancestor(vim, v, a), v, shift);
					sip += shift;
					sop += shift;
				}
				sim += this.getParams(vim).mod;
				sip += this.getParams(vip).mod;
				som += this.getParams(vom).mod;
				sop += this.getParams(vop).mod;

				nr = this.nextRight(vim);
				nl = this.nextLeft(vip);
			}
			if ((nr != null) && (this.nextRight(vop) == null)) {
				final Params vopp = this.getParams(vop);
				vopp.thread = nr;
				vopp.mod += sim - sop;
			}
			if ((nl != null) && (this.nextLeft(vom) == null)) {
				final Params vomp = this.getParams(vom);
				vomp.thread = nl;
				vomp.mod += sip - som;
				a = v;
			}
		}
		return a;
	}

	private NodeItem nextLeft(final NodeItem n) {
		NodeItem c = null;
		if (n.isExpanded()) {
			c = (NodeItem) n.getFirstChild();
		}
		return (c != null ? c : this.getParams(n).thread);
	}

	private NodeItem nextRight(final NodeItem n) {
		NodeItem c = null;
		if (n.isExpanded()) {
			c = (NodeItem) n.getLastChild();
		}
		return (c != null ? c : this.getParams(n).thread);
	}

	private void moveSubtree(final NodeItem wm, final NodeItem wp, final double shift) {
		final Params wmp = this.getParams(wm);
		final Params wpp = this.getParams(wp);
		final double subtrees = wpp.number - wmp.number;
		wpp.change -= shift / subtrees;
		wpp.shift += shift;
		wmp.change += shift / subtrees;
		wpp.prelim += shift;
		wpp.mod += shift;
	}

	private void executeShifts(final NodeItem n) {
		double shift = 0, change = 0;
		for (NodeItem c = (NodeItem) n.getLastChild(); c != null; c = (NodeItem) c.getPreviousSibling()) {
			final Params cp = this.getParams(c);
			cp.prelim += shift;
			cp.mod += shift;
			change += cp.change;
			shift += cp.shift + change;
		}
	}

	private NodeItem ancestor(final NodeItem vim, final NodeItem v, final NodeItem a) {
		final NodeItem p = (NodeItem) v.getParent();
		final Params vimp = this.getParams(vim);
		if (vimp.ancestor.getParent() == p) {
			return vimp.ancestor;
		} else {
			return a;
		}
	}

	private void secondWalk(final NodeItem n, final NodeItem p, final double m, int depth) {
		final Params np = this.getParams(n);
		this.setBreadth(n, p, np.prelim + m);
		this.setDepth(n, p, this.m_depths[depth]);

		if (n.isExpanded()) {
			depth += 1;
			for (NodeItem c = (NodeItem) n.getFirstChild(); c != null; c = (NodeItem) c.getNextSibling()) {
				this.secondWalk(c, n, m + np.mod, depth);
			}
		}

		np.clear();
	}

	private void setBreadth(final NodeItem n, final NodeItem p, final double b) {
		switch (this.m_orientation) {
		case Constants.ORIENT_LEFT_RIGHT:
		case Constants.ORIENT_RIGHT_LEFT:
			this.setY(n, p, this.m_ay + b);
			break;
		case Constants.ORIENT_TOP_BOTTOM:
		case Constants.ORIENT_BOTTOM_TOP:
			this.setX(n, p, this.m_ax + b);
			break;
		default:
			throw new IllegalStateException();
		}
	}

	private void setDepth(final NodeItem n, final NodeItem p, final double d) {
		switch (this.m_orientation) {
		case Constants.ORIENT_LEFT_RIGHT:
			this.setX(n, p, this.m_ax + d);
			break;
		case Constants.ORIENT_RIGHT_LEFT:
			this.setX(n, p, this.m_ax - d);
			break;
		case Constants.ORIENT_TOP_BOTTOM:
			this.setY(n, p, this.m_ay + d);
			break;
		case Constants.ORIENT_BOTTOM_TOP:
			this.setY(n, p, this.m_ay - d);
			break;
		default:
			throw new IllegalStateException();
		}
	}

	// ------------------------------------------------------------------------
	// Params Schema

	/**
	 * The data field in which the parameters used by this layout are stored.
	 */
	public static final String PARAMS = "_reingoldTilfordParams";
	/**
	 * The schema for the parameters used by this layout.
	 */
	public static final Schema PARAMS_SCHEMA = new Schema();

	static {
		PARAMS_SCHEMA.addColumn(PARAMS, Params.class);
	}

	protected void initSchema(final TupleSet ts) {
		ts.addColumns(PARAMS_SCHEMA);
	}

	private Params getParams(final NodeItem item) {
		Params rp = (Params) item.get(PARAMS);
		if (rp == null) {
			rp = new Params();
			item.set(PARAMS, rp);
		}
		if (rp.number == -2) {
			rp.init(item);
		}
		return rp;
	}

	/**
	 * Wrapper class holding parameters used for each node in this layout.
	 */
	public static class Params implements Cloneable {
		double prelim;
		double mod;
		double shift;
		double change;
		int number = -2;
		NodeItem ancestor = null;
		NodeItem thread = null;

		public void init(final NodeItem item) {
			this.ancestor = item;
			this.number = -1;
		}

		public void clear() {
			this.number = -2;
			this.prelim = this.mod = this.shift = this.change = 0;
			this.ancestor = this.thread = null;
		}
	}

} // end of class NodeLinkTreeLayout
