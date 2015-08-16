package prefuse.action.layout;

import java.text.NumberFormat;
import java.util.Iterator;
import java.util.logging.Logger;

import javafx.geometry.Rectangle2D;
import prefuse.Constants;
import prefuse.data.Schema;
import prefuse.data.query.ObjectRangeModel;
import prefuse.data.tuple.TupleSet;
import prefuse.data.util.Index;
import prefuse.util.MathLib;
import prefuse.util.PrefuseLib;
import prefuse.util.ui.ValuedRangeModel;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;

/**
 * Layout Action that positions axis grid lines and labels for a given range
 * model.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class AxisLabelLayout extends Layout {

	public static final String FRAC = "frac";
	public static final String LABEL = "_label";
	public static final String VALUE = "_value";

	private AxisLayout m_layout; // pointer to matching layout, if any
	private ValuedRangeModel m_model;
	private double m_lo, m_hi, m_prevlo, m_prevhi;

	private NumberFormat m_nf = NumberFormat.getInstance();
	private final int m_axis;
	private boolean m_asc = true;
	private int m_scale = Constants.LINEAR_SCALE;

	private double m_spacing; // desired spacing between axis labels

	/**
	 * Create a new AxisLabelLayout layout.
	 *
	 * @param group
	 *            the data group of the axis lines and labels
	 * @param axis
	 *            the axis type, either {@link prefuse.Constants#X_AXIS} or
	 *            {@link prefuse.Constants#Y_AXIS}.
	 * @param values
	 *            the range model that defines the span of the axis
	 */
	public AxisLabelLayout(final String group, final int axis, final ValuedRangeModel values) {
		this(group, axis, values, null);
	}

	/**
	 * Create a new AxisLabelLayout layout.
	 *
	 * @param group
	 *            the data group of the axis lines and labels
	 * @param axis
	 *            the axis type, either {@link prefuse.Constants#X_AXIS} or
	 *            {@link prefuse.Constants#Y_AXIS}.
	 * @param values
	 *            the range model that defines the span of the axis
	 * @param bounds
	 *            the layout bounds within which to place the axis marks
	 */
	public AxisLabelLayout(final String group, final int axis, final ValuedRangeModel values,
			final Rectangle2D bounds) {
		super(group);
		if (bounds != null) {
			this.setLayoutBounds(bounds);
		}
		this.m_model = values;
		this.m_axis = axis;
		this.m_spacing = 50;
	}

	/**
	 * Create a new AxisLabelLayout layout.
	 *
	 * @param group
	 *            the data group of the axis lines and labels
	 * @param layout
	 *            an {@link AxisLayout} instance to model this layout after. The
	 *            axis type and range model of the provided instance will be
	 *            used.
	 */
	public AxisLabelLayout(final String group, final AxisLayout layout) {
		this(group, layout, null, 50);
	}

	/**
	 * Create a new AxisLabelLayout layout.
	 *
	 * @param group
	 *            the data group of the axis lines and labels
	 * @param layout
	 *            an {@link AxisLayout} instance to model this layout after. The
	 *            axis type and range model of the provided instance will be
	 *            used.
	 * @param bounds
	 *            the layout bounds within which to place the axis marks
	 */
	public AxisLabelLayout(final String group, final AxisLayout layout, final Rectangle2D bounds) {
		this(group, layout, bounds, 50);
	}

	/**
	 * Create a new AxisLabelLayout layout.
	 *
	 * @param group
	 *            the data group of the axis lines and labels
	 * @param layout
	 *            an {@link AxisLayout} instance to model this layout after. The
	 *            axis type and range model of the provided instance will be
	 *            used.
	 * @param bounds
	 *            the layout bounds within which to place the axis marks
	 * @param spacing
	 *            the minimum spacing between axis labels
	 */
	public AxisLabelLayout(final String group, final AxisLayout layout, final Rectangle2D bounds,
			final double spacing) {
		super(group);
		if (bounds != null) {
			this.setLayoutBounds(bounds);
		}
		this.m_layout = layout;
		this.m_model = layout.getRangeModel();
		this.m_axis = layout.getAxis();
		this.m_scale = layout.getScale();
		this.m_spacing = spacing;
	}

	// ------------------------------------------------------------------------

	/**
	 * Get the formatter used to format labels for numerical values.
	 *
	 * @return the <code>NumberFormat</code> used to format numerical labels.
	 */
	public NumberFormat getNumberFormat() {
		return this.m_nf;
	}

	/**
	 * Set the formatter used to format labels for numerical values.
	 *
	 * @param nf
	 *            the <code>NumberFormat</code> used to format numerical labels.
	 */
	public void setNumberFormat(final NumberFormat nf) {
		this.m_nf = nf;
	}

	/**
	 * Get the required minimum spacing between axis labels.
	 *
	 * @return the axis label spacing
	 */
	public double getSpacing() {
		return this.m_spacing;
	}

	/**
	 * Set the required minimum spacing between axis labels.
	 *
	 * @param spacing
	 *            the axis label spacing to use
	 */
	public void setSpacing(final double spacing) {
		this.m_spacing = spacing;
	}

	/**
	 * Returns the scale type used for the axis. This setting only applies for
	 * numerical data types (i.e., when axis values are from a
	 * <code>NumberValuedRange</code>).
	 *
	 * @return the scale type. One of {@link prefuse.Constants#LINEAR_SCALE},
	 *         {@link prefuse.Constants#SQRT_SCALE}, or
	 *         {@link Constants#LOG_SCALE}.
	 */
	public int getScale() {
		return this.m_scale;
	}

	/**
	 * Sets the scale type used for the axis. This setting only applies for
	 * numerical data types (i.e., when axis values are from a
	 * <code>NumberValuedRange</code>).
	 *
	 * @param scale
	 *            the scale type. One of {@link prefuse.Constants#LINEAR_SCALE},
	 *            {@link prefuse.Constants#SQRT_SCALE}, or
	 *            {@link Constants#LOG_SCALE}.
	 */
	public void setScale(final int scale) {
		if ((scale < 0) || (scale >= Constants.SCALE_COUNT)) {
			throw new IllegalArgumentException("Unrecognized scale type: " + scale);
		}
		this.m_scale = scale;
	}

	/**
	 * Indicates if the axis values should be presented in ascending order along
	 * the axis.
	 *
	 * @return true if data values increase as pixel coordinates increase, false
	 *         if data values decrease as pixel coordinates increase.
	 */
	public boolean isAscending() {
		return this.m_asc;
	}

	/**
	 * Sets if the axis values should be presented in ascending order along the
	 * axis.
	 *
	 * @param asc
	 *            true if data values should increase as pixel coordinates
	 *            increase, false if data values should decrease as pixel
	 *            coordinates increase.
	 */
	public void setAscending(final boolean asc) {
		this.m_asc = asc;
	}

	/**
	 * Sets the range model used to layout this axis.
	 *
	 * @param model
	 *            the range model
	 */
	public void setRangeModel(final ValuedRangeModel model) {
		this.m_model = model;
	}

	// ------------------------------------------------------------------------

	/**
	 * @see prefuse.action.GroupAction#run(double)
	 */
	@Override
	public void run(final double frac) {
		if ((this.m_model == null) && (this.m_layout != null)) {
			this.m_model = this.m_layout.getRangeModel();
		}

		if (this.m_model == null) {
			Logger.getLogger(this.getClass().getName()).warning("Axis labels missing a range model.");
			return;
		}

		final VisualTable labels = this.getTable();

		// check the axis label group to see if we can get a
		// more precise reading of the previous scale
		final Double dfrac = (Double) labels.getClientProperty(FRAC);
		final double fr = dfrac == null ? 1.0 : dfrac.doubleValue();
		this.m_prevlo = this.m_prevlo + (fr * (this.m_lo - this.m_prevlo));
		this.m_prevhi = this.m_prevhi + (fr * (this.m_hi - this.m_prevhi));

		// now compute the layout
		if (this.m_model instanceof ObjectRangeModel) { // ordinal layout
														// get the current high
														// and low values
			this.m_lo = this.m_model.getValue();
			this.m_hi = this.m_lo + this.m_model.getExtent();

			// compute the layout
			this.ordinalLayout(labels);
		} else { // numerical layout
					// get the current high and low values
			this.m_lo = ((Number) this.m_model.getLowValue()).doubleValue();
			this.m_hi = ((Number) this.m_model.getHighValue()).doubleValue();

			// compute the layout
			switch (this.m_scale) {
			case Constants.LOG_SCALE:
				this.logLayout(labels);
				break;
			case Constants.SQRT_SCALE:
				this.sqrtLayout(labels);
				break;
			case Constants.LINEAR_SCALE:
			default:
				this.linearLayout(labels);
			}
		}

		// get rid of any labels that are no longer being used
		this.garbageCollect(labels);
	}

	// ------------------------------------------------------------------------
	// Quantitative Axis Layout

	/**
	 * Calculates a quantitative, linearly scaled layout.
	 */
	protected void linearLayout(final VisualTable labels) {
		final Rectangle2D b = this.getLayoutBounds();
		final double breadth = this.getBreadth(b);

		final double span = this.m_hi - this.m_lo;
		final double pspan = this.m_prevhi - this.m_prevlo;
		double vlo = 0;
		if (this.m_lo >= 0) {
			vlo = Math.pow(10, Math.floor(MathLib.log10(this.m_lo)));
		} else {
			vlo = -Math.pow(10, 1 + Math.floor(MathLib.log10(-this.m_lo)));
		}
		// if ( vlo == 10 || vlo == 1 || vlo == 0.1 ) vlo = 0;

		// mark previously visible labels
		final Iterator iter = labels.tuples();
		while (iter.hasNext()) {
			final VisualItem item = (VisualItem) iter.next();
			this.reset(item);
			final double v = item.getDouble(VALUE);
			final double x = span == 0 ? 0 : ((v - this.m_lo) / span) * breadth;
			this.set(item, x, b);
		}

		final Index index = labels.index(VALUE);
		double step = this.getLinearStep(span, span == 0 ? 0 : breadth / span);
		if (step == 0) {
			step = 1;
		}
		int r;

		for (double x, v = vlo; v <= this.m_hi; v += step) {
			x = ((v - this.m_lo) / span) * breadth;
			if (x < -0.5) {
				continue;
			} else if ((r = index.get(v)) >= 0) {
				final VisualItem item = labels.getItem(r);
				item.setVisible(true);
				item.setEndVisible(true);
			} else {
				final VisualItem item = labels.addItem();
				item.set(LABEL, this.m_nf.format(v));
				item.setDouble(VALUE, v);
				final double f = pspan == 0 ? 0 : ((v - this.m_prevlo) / pspan);
				if ((f <= 0) || (f >= 1.0)) {
					item.setStartVisible(true);
				}
				this.set(item, f * breadth, b);
				this.set(item, x, b);
			}
		}
	}

	/**
	 * Calculates a quantitative, square root scaled layout.
	 */
	protected void sqrtLayout(final VisualTable labels) {
		final Rectangle2D b = this.getLayoutBounds();
		final double breadth = this.getBreadth(b);

		final double span = this.m_hi - this.m_lo;
		final double splo = MathLib.safeSqrt(this.m_prevlo);
		final double spspan = MathLib.safeSqrt(this.m_prevhi) - splo;
		final double vlo = Math.pow(10, Math.floor(MathLib.safeLog10(this.m_lo)));
		final double slo = MathLib.safeSqrt(this.m_lo);
		final double sspan = MathLib.safeSqrt(this.m_hi) - slo;

		// mark previously visible labels
		final Iterator iter = labels.tuples();
		while (iter.hasNext()) {
			final VisualItem item = (VisualItem) iter.next();
			this.reset(item);
			final double v = item.getDouble(VALUE);
			final double x = span == 0 ? 0 : ((MathLib.safeSqrt(v) - slo) / sspan) * breadth;
			this.set(item, x, b);
		}

		final Index index = labels.index(VALUE);
		double step = this.getLinearStep(span, breadth / span);
		if (step == 0) {
			step = 1;
		}
		int r;
		for (double x, v = vlo; v <= this.m_hi; v += step) {
			x = ((MathLib.safeSqrt(v) - slo) / sspan) * breadth;
			if (x < -0.5) {
				continue;
			} else if ((r = index.get(v)) >= 0) {
				final VisualItem item = labels.getItem(r);
				item.setVisible(true);
				item.setEndVisible(true);
			} else {
				final VisualItem item = labels.addItem();
				item.set(LABEL, this.m_nf.format(v));
				item.setDouble(VALUE, v);
				final double f = spspan == 0 ? 0 : ((MathLib.safeSqrt(v) - splo) / spspan);
				if ((f <= 0) || (f >= 1.0)) {
					item.setStartVisible(true);
				}
				this.set(item, f * breadth, b);
				this.set(item, x, b);
			}
		}
	}

	/**
	 * Calculates a quantitative, logarithmically-scaled layout. TODO: This
	 * method is currently not working correctly.
	 */
	protected void logLayout(final VisualTable labels) {
		final Rectangle2D b = this.getLayoutBounds();
		final double breadth = this.getBreadth(b);

		labels.clear();

		// get span in log space
		// get log of the difference
		// if [0.1,1) round to .1's 0.1-->0.1
		// if [1,10) round to 1's 1-->1
		// if [10,100) round to 10's 10-->10
		final double llo = MathLib.safeLog10(this.m_lo);
		final double lhi = MathLib.safeLog10(this.m_hi);
		final double lspan = lhi - llo;

		final double d = MathLib.log10(lhi - llo);
		final int e = (int) Math.floor(d);
		final int ilo = (int) Math.floor(llo);
		final int ihi = (int) Math.ceil(lhi);

		final double start = Math.pow(10, ilo);
		final double end = Math.pow(10, ihi);
		double step = start * Math.pow(10, e);
		// System.out.println((hi-lo)+"\t"+e+"\t"+start+"\t"+end+"\t"+step);

		// TODO: catch infinity case if diff is zero
		// figure out label cases better
		for (double val, v = start, i = 0; v <= end; v += step, ++i) {
			val = MathLib.safeLog10(v);
			if ((i != 0) && (Math.abs(val - Math.round(val)) < 0.0001)) {
				i = 0;
				step = 10 * step;
			}
			val = ((val - llo) / lspan) * breadth;
			if (val < -0.5) {
				continue;
			}

			final VisualItem item = labels.addItem();
			this.set(item, val, b);
			final String label = i == 0 ? this.m_nf.format(v) : null;
			item.set(LABEL, label);
			item.setDouble(VALUE, v);
		}
	}

	/**
	 * Get the "breadth" of a rectangle, based on the axis type.
	 */
	protected double getBreadth(final Rectangle2D b) {
		switch (this.m_axis) {
		case Constants.X_AXIS:
			return b.getWidth();
		default:
			return b.getHeight();
		}
	}

	/**
	 * Adjust a value according to the current scale type.
	 */
	protected double adjust(final double v) {
		switch (this.m_scale) {
		case Constants.LOG_SCALE:
			return Math.pow(10, v);
		case Constants.SQRT_SCALE:
			return v * v;
		case Constants.LINEAR_SCALE:
		default:
			return v;
		}
	}

	/**
	 * Compute a linear step between axis marks.
	 */
	protected double getLinearStep(final double span, final double scale) {
		final double log10 = Math.log(span) / Math.log(10);
		double step = Math.pow(10, Math.floor(log10));

		final double delta = (step * scale) / this.m_spacing;
		if (delta > 20) {
			step /= 20;
		} else if (delta > 10) {
			step /= 10;
		} else if (delta > 5) {
			step /= 5;
		} else if (delta > 4) {
			step /= 4;
		} else if (delta > 2) {
			step /= 2;
		} else if (delta < 1) {
			step *= 2;
		}
		return step;
	}

	// ------------------------------------------------------------------------
	// Ordinal Axis Layout

	/**
	 * Compute an ordinal layout of axis marks.
	 */
	protected void ordinalLayout(final VisualTable labels) {
		final ObjectRangeModel model = (ObjectRangeModel) this.m_model;
		final double span = this.m_hi - this.m_lo;
		final double pspan = this.m_prevhi - this.m_prevlo;

		final Rectangle2D b = this.getLayoutBounds();
		final double breadth = this.getBreadth(b);
		final double scale = breadth / span;
		int step = this.getOrdinalStep(span, scale);
		if (step <= 0) {
			step = 1;
		}

		// mark previously visible labels
		final Iterator iter = labels.tuples();
		while (iter.hasNext()) {
			final VisualItem item = (VisualItem) iter.next();
			this.reset(item);
			final double v = item.getDouble(VALUE);
			final double x = span == 0 ? 0.5 * breadth : ((v - this.m_lo) / span) * breadth;
			this.set(item, x, b);
		}

		final Index index = labels.index(VALUE);

		// handle remaining labels
		for (int r, v = (int) this.m_lo; v <= this.m_hi; v += step) {
			if ((r = index.get((double) v)) >= 0) {
				final VisualItem item = labels.getItem(r);
				item.set(VisualItem.LABEL, model.getObject(v).toString());
				item.setVisible(true);
				item.setEndVisible(true);
			} else {
				final VisualItem item = labels.addItem();
				item.set(VisualItem.LABEL, model.getObject(v).toString());
				item.setDouble(VisualItem.VALUE, v);
				final double f = pspan == 0 ? 0.5 : ((v - this.m_prevlo) / pspan);
				if ((f <= 0) || (f >= 1.0)) {
					item.setStartVisible(true);
				}
				this.set(item, f * breadth, b);
				this.set(item, ((v - this.m_lo) * breadth) / span, b);
			}
		}
	}

	/**
	 * Compute an ordinal step between axis marks.
	 */
	protected int getOrdinalStep(final double span, final double scale) {
		return (scale >= this.m_spacing ? 1 : (int) Math.ceil(this.m_spacing / scale));
	}

	// ------------------------------------------------------------------------
	// Auxiliary methods

	/**
	 * Set the layout values for an axis label item.
	 */
	protected void set(final VisualItem item, double xOrY, final Rectangle2D b) {
		switch (this.m_axis) {
		case Constants.X_AXIS:
			xOrY = this.m_asc ? xOrY + b.getMinX() : b.getMaxX() - xOrY;
			PrefuseLib.updateDouble(item, VisualItem.X, xOrY);
			PrefuseLib.updateDouble(item, VisualItem.Y, b.getMinY());
			PrefuseLib.updateDouble(item, VisualItem.X2, xOrY);
			PrefuseLib.updateDouble(item, VisualItem.Y2, b.getMaxY());
			break;
		case Constants.Y_AXIS:
			xOrY = this.m_asc ? b.getMaxY() - xOrY - 1 : xOrY + b.getMinY();
			PrefuseLib.updateDouble(item, VisualItem.X, b.getMinX());
			PrefuseLib.updateDouble(item, VisualItem.Y, xOrY);
			PrefuseLib.updateDouble(item, VisualItem.X2, b.getMaxX());
			PrefuseLib.updateDouble(item, VisualItem.Y2, xOrY);
		}
	}

	/**
	 * Reset an axis label VisualItem
	 */
	protected void reset(final VisualItem item) {
		item.setVisible(false);
		item.setEndVisible(false);
		item.setStartStrokeColor(item.getStrokeColor());
		item.revertToDefault(VisualItem.STROKECOLOR);
		item.revertToDefault(VisualItem.ENDSTROKECOLOR);
		item.setStartTextColor(item.getTextColor());
		item.revertToDefault(VisualItem.TEXTCOLOR);
		item.revertToDefault(VisualItem.ENDTEXTCOLOR);
		item.setStartFillColor(item.getFillColor());
		item.revertToDefault(VisualItem.FILLCOLOR);
		item.revertToDefault(VisualItem.ENDFILLCOLOR);
	}

	/**
	 * Remove axis labels no longer being used.
	 */
	protected void garbageCollect(final VisualTable labels) {
		final Iterator iter = labels.tuples();
		while (iter.hasNext()) {
			final VisualItem item = (VisualItem) iter.next();
			if (!item.isStartVisible() && !item.isEndVisible()) {
				labels.removeTuple(item);
			}
		}
	}

	/**
	 * Create a new table for representing axis labels.
	 */
	protected VisualTable getTable() {
		final TupleSet ts = this.m_vis.getGroup(this.m_group);
		if (ts == null) {
			final Schema s = PrefuseLib.getAxisLabelSchema();
			final VisualTable vt = this.m_vis.addTable(this.m_group, s);
			vt.index(VALUE);
			return vt;
		} else if (ts instanceof VisualTable) {
			return (VisualTable) ts;
		} else {
			throw new IllegalStateException("Group already exists, not being used for labels");
		}
	}

} // end of class AxisLabels
