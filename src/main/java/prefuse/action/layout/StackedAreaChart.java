package prefuse.action.layout;

import java.util.Arrays;
import java.util.Iterator;

import javafx.geometry.Rectangle2D;
import prefuse.Constants;
import prefuse.data.Table;
import prefuse.data.query.NumberRangeModel;
import prefuse.util.ArrayLib;
import prefuse.util.MathLib;
import prefuse.util.PrefuseLib;
import prefuse.util.ui.ValuedRangeModel;
import prefuse.visual.VisualItem;

/**
 * Layout Action that computes a stacked area chart, in which a series of data
 * values are consecutively stacked on top of each other.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class StackedAreaChart extends Layout {

	private final String m_field;
	private final String m_start;
	private final String m_end;

	private String[] columns;
	private final double[] baseline;
	private final double[] peaks;
	private final float[] poly;
	private double m_padding = 0.05;
	private float m_threshold;
	private Rectangle2D bounds;

	private int m_orientation = Constants.ORIENT_BOTTOM_TOP;
	private boolean m_horiz = false;
	private boolean m_top = false;

	private boolean m_norm = false;
	private final NumberRangeModel m_model;

	/**
	 * Create a new StackedAreaChart.
	 * 
	 * @param group
	 *            the data group to layout
	 * @param field
	 *            the data field in which to store computed polygons
	 * @param columns
	 *            the various data fields, in sorted order, that should be
	 *            referenced for each consecutive point of a stack layer
	 */
	public StackedAreaChart(final String group, final String field, final String[] columns) {
		this(group, field, columns, 1.0);
	}

	/**
	 * Create a new StackedAreaChart.
	 * 
	 * @param group
	 *            the data group to layout
	 * @param field
	 *            the data field in which to store computed polygons
	 * @param columns
	 *            the various data fields, in sorted order, that should be
	 *            referenced for each consecutive point of a stack layer
	 * @param threshold
	 *            height threshold under which stacks should not be made
	 *            visible.
	 */
	public StackedAreaChart(final String group, final String field, final String[] columns, final double threshold) {
		super(group);
		this.columns = columns;
		this.baseline = new double[columns.length];
		this.peaks = new double[columns.length];
		this.poly = new float[4 * columns.length];

		this.m_field = field;
		this.m_start = PrefuseLib.getStartField(field);
		this.m_end = PrefuseLib.getEndField(field);
		this.setThreshold(threshold);

		this.m_model = new NumberRangeModel(0, 1, 0, 1);
	}

	// ------------------------------------------------------------------------

	/**
	 * Set the data columns used to compute the stacked layout
	 * 
	 * @param cols
	 *            the various data fields, in sorted order, that should be
	 *            referenced for each consecutive point of a stack layer
	 */
	public void setColumns(final String[] cols) {
		this.columns = cols;
	}

	/**
	 * Sets if the stacks are normalized, such that each column is independently
	 * scaled.
	 * 
	 * @param b
	 *            true to normalize, false otherwise
	 */
	public void setNormalized(final boolean b) {
		this.m_norm = b;
	}

	/**
	 * Indicates if the stacks are normalized, such that each column is
	 * independently scaled.
	 * 
	 * @return true if normalized, false otherwise
	 */
	public boolean isNormalized() {
		return this.m_norm;
	}

	/**
	 * Gets the percentage of the layout bounds that should be reserved for
	 * empty space at the top of the stack.
	 * 
	 * @return the padding percentage
	 */
	public double getPaddingPercentage() {
		return this.m_padding;
	}

	/**
	 * Sets the percentage of the layout bounds that should be reserved for
	 * empty space at the top of the stack.
	 * 
	 * @param p
	 *            the padding percentage to use
	 */
	public void setPaddingPercentage(final double p) {
		if ((p < 0) || (p > 1)) {
			throw new IllegalArgumentException("Illegal padding percentage: " + p);
		}
		this.m_padding = p;
	}

	/**
	 * Get the minimum height threshold under which stacks should not be made
	 * visible.
	 * 
	 * @return the minimum height threshold for visibility
	 */
	public double getThreshold() {
		return this.m_threshold;
	}

	/**
	 * Set the minimum height threshold under which stacks should not be made
	 * visible.
	 * 
	 * @param threshold
	 *            the minimum height threshold for visibility to use
	 */
	public void setThreshold(final double threshold) {
		this.m_threshold = (float) threshold;
	}

	/**
	 * Get the range model describing the range occupied by the value stack.
	 * 
	 * @return the stack range model
	 */
	public ValuedRangeModel getRangeModel() {
		return this.m_model;
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

	// TODO: support externally driven range specification (i.e. stack zooming)
	// public void setRangeModel(NumberRangeModel model) {
	// m_model = model;
	// }

	// ------------------------------------------------------------------------

	/**
	 * @see prefuse.action.Action#run(double)
	 */
	@Override
	public void run(final double frac) {
		this.bounds = this.getLayoutBounds();
		Arrays.fill(this.baseline, 0);

		// get the orientation specifics sorted out
		final float min = (float) (this.m_horiz ? this.bounds.getMaxY() : this.bounds.getMinX());
		final float hgt = (float) (this.m_horiz ? this.bounds.getWidth() : this.bounds.getHeight());
		final int xbias = (this.m_horiz ? 1 : 0);
		final int ybias = (this.m_horiz ? 0 : 1);
		final int mult = this.m_top ? 1 : -1;
		float inc = (float) (this.m_horiz ? (this.bounds.getMinY() - this.bounds.getMaxY())
				: (this.bounds.getMaxX() - this.bounds.getMinX()));
		inc /= this.columns.length - 1;
		final int len = this.columns.length;

		// perform first walk to compute max values
		final double maxValue = this.getPeaks();
		final float b = (float) (this.m_horiz ? (this.m_top ? this.bounds.getMinX() : this.bounds.getMaxX())
				: (this.m_top ? this.bounds.getMinY() : this.bounds.getMaxY()));
		Arrays.fill(this.baseline, b);

		this.m_model.setValueRange(0, maxValue, 0, maxValue);

		// perform second walk to compute polygon layout
		final Table t = (Table) this.m_vis.getGroup(this.m_group);
		final Iterator iter = t.tuplesReversed();
		while (iter.hasNext()) {
			final VisualItem item = (VisualItem) iter.next();
			if (!item.isVisible()) {
				continue;
			}

			float height = 0;

			for (int i = len; --i >= 0;) {
				this.poly[(2 * (len - 1 - i)) + xbias] = min + (i * inc);
				this.poly[(2 * (len - 1 - i)) + ybias] = (float) this.baseline[i];
			}
			for (int i = 0; i < this.columns.length; ++i) {
				final int base = 2 * (len + i);
				final double value = item.getDouble(this.columns[i]);
				this.baseline[i] += mult * hgt * MathLib.linearInterp(value, 0, this.peaks[i]);
				this.poly[base + xbias] = min + (i * inc);
				this.poly[base + ybias] = (float) this.baseline[i];
				height = Math.max(height, Math.abs(this.poly[(2 * (len - 1 - i)) + ybias] - this.poly[base + ybias]));
			}
			if (height < this.m_threshold) {
				item.setVisible(false);
			}

			this.setX(item, null, 0);
			this.setY(item, null, 0);
			this.setPolygon(item, this.poly);
		}
	}

	private double getPeaks() {
		double sum = 0;

		// first, compute max value of the current data
		Arrays.fill(this.peaks, 0);
		final Iterator iter = this.m_vis.visibleItems(this.m_group);
		while (iter.hasNext()) {
			final VisualItem item = (VisualItem) iter.next();
			for (int i = 0; i < this.columns.length; ++i) {
				final double val = item.getDouble(this.columns[i]);
				this.peaks[i] += val;
				sum += val;
			}
		}
		double max = ArrayLib.max(this.peaks);

		// update peaks array as needed
		if (!this.m_norm) {
			Arrays.fill(this.peaks, max);
		}

		// adjust peaks to include padding space
		if (!this.m_norm) {
			for (int i = 0; i < this.peaks.length; ++i) {
				this.peaks[i] += this.m_padding * this.peaks[i];
			}
			max += this.m_padding * max;
		}

		// return max range value
		if (this.m_norm) {
			max = 1.0;
		}
		if (Double.isNaN(max)) {
			max = 0;
		}
		return max;
	}

	/**
	 * Sets the polygon values for a visual item.
	 */
	private void setPolygon(final VisualItem item, final float[] poly) {
		final float[] a = this.getPolygon(item, this.m_field);
		final float[] s = this.getPolygon(item, this.m_start);
		final float[] e = this.getPolygon(item, this.m_end);
		System.arraycopy(a, 0, s, 0, a.length);
		System.arraycopy(poly, 0, a, 0, poly.length);
		System.arraycopy(poly, 0, e, 0, poly.length);
		item.setValidated(false);
	}

	/**
	 * Get the polygon values for a visual item.
	 */
	private float[] getPolygon(final VisualItem item, final String field) {
		float[] poly = (float[]) item.get(field);
		if ((poly == null) || (poly.length < (4 * this.columns.length))) {
			// get oriented
			final int len = this.columns.length;
			float inc = (float) (this.m_horiz ? (this.bounds.getMinY() - this.bounds.getMaxY())
					: (this.bounds.getMaxX() - this.bounds.getMinX()));
			inc /= len - 1;
			final float max = (float) (this.m_horiz ? (this.m_top ? this.bounds.getMaxX() : this.bounds.getMinX())
					: (this.m_top ? this.bounds.getMinY() : this.bounds.getMaxY()));
			final float min = (float) (this.m_horiz ? this.bounds.getMaxY() : this.bounds.getMinX());
			final int bias = (this.m_horiz ? 1 : 0);

			// create polygon, populate default values
			poly = new float[4 * len];
			Arrays.fill(poly, max);
			for (int i = 0; i < len; ++i) {
				final float x = (i * inc) + min;
				poly[(2 * (len + i)) + bias] = x;
				poly[(2 * (len - 1 - i)) + bias] = x;
			}
			item.set(field, poly);
		}
		return poly;
	}

} // end of class StackedAreaChart
