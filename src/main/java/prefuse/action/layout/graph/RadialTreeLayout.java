package prefuse.action.layout.graph;

import java.util.Iterator;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Schema;
import prefuse.data.tuple.TupleSet;
import prefuse.util.ArrayLib;
import prefuse.util.MathLib;
import prefuse.visual.NodeItem;

/**
 * <p>
 * TreeLayout instance that computes a radial layout, laying out subsequent
 * depth levels of a tree on circles of progressively increasing radius.
 * </p>
 *
 * <p>
 * The algorithm used is that of Ka-Ping Yee, Danyel Fisher, Rachna Dhamija, and
 * Marti Hearst in their research paper
 * <a href="http://citeseer.ist.psu.edu/448292.html">Animated Exploration of
 * Dynamic Graphs with Radial Layout</a>, InfoVis 2001. This algorithm computes
 * a radial layout which factors in possible variation in sizes, and maintains
 * both orientation and ordering constraints to facilitate smooth and
 * understandable transitions between layout configurations.
 * </p>
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class RadialTreeLayout extends TreeLayout {

	public static final int DEFAULT_RADIUS = 50;
	private static final int MARGIN = 30;

	protected int m_maxDepth = 0;
	protected double m_radiusInc;
	protected double m_theta1, m_theta2;
	protected boolean m_setTheta = false;
	protected boolean m_autoScale = true;

	protected Point2D m_origin;
	protected NodeItem m_prevRoot;

	/**
	 * Creates a new RadialTreeLayout. Automatic scaling of the radius values to
	 * fit the layout bounds is enabled by default.
	 *
	 * @param group
	 *            the data group to process. This should resolve to either a
	 *            Graph or Tree instance.
	 */
	public RadialTreeLayout(final String group) {
		super(group);
		this.m_radiusInc = DEFAULT_RADIUS;
		this.m_prevRoot = null;
		this.m_theta1 = 0;
		this.m_theta2 = this.m_theta1 + MathLib.TWO_PI;
	}

	/**
	 * Creates a new RadialTreeLayout using the specified radius increment
	 * between levels of the layout. Automatic scaling of the radius values is
	 * disabled by default.
	 *
	 * @param group
	 *            the data group to process. This should resolve to either a
	 *            Graph or Tree instance.
	 * @param radius
	 *            the radius increment to use between subsequent rings in the
	 *            layout.
	 */
	public RadialTreeLayout(final String group, final int radius) {
		this(group);
		this.m_radiusInc = radius;
		this.m_autoScale = false;
	}

	/**
	 * Set the radius increment to use between concentric circles. Note that
	 * this value is used only if auto-scaling is disabled.
	 *
	 * @return the radius increment between subsequent rings of the layout when
	 *         auto-scaling is disabled
	 */
	public double getRadiusIncrement() {
		return this.m_radiusInc;
	}

	/**
	 * Set the radius increment to use between concentric circles. Note that
	 * this value is used only if auto-scaling is disabled.
	 *
	 * @param inc
	 *            the radius increment between subsequent rings of the layout
	 * @see #setAutoScale(boolean)
	 */
	public void setRadiusIncrement(final double inc) {
		this.m_radiusInc = inc;
	}

	/**
	 * Indicates if the layout automatically scales to fit the layout bounds.
	 *
	 * @return true if auto-scaling is enabled, false otherwise
	 */
	public boolean getAutoScale() {
		return this.m_autoScale;
	}

	/**
	 * Set whether or not the layout should automatically scale itself to fit
	 * the layout bounds.
	 *
	 * @param s
	 *            true to automatically scale to fit display, false otherwise
	 */
	public void setAutoScale(final boolean s) {
		this.m_autoScale = s;
	}

	/**
	 * Constrains this layout to the specified angular sector
	 *
	 * @param theta
	 *            the starting angle, in radians
	 * @param width
	 *            the angular width, in radians
	 */
	public void setAngularBounds(final double theta, final double width) {
		this.m_theta1 = theta;
		this.m_theta2 = theta + width;
		this.m_setTheta = true;
	}

	/**
	 * @see prefuse.action.Action#run(double)
	 */
	@Override
	public void run(final double frac) {
		final Graph g = (Graph) this.m_vis.getGroup(this.m_group);
		this.initSchema(g.getNodes());

		this.m_origin = this.getLayoutAnchor();
		final NodeItem n = this.getLayoutRoot();
		final Params np = (Params) n.get(PARAMS);

		g.getSpanningTree(n);

		// calc relative widths and maximum tree depth
		// performs one pass over the tree
		this.m_maxDepth = 0;
		this.calcAngularWidth(n, 0);

		if (this.m_autoScale) {
			this.setScale(this.getLayoutBounds());
		}
		if (!this.m_setTheta) {
			this.calcAngularBounds(n);
		}

		// perform the layout
		if (this.m_maxDepth > 0) {
			this.layout(n, this.m_radiusInc, this.m_theta1, this.m_theta2);
		}

		// update properties of the root node
		this.setX(n, null, this.m_origin.getX());
		this.setY(n, null, this.m_origin.getY());
		np.angle = this.m_theta2 - this.m_theta1;
	}

	/**
	 * Clears references to graph tuples. The group and visualization are
	 * retained.
	 */
	@Override
	public void reset() {
		super.reset();
		this.m_prevRoot = null;
	}

	protected void setScale(final Rectangle2D bounds) {
		final double r = Math.min(bounds.getWidth(), bounds.getHeight()) / 2.0;
		if (this.m_maxDepth > 0) {
			this.m_radiusInc = (r - MARGIN) / this.m_maxDepth;
		}
	}

	/**
	 * Calculates the angular bounds of the layout, attempting to preserve the
	 * angular orientation of the display across transitions.
	 */
	private void calcAngularBounds(final NodeItem r) {
		if ((this.m_prevRoot == null) || !this.m_prevRoot.isValid() || (r == this.m_prevRoot)) {
			this.m_prevRoot = r;
			return;
		}

		// try to find previous parent of root
		NodeItem p = this.m_prevRoot;
		while (true) {
			final NodeItem pp = (NodeItem) p.getParent();
			if (pp == r) {
				break;
			} else if (pp == null) {
				this.m_prevRoot = r;
				return;
			}
			p = pp;
		}

		// compute offset due to children's angular width
		double dt = 0;
		final Iterator iter = this.sortedChildren(r);
		while (iter.hasNext()) {
			final Node n = (Node) iter.next();
			if (n == p) {
				break;
			}
			dt += ((Params) n.get(PARAMS)).width;
		}
		final double rw = ((Params) r.get(PARAMS)).width;
		final double pw = ((Params) p.get(PARAMS)).width;
		dt = (-MathLib.TWO_PI * (dt + (pw / 2))) / rw;

		// set angular bounds
		this.m_theta1 = dt + Math.atan2(p.getY() - r.getY(), p.getX() - r.getX());
		this.m_theta2 = this.m_theta1 + MathLib.TWO_PI;
		this.m_prevRoot = r;
	}

	/**
	 * Computes relative measures of the angular widths of each expanded
	 * subtree. Node diameters are taken into account to improve space
	 * allocation for variable-sized nodes.
	 *
	 * This method also updates the base angle value for nodes to ensure proper
	 * ordering of nodes.
	 */
	private double calcAngularWidth(final NodeItem n, final int d) {
		if (d > this.m_maxDepth) {
			this.m_maxDepth = d;
		}
		double aw = 0;

		final Bounds bounds = n.getBounds();
		final double w = bounds.getWidth(), h = bounds.getHeight();
		final double diameter = d == 0 ? 0 : Math.sqrt((w * w) + (h * h)) / d;

		if (n.isExpanded() && (n.getChildCount() > 0)) {
			final Iterator childIter = n.children();
			while (childIter.hasNext()) {
				final NodeItem c = (NodeItem) childIter.next();
				aw += this.calcAngularWidth(c, d + 1);
			}
			aw = Math.max(diameter, aw);
		} else {
			aw = diameter;
		}
		((Params) n.get(PARAMS)).width = aw;
		return aw;
	}

	private static final double normalize(double angle) {
		while (angle > MathLib.TWO_PI) {
			angle -= MathLib.TWO_PI;
		}
		while (angle < 0) {
			angle += MathLib.TWO_PI;
		}
		return angle;
	}

	private Iterator sortedChildren(final NodeItem n) {
		double base = 0;
		// update base angle for node ordering
		final NodeItem p = (NodeItem) n.getParent();
		if (p != null) {
			base = normalize(Math.atan2(p.getY() - n.getY(), p.getX() - n.getX()));
		}
		final int cc = n.getChildCount();
		if (cc == 0) {
			return null;
		}

		NodeItem c = (NodeItem) n.getFirstChild();

		// TODO: this is hacky and will break when filtering
		// how to know that a branch is newly expanded?
		// is there an alternative property we should check?
		if (!c.isStartVisible()) {
			// use natural ordering for previously invisible nodes
			return n.children();
		}

		final double angle[] = new double[cc];
		final int idx[] = new int[cc];
		for (int i = 0; i < cc; ++i, c = (NodeItem) c.getNextSibling()) {
			idx[i] = i;
			angle[i] = normalize(-base + Math.atan2(c.getY() - n.getY(), c.getX() - n.getX()));
		}
		ArrayLib.sort(angle, idx);

		// return iterator over sorted children
		return new Iterator() {
			int cur = 0;

			@Override
			public Object next() {
				return n.getChild(idx[this.cur++]);
			}

			@Override
			public boolean hasNext() {
				return this.cur < idx.length;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * Compute the layout.
	 *
	 * @param n
	 *            the root of the current subtree under consideration
	 * @param r
	 *            the radius, current distance from the center
	 * @param theta1
	 *            the start (in radians) of this subtree's angular region
	 * @param theta2
	 *            the end (in radians) of this subtree's angular region
	 */
	protected void layout(final NodeItem n, final double r, final double theta1, final double theta2) {
		final double dtheta = (theta2 - theta1);
		final double dtheta2 = dtheta / 2.0;
		final double width = ((Params) n.get(PARAMS)).width;
		double cfrac, nfrac = 0.0;

		final Iterator childIter = this.sortedChildren(n);
		while ((childIter != null) && childIter.hasNext()) {
			final NodeItem c = (NodeItem) childIter.next();
			final Params cp = (Params) c.get(PARAMS);
			cfrac = cp.width / width;
			if (c.isExpanded() && (c.getChildCount() > 0)) {
				this.layout(c, r + this.m_radiusInc, theta1 + (nfrac * dtheta), theta1 + ((nfrac + cfrac) * dtheta));
			}
			this.setPolarLocation(c, n, r, theta1 + (nfrac * dtheta) + (cfrac * dtheta2));
			cp.angle = cfrac * dtheta;
			nfrac += cfrac;
		}

	}

	/**
	 * Set the position of the given node, given in polar co-ordinates.
	 *
	 * @param n
	 *            the NodeItem to set the position
	 * @param p
	 *            the referrer parent NodeItem
	 * @param r
	 *            the radius
	 * @param t
	 *            the angle theta
	 */
	protected void setPolarLocation(final NodeItem n, final NodeItem p, final double r, final double t) {
		this.setX(n, p, this.m_origin.getX() + (r * Math.cos(t)));
		this.setY(n, p, this.m_origin.getY() + (r * Math.sin(t)));
	}

	// ------------------------------------------------------------------------
	// Params

	/**
	 * The data field in which the parameters used by this layout are stored.
	 */
	public static final String PARAMS = "_radialTreeLayoutParams";
	/**
	 * The schema for the parameters used by this layout.
	 */
	public static final Schema PARAMS_SCHEMA = new Schema();

	static {
		PARAMS_SCHEMA.addColumn(PARAMS, Params.class, new Params());
	}

	protected void initSchema(final TupleSet ts) {
		ts.addColumns(PARAMS_SCHEMA);
	}

	/**
	 * Wrapper class holding parameters used for each node in this layout.
	 */
	public static class Params implements Cloneable {
		double width;
		double angle;

		@Override
		public Object clone() {
			final Params p = new Params();
			p.width = this.width;
			p.angle = this.angle;
			return p;
		}
	}

} // end of class RadialTreeLayout
