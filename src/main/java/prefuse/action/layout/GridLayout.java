package prefuse.action.layout;

import java.util.Iterator;

import javafx.geometry.Rectangle2D;
import prefuse.data.Node;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.VisualItem;

/**
 * Implements a uniform grid-based layout. This component can either use preset
 * grid dimensions or analyze a grid-shaped graph to determine them
 * automatically.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class GridLayout extends Layout {

	protected int rows;
	protected int cols;
	protected boolean analyze = false;

	/**
	 * Create a new GridLayout without preset dimensions. The layout will
	 * attempt to analyze an input graph to determine grid parameters.
	 * 
	 * @param group
	 *            the data group to layout. In this automatic grid analysis
	 *            configuration, the group <b>must</b> resolve to a set of graph
	 *            nodes.
	 */
	public GridLayout(final String group) {
		super(group);
		this.analyze = true;
	}

	/**
	 * Create a new GridLayout using the specified grid dimensions. If the input
	 * data has more elements than the grid dimensions can hold, the left over
	 * elements will not be visible.
	 * 
	 * @param group
	 *            the data group to layout
	 * @param nrows
	 *            the number of rows of the grid
	 * @param ncols
	 *            the number of columns of the grid
	 */
	public GridLayout(final String group, final int nrows, final int ncols) {
		super(group);
		this.rows = nrows;
		this.cols = ncols;
		this.analyze = false;
	}

	/**
	 * @see prefuse.action.Action#run(double)
	 */
	@Override
	public void run(final double frac) {
		final Rectangle2D b = this.getLayoutBounds();
		final double bx = b.getMinX(), by = b.getMinY();
		final double w = b.getWidth(), h = b.getHeight();

		final TupleSet ts = this.m_vis.getGroup(this.m_group);
		int m = this.rows, n = this.cols;
		if (this.analyze) {
			final int[] d = analyzeGraphGrid(ts);
			m = d[0];
			n = d[1];
		}

		final Iterator iter = ts.tuples();
		// layout grid contents
		for (int i = 0; iter.hasNext() && (i < (m * n)); ++i) {
			final VisualItem item = (VisualItem) iter.next();
			item.setVisible(true);
			final double x = bx + (w * ((i % n) / (double) (n - 1)));
			final double y = by + (h * ((i / n) / (double) (m - 1)));
			this.setX(item, null, x);
			this.setY(item, null, y);
		}
		// set left-overs invisible
		while (iter.hasNext()) {
			final VisualItem item = (VisualItem) iter.next();
			item.setVisible(false);
		}
	}

	/**
	 * Analyzes a set of nodes to try and determine grid dimensions. Currently
	 * looks for the edge count on a node to drop to 2 to determine the end of a
	 * row.
	 * 
	 * @param ts
	 *            TupleSet ts a set of nodes to analyze. Contained tuples
	 *            <b>must</b> implement be Node instances.
	 * @return a two-element int array with the row and column lengths
	 */
	public static int[] analyzeGraphGrid(final TupleSet ts) {
		// TODO: more robust grid analysis?
		int m, n;
		final Iterator iter = ts.tuples();
		iter.next();
		for (n = 2; iter.hasNext(); n++) {
			final Node nd = (Node) iter.next();
			if (nd.getDegree() == 2) {
				break;
			}
		}
		m = ts.getTupleCount() / n;
		return new int[] { m, n };
	}

	/**
	 * Get the number of grid columns.
	 * 
	 * @return the number of grid columns
	 */
	public int getNumCols() {
		return this.cols;
	}

	/**
	 * Set the number of grid columns.
	 * 
	 * @param cols
	 *            the number of grid columns to use
	 */
	public void setNumCols(final int cols) {
		this.cols = cols;
	}

	/**
	 * Get the number of grid rows.
	 * 
	 * @return the number of grid rows
	 */
	public int getNumRows() {
		return this.rows;
	}

	/**
	 * Set the number of grid rows.
	 * 
	 * @param rows
	 *            the number of grid rows to use
	 */
	public void setNumRows(final int rows) {
		this.rows = rows;
	}

} // end of class GridLayout
