package prefuse.action.layout.graph;

import java.util.Iterator;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import prefuse.action.layout.Layout;
import prefuse.data.Graph;
import prefuse.data.Schema;
import prefuse.data.tuple.TupleSet;
import prefuse.util.PrefuseLib;
import prefuse.util.force.DragForce;
import prefuse.util.force.ForceItem;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.NBodyForce;
import prefuse.util.force.SpringForce;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 * <p>
 * Layout that positions graph elements based on a physics simulation of
 * interacting forces; by default, nodes repel each other, edges act as springs,
 * and drag forces (similar to air resistance) are applied. This algorithm can
 * be run for multiple iterations for a run-once layout computation or
 * repeatedly run in an animated fashion for a dynamic and interactive layout.
 * </p>
 *
 * <p>
 * The running time of this layout algorithm is the greater of O(N log N) and
 * O(E), where N is the number of nodes and E the number of edges. The addition
 * of custom force calculation modules may, however, increase this value.
 * </p>
 *
 * <p>
 * The {@link prefuse.util.force.ForceSimulator} used to drive this layout can
 * be set explicitly, allowing any number of custom force directed layouts to be
 * created through the user's selection of included
 * {@link prefuse.util.force.Force} components. Each node in the layout is
 * mapped to a {@link prefuse.util.force.ForceItem} instance and each edge to a
 * {@link prefuse.util.force.Spring} instance for storing the state of the
 * simulation. See the {@link prefuse.util.force} package for more.
 * </p>
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class ForceDirectedLayout extends Layout {

	private ForceSimulator m_fsim;
	private long m_lasttime = -1L;
	private long m_maxstep = 50L;
	private final boolean m_runonce;
	private int m_iterations = 100;
	private final boolean m_enforceBounds;

	protected transient VisualItem referrer;

	protected String m_nodeGroup;
	protected String m_edgeGroup;

	/**
	 * Create a new ForceDirectedLayout. By default, this layout will not
	 * restrict the layout to the layout bounds and will assume it is being run
	 * in animated (rather than run-once) fashion.
	 *
	 * @param graph
	 *            the data group to layout. Must resolve to a Graph instance.
	 */
	public ForceDirectedLayout(final String graph) {
		this(graph, false, false);
	}

	/**
	 * Create a new ForceDirectedLayout. The layout will assume it is being run
	 * in animated (rather than run-once) fashion.
	 *
	 * @param group
	 *            the data group to layout. Must resolve to a Graph instance.
	 * @param enforceBounds
	 *            indicates whether or not the layout should require that all
	 *            node placements stay within the layout bounds.
	 */
	public ForceDirectedLayout(final String group, final boolean enforceBounds) {
		this(group, enforceBounds, false);
	}

	/**
	 * Create a new ForceDirectedLayout.
	 *
	 * @param group
	 *            the data group to layout. Must resolve to a Graph instance.
	 * @param enforceBounds
	 *            indicates whether or not the layout should require that all
	 *            node placements stay within the layout bounds.
	 * @param runonce
	 *            indicates if the layout will be run in a run-once or animated
	 *            fashion. In run-once mode, the layout will run for a set
	 *            number of iterations when invoked. In animation mode, only one
	 *            iteration of the layout is computed.
	 */
	public ForceDirectedLayout(final String group, final boolean enforceBounds, final boolean runonce) {
		super(group);
		this.m_nodeGroup = PrefuseLib.getGroupName(group, Graph.NODES);
		this.m_edgeGroup = PrefuseLib.getGroupName(group, Graph.EDGES);

		this.m_enforceBounds = enforceBounds;
		this.m_runonce = runonce;
		this.m_fsim = new ForceSimulator();
		this.m_fsim.addForce(new NBodyForce());
		this.m_fsim.addForce(new SpringForce());
		this.m_fsim.addForce(new DragForce());
	}

	/**
	 * Create a new ForceDirectedLayout. The layout will assume it is being run
	 * in animated (rather than run-once) fashion.
	 *
	 * @param group
	 *            the data group to layout. Must resolve to a Graph instance.
	 * @param fsim
	 *            the force simulator used to drive the layout computation
	 * @param enforceBounds
	 *            indicates whether or not the layout should require that all
	 *            node placements stay within the layout bounds.
	 */
	public ForceDirectedLayout(final String group, final ForceSimulator fsim, final boolean enforceBounds) {
		this(group, fsim, enforceBounds, false);
	}

	/**
	 * Create a new ForceDirectedLayout.
	 *
	 * @param group
	 *            the data group to layout. Must resolve to a Graph instance.
	 * @param fsim
	 *            the force simulator used to drive the layout computation
	 * @param enforceBounds
	 *            indicates whether or not the layout should require that all
	 *            node placements stay within the layout bounds.
	 * @param runonce
	 *            indicates if the layout will be run in a run-once or animated
	 *            fashion. In run-once mode, the layout will run for a set
	 *            number of iterations when invoked. In animation mode, only one
	 *            iteration of the layout is computed.
	 */
	public ForceDirectedLayout(final String group, final ForceSimulator fsim, final boolean enforceBounds,
			final boolean runonce) {
		super(group);
		this.m_nodeGroup = PrefuseLib.getGroupName(group, Graph.NODES);
		this.m_edgeGroup = PrefuseLib.getGroupName(group, Graph.EDGES);

		this.m_enforceBounds = enforceBounds;
		this.m_runonce = runonce;
		this.m_fsim = fsim;
	}

	// ------------------------------------------------------------------------

	/**
	 * Get the maximum timestep allowed for integrating node settings between
	 * runs of this layout. When computation times are longer than desired, and
	 * node positions are changing dramatically between animated frames, the max
	 * step time can be lowered to suppress node movement.
	 *
	 * @return the maximum timestep allowed for integrating between two layout
	 *         steps.
	 */
	public long getMaxTimeStep() {
		return this.m_maxstep;
	}

	/**
	 * Set the maximum timestep allowed for integrating node settings between
	 * runs of this layout. When computation times are longer than desired, and
	 * node positions are changing dramatically between animated frames, the max
	 * step time can be lowered to suppress node movement.
	 *
	 * @param maxstep
	 *            the maximum timestep allowed for integrating between two
	 *            layout steps
	 */
	public void setMaxTimeStep(final long maxstep) {
		this.m_maxstep = maxstep;
	}

	/**
	 * Get the force simulator driving this layout.
	 *
	 * @return the force simulator
	 */
	public ForceSimulator getForceSimulator() {
		return this.m_fsim;
	}

	/**
	 * Set the force simulator driving this layout.
	 *
	 * @param fsim
	 *            the force simulator
	 */
	public void setForceSimulator(final ForceSimulator fsim) {
		this.m_fsim = fsim;
	}

	/**
	 * Get the number of iterations to use when computing a layout in run-once
	 * mode.
	 *
	 * @return the number of layout iterations to run
	 */
	public int getIterations() {
		return this.m_iterations;
	}

	/**
	 * Set the number of iterations to use when computing a layout in run-once
	 * mode.
	 *
	 * @param iter
	 *            the number of layout iterations to run
	 */
	public void setIterations(final int iter) {
		if (iter < 1) {
			throw new IllegalArgumentException("Iterations must be a positive number!");
		}
		this.m_iterations = iter;
	}

	/**
	 * Explicitly sets the node and edge groups to use for this layout,
	 * overriding the group setting passed to the constructor.
	 *
	 * @param nodeGroup
	 *            the node data group
	 * @param edgeGroup
	 *            the edge data group
	 */
	public void setDataGroups(final String nodeGroup, final String edgeGroup) {
		this.m_nodeGroup = nodeGroup;
		this.m_edgeGroup = edgeGroup;
	}

	// ------------------------------------------------------------------------

	/**
	 * @see prefuse.action.Action#run(double)
	 */
	@Override
	public void run(final double frac) {
		// perform different actions if this is a run-once or
		// run-continuously layout
		if (this.m_runonce) {
			final Point2D anchor = this.getLayoutAnchor();
			final Iterator iter = this.m_vis.visibleItems(this.m_nodeGroup);
			while (iter.hasNext()) {
				final VisualItem item = (NodeItem) iter.next();
				item.setX(anchor.getX());
				item.setY(anchor.getY());
			}
			this.m_fsim.clear();
			long timestep = 1000L;
			this.initSimulator(this.m_fsim);
			for (int i = 0; i < this.m_iterations; i++) {
				// use an annealing schedule to set time step
				timestep *= (1.0 - (i / (double) this.m_iterations));
				final long step = timestep + 50;
				// run simulator
				this.m_fsim.runSimulator(step);
				// debugging output
				// if (i % 10 == 0 ) {
				// System.out.println("iter: "+i);
				// }
			}
			this.updateNodePositions();
		} else {
			// get timestep
			if (this.m_lasttime == -1) {
				this.m_lasttime = System.currentTimeMillis() - 20;
			}
			final long time = System.currentTimeMillis();
			final long timestep = Math.min(this.m_maxstep, time - this.m_lasttime);
			this.m_lasttime = time;

			// run force simulator
			this.m_fsim.clear();
			this.initSimulator(this.m_fsim);
			this.m_fsim.runSimulator(timestep);
			this.updateNodePositions();
		}
		if (frac == 1.0) {
			this.reset();
		}
	}

	private void updateNodePositions() {
		final Rectangle2D bounds = this.getLayoutBounds();
		double x1 = 0, x2 = 0, y1 = 0, y2 = 0;
		if (bounds != null) {
			x1 = bounds.getMinX();
			y1 = bounds.getMinY();
			x2 = bounds.getMaxX();
			y2 = bounds.getMaxY();
		}

		// update positions
		final Iterator iter = this.m_vis.visibleItems(this.m_nodeGroup);
		while (iter.hasNext()) {
			final VisualItem item = (VisualItem) iter.next();
			final ForceItem fitem = (ForceItem) item.get(FORCEITEM);

			if (item.isFixed()) {
				// clear any force computations
				fitem.force[0] = 0.0f;
				fitem.force[1] = 0.0f;
				fitem.velocity[0] = 0.0f;
				fitem.velocity[1] = 0.0f;

				if (Double.isNaN(item.getX())) {
					this.setX(item, this.referrer, 0.0);
					this.setY(item, this.referrer, 0.0);
				}
				continue;
			}

			double x = fitem.location[0];
			double y = fitem.location[1];

			if (this.m_enforceBounds && (bounds != null)) {
				final Bounds b = item.getBounds();
				final double hw = b.getWidth() / 2;
				final double hh = b.getHeight() / 2;
				if ((x + hw) > x2) {
					x = x2 - hw;
				}
				if ((x - hw) < x1) {
					x = x1 + hw;
				}
				if ((y + hh) > y2) {
					y = y2 - hh;
				}
				if ((y - hh) < y1) {
					y = y1 + hh;
				}
			}

			// set the actual position
			this.setX(item, this.referrer, x);
			this.setY(item, this.referrer, y);
		}
	}

	/**
	 * Reset the force simulation state for all nodes processed by this layout.
	 */
	public void reset() {
		final Iterator iter = this.m_vis.visibleItems(this.m_nodeGroup);
		while (iter.hasNext()) {
			final VisualItem item = (VisualItem) iter.next();
			final ForceItem fitem = (ForceItem) item.get(FORCEITEM);
			if (fitem != null) {
				fitem.location[0] = (float) item.getEndX();
				fitem.location[1] = (float) item.getEndY();
				fitem.force[0] = fitem.force[1] = 0;
				fitem.velocity[0] = fitem.velocity[1] = 0;
			}
		}
		this.m_lasttime = -1L;
	}

	/**
	 * Loads the simulator with all relevant force items and springs.
	 *
	 * @param fsim
	 *            the force simulator driving this layout
	 */
	protected void initSimulator(final ForceSimulator fsim) {
		// make sure we have force items to work with
		final TupleSet ts = this.m_vis.getGroup(this.m_nodeGroup);
		if (ts == null) {
			return;
		}
		try {
			ts.addColumns(FORCEITEM_SCHEMA);
		} catch (final IllegalArgumentException iae) {
			/* ignored */ }

		float startX = (this.referrer == null ? 0f : (float) this.referrer.getX());
		float startY = (this.referrer == null ? 0f : (float) this.referrer.getY());
		startX = Float.isNaN(startX) ? 0f : startX;
		startY = Float.isNaN(startY) ? 0f : startY;

		Iterator iter = this.m_vis.visibleItems(this.m_nodeGroup);
		while (iter.hasNext()) {
			final VisualItem item = (VisualItem) iter.next();
			final ForceItem fitem = (ForceItem) item.get(FORCEITEM);
			fitem.mass = this.getMassValue(item);
			final double x = item.getEndX();
			final double y = item.getEndY();
			fitem.location[0] = (Double.isNaN(x) ? startX : (float) x);
			fitem.location[1] = (Double.isNaN(y) ? startY : (float) y);
			fsim.addItem(fitem);
		}
		if (this.m_edgeGroup != null) {
			iter = this.m_vis.visibleItems(this.m_edgeGroup);
			while (iter.hasNext()) {
				final EdgeItem e = (EdgeItem) iter.next();
				final NodeItem n1 = e.getSourceItem();
				final ForceItem f1 = (ForceItem) n1.get(FORCEITEM);
				final NodeItem n2 = e.getTargetItem();
				final ForceItem f2 = (ForceItem) n2.get(FORCEITEM);
				final float coeff = this.getSpringCoefficient(e);
				final float slen = this.getSpringLength(e);
				fsim.addSpring(f1, f2, (coeff >= 0 ? coeff : -1.f), (slen >= 0 ? slen : -1.f));
			}
		}
	}

	/**
	 * Get the mass value associated with the given node. Subclasses should
	 * override this method to perform custom mass assignment.
	 *
	 * @param n
	 *            the node for which to compute the mass value
	 * @return the mass value for the node. By default, all items are given a
	 *         mass value of 1.0.
	 */
	protected float getMassValue(final VisualItem n) {
		return 1.0f;
	}

	/**
	 * Get the spring length for the given edge. Subclasses should override this
	 * method to perform custom spring length assignment.
	 *
	 * @param e
	 *            the edge for which to compute the spring length
	 * @return the spring length for the edge. A return value of -1 means to
	 *         ignore this method and use the global default.
	 */
	protected float getSpringLength(final EdgeItem e) {
		return -1.f;
	}

	/**
	 * Get the spring coefficient for the given edge, which controls the tension
	 * or strength of the spring. Subclasses should override this method to
	 * perform custom spring tension assignment.
	 *
	 * @param e
	 *            the edge for which to compute the spring coefficient.
	 * @return the spring coefficient for the edge. A return value of -1 means
	 *         to ignore this method and use the global default.
	 */
	protected float getSpringCoefficient(final EdgeItem e) {
		return -1.f;
	}

	/**
	 * Get the referrer item to use to set x or y coordinates that are
	 * initialized to NaN.
	 *
	 * @return the referrer item.
	 * @see prefuse.util.PrefuseLib#setX(VisualItem, VisualItem, double)
	 * @see prefuse.util.PrefuseLib#setY(VisualItem, VisualItem, double)
	 */
	public VisualItem getReferrer() {
		return this.referrer;
	}

	/**
	 * Set the referrer item to use to set x or y coordinates that are
	 * initialized to NaN.
	 *
	 * @param referrer
	 *            the referrer item to use.
	 * @see prefuse.util.PrefuseLib#setX(VisualItem, VisualItem, double)
	 * @see prefuse.util.PrefuseLib#setY(VisualItem, VisualItem, double)
	 */
	public void setReferrer(final VisualItem referrer) {
		this.referrer = referrer;
	}

	// ------------------------------------------------------------------------
	// ForceItem Schema Addition

	/**
	 * The data field in which the parameters used by this layout are stored.
	 */
	public static final String FORCEITEM = "_forceItem";
	/**
	 * The schema for the parameters used by this layout.
	 */
	public static final Schema FORCEITEM_SCHEMA = new Schema();

	static {
		FORCEITEM_SCHEMA.addColumn(FORCEITEM, ForceItem.class, new ForceItem());
	}

} // end of class ForceDirectedLayout
