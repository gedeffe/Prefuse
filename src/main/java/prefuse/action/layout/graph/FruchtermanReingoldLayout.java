package prefuse.action.layout.graph;

import java.util.Iterator;
import java.util.Random;

import javafx.geometry.Rectangle2D;
import prefuse.action.layout.Layout;
import prefuse.data.Graph;
import prefuse.data.Schema;
import prefuse.data.tuple.TupleSet;
import prefuse.util.PrefuseLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 * <p>
 * Layout instance implementing the Fruchterman-Reingold algorithm for
 * force-directed placement of graph nodes. The computational complexity of this
 * algorithm is quadratic [O(n^2)] in the number of nodes, so should only be
 * applied for relatively small graphs, particularly in interactive situations.
 * </p>
 *
 * <p>
 * This implementation was ported from the implementation in the
 * <a href="http://jung.sourceforge.net/">JUNG</a> framework.
 * </p>
 *
 * @author Scott White, Yan-Biao Boey, Danyel Fisher
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class FruchtermanReingoldLayout extends Layout {

	private double forceConstant;
	private double temp;
	private int maxIter = 700;

	protected String m_nodeGroup;
	protected String m_edgeGroup;
	protected int m_fidx;

	private static final double EPSILON = 0.000001D;
	private static final double ALPHA = 0.1;

	/**
	 * Create a new FruchtermanReingoldLayout.
	 *
	 * @param graph
	 *            the data field to layout. Must resolve to a Graph instance.
	 */
	public FruchtermanReingoldLayout(final String graph) {
		this(graph, 700);
	}

	/**
	 * Create a new FruchtermanReingoldLayout
	 *
	 * @param graph
	 *            the data field to layout. Must resolve to a Graph instance.
	 * @param maxIter
	 *            the maximum number of iterations of the algorithm to run
	 */
	public FruchtermanReingoldLayout(final String graph, final int maxIter) {
		super(graph);
		this.m_nodeGroup = PrefuseLib.getGroupName(graph, Graph.NODES);
		this.m_edgeGroup = PrefuseLib.getGroupName(graph, Graph.EDGES);
		this.maxIter = maxIter;
	}

	/**
	 * Get the maximum number of iterations to run of this algorithm.
	 *
	 * @return the maximum number of iterations
	 */
	public int getMaxIterations() {
		return this.maxIter;
	}

	/**
	 * Set the maximum number of iterations to run of this algorithm.
	 *
	 * @param maxIter
	 *            the maximum number of iterations to use
	 */
	public void setMaxIterations(final int maxIter) {
		this.maxIter = maxIter;
	}

	/**
	 * @see prefuse.action.Action#run(double)
	 */
	@Override
	public void run(final double frac) {
		final Graph g = (Graph) this.m_vis.getGroup(this.m_group);
		final Rectangle2D bounds = super.getLayoutBounds();
		this.init(g, bounds);

		for (int curIter = 0; curIter < this.maxIter; curIter++) {

			// Calculate repulsion
			for (final Iterator iter = g.nodes(); iter.hasNext();) {
				final NodeItem n = (NodeItem) iter.next();
				if (n.isFixed()) {
					continue;
				}
				this.calcRepulsion(g, n);
			}

			// Calculate attraction
			for (final Iterator iter = g.edges(); iter.hasNext();) {
				final EdgeItem e = (EdgeItem) iter.next();
				this.calcAttraction(e);
			}

			for (final Iterator iter = g.nodes(); iter.hasNext();) {
				final NodeItem n = (NodeItem) iter.next();
				if (n.isFixed()) {
					continue;
				}
				this.calcPositions(n, bounds);
			}

			this.cool(curIter);
		}

		this.finish(g);
	}

	private void init(final Graph g, final Rectangle2D b) {
		this.initSchema(g.getNodes());

		this.temp = b.getWidth() / 10;
		this.forceConstant = 0.75 * Math.sqrt((b.getHeight() * b.getWidth()) / g.getNodeCount());

		// initialize node positions
		final Iterator nodeIter = g.nodes();
		final Random rand = new Random(42); // get a deterministic layout result
		final double scaleW = (ALPHA * b.getWidth()) / 2;
		final double scaleH = (ALPHA * b.getHeight()) / 2;
		while (nodeIter.hasNext()) {
			final NodeItem n = (NodeItem) nodeIter.next();
			final Params np = this.getParams(n);
			np.loc[0] = ((b.getMinX() + b.getWidth()) / 2) + (rand.nextDouble() * scaleW);
			np.loc[1] = ((b.getMinY() + b.getHeight()) / 2) + (rand.nextDouble() * scaleH);
		}
	}

	private void finish(final Graph g) {
		final Iterator nodeIter = g.nodes();
		while (nodeIter.hasNext()) {
			final NodeItem n = (NodeItem) nodeIter.next();
			final Params np = this.getParams(n);
			this.setX(n, null, np.loc[0]);
			this.setY(n, null, np.loc[1]);
		}
	}

	public void calcPositions(final NodeItem n, final Rectangle2D b) {
		final Params np = this.getParams(n);
		final double deltaLength = Math.max(EPSILON, Math.sqrt((np.disp[0] * np.disp[0]) + (np.disp[1] * np.disp[1])));

		final double xDisp = (np.disp[0] / deltaLength) * Math.min(deltaLength, this.temp);

		if (Double.isNaN(xDisp)) {
			System.err.println("Mathematical error... (calcPositions:xDisp)");
		}

		final double yDisp = (np.disp[1] / deltaLength) * Math.min(deltaLength, this.temp);

		np.loc[0] += xDisp;
		np.loc[1] += yDisp;

		// don't let nodes leave the display
		final double borderWidth = b.getWidth() / 50.0;
		double x = np.loc[0];
		if (x < (b.getMinX() + borderWidth)) {
			x = b.getMinX() + borderWidth + (Math.random() * borderWidth * 2.0);
		} else if (x > (b.getMaxX() - borderWidth)) {
			x = b.getMaxX() - borderWidth - (Math.random() * borderWidth * 2.0);
		}

		double y = np.loc[1];
		if (y < (b.getMinY() + borderWidth)) {
			y = b.getMinY() + borderWidth + (Math.random() * borderWidth * 2.0);
		} else if (y > (b.getMaxY() - borderWidth)) {
			y = b.getMaxY() - borderWidth - (Math.random() * borderWidth * 2.0);
		}

		np.loc[0] = x;
		np.loc[1] = y;
	}

	public void calcAttraction(final EdgeItem e) {
		final NodeItem n1 = e.getSourceItem();
		final Params n1p = this.getParams(n1);
		final NodeItem n2 = e.getTargetItem();
		final Params n2p = this.getParams(n2);

		final double xDelta = n1p.loc[0] - n2p.loc[0];
		final double yDelta = n1p.loc[1] - n2p.loc[1];

		final double deltaLength = Math.max(EPSILON, Math.sqrt((xDelta * xDelta) + (yDelta * yDelta)));
		final double force = (deltaLength * deltaLength) / this.forceConstant;

		if (Double.isNaN(force)) {
			System.err.println("Mathematical error...");
		}

		final double xDisp = (xDelta / deltaLength) * force;
		final double yDisp = (yDelta / deltaLength) * force;

		n1p.disp[0] -= xDisp;
		n1p.disp[1] -= yDisp;
		n2p.disp[0] += xDisp;
		n2p.disp[1] += yDisp;
	}

	public void calcRepulsion(final Graph g, final NodeItem n1) {
		final Params np = this.getParams(n1);
		np.disp[0] = 0.0;
		np.disp[1] = 0.0;

		for (final Iterator iter2 = g.nodes(); iter2.hasNext();) {
			final NodeItem n2 = (NodeItem) iter2.next();
			final Params n2p = this.getParams(n2);
			if (n2.isFixed()) {
				continue;
			}
			if (n1 != n2) {
				final double xDelta = np.loc[0] - n2p.loc[0];
				final double yDelta = np.loc[1] - n2p.loc[1];

				final double deltaLength = Math.max(EPSILON, Math.sqrt((xDelta * xDelta) + (yDelta * yDelta)));

				final double force = (this.forceConstant * this.forceConstant) / deltaLength;

				if (Double.isNaN(force)) {
					System.err.println("Mathematical error...");
				}

				np.disp[0] += (xDelta / deltaLength) * force;
				np.disp[1] += (yDelta / deltaLength) * force;
			}
		}
	}

	private void cool(final int curIter) {
		this.temp *= (1.0 - (curIter / (double) this.maxIter));
	}

	// ------------------------------------------------------------------------
	// Params Schema

	/**
	 * The data field in which the parameters used by this layout are stored.
	 */
	public static final String PARAMS = "_fruchtermanReingoldParams";
	/**
	 * The schema for the parameters used by this layout.
	 */
	public static final Schema PARAMS_SCHEMA = new Schema();

	static {
		PARAMS_SCHEMA.addColumn(PARAMS, Params.class);
	}

	protected void initSchema(final TupleSet ts) {
		try {
			ts.addColumns(PARAMS_SCHEMA);
		} catch (final IllegalArgumentException iae) {
		}
		;
	}

	private Params getParams(final VisualItem item) {
		Params rp = (Params) item.get(PARAMS);
		if (rp == null) {
			rp = new Params();
			item.set(PARAMS, rp);
		}
		return rp;
	}

	/**
	 * Wrapper class holding parameters used for each node in this layout.
	 */
	public static class Params implements Cloneable {
		double[] loc = new double[2];
		double[] disp = new double[2];
	}

} // end of class FruchtermanReingoldLayout
