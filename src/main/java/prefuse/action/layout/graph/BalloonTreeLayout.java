package prefuse.action.layout.graph;

import java.util.Iterator;

import prefuse.data.Graph;
import prefuse.data.Schema;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.NodeItem;

/**
 * <p>
 * Layout that computes a circular "balloon-tree" layout of a tree. This layout
 * places children nodes radially around their parents, and is equivalent to a
 * top-down flattened view of a ConeTree.
 * </p>
 *
 * <p>
 * The algorithm used is that of G. Melan\c{c}on and I. Herman from their
 * research paper Circular Drawings of Rooted Trees, Reports of the Centre for
 * Mathematics and Computer Sciences, Report Number INS-9817, 1998.
 * </p>
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class BalloonTreeLayout extends TreeLayout {

	private int m_minRadius = 2;

	/**
	 * Create a new BalloonTreeLayout
	 * 
	 * @param group
	 *            the data group to layout. Must resolve to a Graph or Tree
	 *            instance.
	 */
	public BalloonTreeLayout(final String group) {
		this(group, 2);
	}

	/**
	 * Create a new BalloonTreeLayout
	 * 
	 * @param group
	 *            the data group to layout. Must resolve to a Graph or Tree
	 *            instance.
	 * @param minRadius
	 *            the minimum radius to use for a layout circle
	 */
	public BalloonTreeLayout(final String group, final int minRadius) {
		super(group);
		this.m_minRadius = minRadius;
	}

	/**
	 * Get the minimum radius used for a layout circle.
	 * 
	 * @return the minimum layout radius
	 */
	public int getMinRadius() {
		return this.m_minRadius;
	}

	/**
	 * Set the minimum radius used for a layout circle.
	 * 
	 * @param minRadius
	 *            the minimum layout radius
	 */
	public void setMinRadius(final int minRadius) {
		this.m_minRadius = minRadius;
	}

	/**
	 * @see prefuse.action.Action#run(double)
	 */
	@Override
	public void run(final double frac) {
		final Graph g = (Graph) this.m_vis.getGroup(this.m_group);
		this.initSchema(g.getNodes());

		final javafx.geometry.Point2D anchor = this.getLayoutAnchor();
		final NodeItem n = this.getLayoutRoot();
		this.layout(n, anchor.getX(), anchor.getY());
	}

	private void layout(final NodeItem n, final double x, final double y) {
		this.firstWalk(n);
		this.secondWalk(n, null, x, y, 1, 0);
	}

	private void firstWalk(final NodeItem n) {
		final Params np = this.getParams(n);
		np.d = 0;
		double s = 0;
		final Iterator childIter = n.children();
		while (childIter.hasNext()) {
			final NodeItem c = (NodeItem) childIter.next();
			if (!c.isVisible()) {
				continue;
			}
			this.firstWalk(c);
			final Params cp = this.getParams(c);
			np.d = Math.max(np.d, cp.r);
			cp.a = Math.atan(((double) cp.r) / (np.d + cp.r));
			s += cp.a;
		}
		this.adjustChildren(np, s);
		this.setRadius(np);
	}

	private void adjustChildren(final Params np, final double s) {
		if (s > Math.PI) {
			np.c = Math.PI / s;
			np.f = 0;
		} else {
			np.c = 1;
			np.f = Math.PI - s;
		}
	}

	private void setRadius(final Params np) {
		np.r = Math.max(np.d, this.m_minRadius) + (2 * np.d);
	}

	/*
	 * private void setRadius(NodeItem n, ParamBlock np) { int numChildren =
	 * n.getChildCount(); double p = Math.PI; double fs = (numChildren==0 ? 0 :
	 * np.f/numChildren); double pr = 0; double bx = 0, by = 0; Iterator
	 * childIter = n.getChildren(); while ( childIter.hasNext() ) { NodeItem c =
	 * (NodeItem)childIter.next(); ParamBlock cp = getParams(c); p += pr + cp.a
	 * + fs; bx += (cp.r)*Math.cos(p); by += (cp.r)*Math.sin(p); pr = cp.a; } if
	 * ( numChildren != 0 ) { bx /= numChildren; by /= numChildren; } np.rx =
	 * -bx; np.ry = -by;
	 * 
	 * p = Math.PI; pr = 0; np.r = 0; childIter = n.getChildren(); while (
	 * childIter.hasNext() ) { NodeItem c = (NodeItem)childIter.next();
	 * ParamBlock cp = getParams(c); p += pr + cp.a + fs; double x =
	 * cp.r*Math.cos(p)-bx; double y = cp.r*Math.sin(p)-by; double d =
	 * Math.sqrt(x*x+y*y) + cp.r; np.r = Math.max(np.r, (int)Math.round(d)); pr
	 * = cp.a; } if ( np.r == 0 ) np.r = m_minRadius + 2*np.d; } //
	 * 
	 * private void secondWalk2(NodeItem n, NodeItem r, double x, double y,
	 * double l, double t) { ParamBlock np = getParams(n); double cost =
	 * Math.cos(t); double sint = Math.sin(t); double nx = x +
	 * l*(np.rx*cost-np.ry*sint); double ny = y + l*(np.rx*sint+np.ry*cost);
	 * setLocation(n,r,nx,ny); double dd = l*np.d; double p = Math.PI; double fs
	 * = np.f / (n.getChildCount()+1); double pr = 0; Iterator childIter =
	 * n.getChildren(); while ( childIter.hasNext() ) { NodeItem c =
	 * (NodeItem)childIter.next(); ParamBlock cp = getParams(c); double aa =
	 * np.c * cp.a; double rr = np.d * Math.tan(aa)/(1-Math.tan(aa)); p += pr +
	 * aa + fs; double xx = (l*rr+dd)*Math.cos(p)+np.rx; double yy =
	 * (l*rr+dd)*Math.sin(p)+np.ry; double x2 = xx*cost - yy*sint; double y2 =
	 * xx*sint + yy*cost; pr = aa; secondWalk2(c, n, x+x2, y+y2, l*rr/cp.r, p);
	 * } } //
	 */

	private void secondWalk(final NodeItem n, final NodeItem r, final double x, final double y, final double l,
			final double t) {
		this.setX(n, r, x);
		this.setY(n, r, y);

		final Params np = this.getParams(n);
		int numChildren = 0;
		Iterator childIter = n.children();
		while (childIter.hasNext()) {
			final NodeItem c = (NodeItem) childIter.next();
			if (c.isVisible()) {
				++numChildren;
			}
		}
		final double dd = l * np.d;
		double p = t + Math.PI;
		final double fs = (numChildren == 0 ? 0 : np.f / numChildren);
		double pr = 0;
		childIter = n.children();
		while (childIter.hasNext()) {
			final NodeItem c = (NodeItem) childIter.next();
			if (!c.isVisible()) {
				continue;
			}
			final Params cp = this.getParams(c);
			final double aa = np.c * cp.a;
			final double rr = (np.d * Math.tan(aa)) / (1 - Math.tan(aa));
			p += pr + aa + fs;
			final double xx = ((l * rr) + dd) * Math.cos(p);
			final double yy = ((l * rr) + dd) * Math.sin(p);
			pr = aa;
			this.secondWalk(c, n, x + xx, y + yy, l * np.c/* l*rr/cp.r */, p);
		}
	}

	// ------------------------------------------------------------------------
	// Parameters

	/**
	 * The data field in which the parameters used by this layout are stored.
	 */
	public static final String PARAMS = "_balloonTreeLayoutParams";
	/**
	 * The schema for the parameters used by this layout.
	 */
	public static final Schema PARAMS_SCHEMA = new Schema();

	static {
		PARAMS_SCHEMA.addColumn(PARAMS, Params.class);
	}

	private void initSchema(final TupleSet ts) {
		try {
			ts.addColumns(PARAMS_SCHEMA);
		} catch (final IllegalArgumentException iae) {
		}
	}

	private Params getParams(final NodeItem n) {
		Params np = (Params) n.get(PARAMS);
		if (np == null) {
			np = new Params();
			n.set(PARAMS, np);
		}
		return np;
	}

	/**
	 * Wrapper class holding parameters used for each node in this layout.
	 */
	public static class Params {
		public int d;
		public int r;
		public double rx, ry;
		public double a;
		public double c;
		public double f;
	}

} // end of class BalloonTreeLayout
