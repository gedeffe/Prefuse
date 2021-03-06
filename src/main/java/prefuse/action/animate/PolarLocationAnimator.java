package prefuse.action.animate;

import javafx.geometry.Point2D;
import prefuse.Display;
import prefuse.action.ItemAction;
import prefuse.util.MathLib;
import prefuse.visual.VisualItem;

/**
 * Animator that interpolates between starting and ending display locations by
 * linearly interpolating between polar coordinates.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class PolarLocationAnimator extends ItemAction {

	private Point2D m_anchor = Point2D.ZERO;
	private String m_linear = null;

	// temp variables
	private double ax, ay, sx, sy, ex, ey, x, y;
	private double dt1, dt2, sr, st, er, et, r, t, stt, ett;

	/**
	 * Creates a PolarLocationAnimator that operates on all VisualItems within a
	 * Visualization.
	 */
	public PolarLocationAnimator() {
		super();
	}

	/**
	 * Creates a PolarLocationAnimator that operates on VisualItems within the
	 * specified group.
	 * 
	 * @param group
	 *            the data group to process
	 */
	public PolarLocationAnimator(final String group) {
		super(group);
	}

	/**
	 * Creates a PolarLocationAnimator that operates on VisualItems within the
	 * specified group, while using regular linear interpolation (in Cartesian
	 * (x,y) coordinates rather than polar coordinates) for items also contained
	 * within the specified linearGroup.
	 * 
	 * @param group
	 *            the data group to process
	 * @param linearGroup
	 *            the group of items that should be interpolated in Cartesian
	 *            (standard x,y) coordinates rather than polar coordinates. Note
	 *            that this animator will not process any items in
	 *            <code>linearGroup</code> that are not also in
	 *            <code>group</code>.
	 */
	public PolarLocationAnimator(final String group, final String linearGroup) {
		super(group);
		this.m_linear = linearGroup;
	}

	private void setAnchor() {
		final Display d = this.getVisualization().getDisplay(0);
		this.m_anchor = new Point2D(d.getWidth() / 2, d.getHeight() / 2);
		this.m_anchor = d.getAbsoluteCoordinate(this.m_anchor);
		this.ax = this.m_anchor.getX();
		this.ay = this.m_anchor.getY();
	}

	/**
	 * @see prefuse.action.Action#run(double)
	 */
	@Override
	public void run(final double frac) {
		this.setAnchor();
		super.run(frac);
	}

	/**
	 * @see prefuse.action.ItemAction#process(prefuse.visual.VisualItem, double)
	 */
	@Override
	public void process(final VisualItem item, final double frac) {
		if ((this.m_linear != null) && item.isInGroup(this.m_linear)) {
			// perform linear interpolation instead
			double s = item.getStartX();
			item.setX(s + (frac * (item.getEndX() - s)));
			s = item.getStartY();
			item.setY(s + (frac * (item.getEndY() - s)));
			return;
		}

		// otherwise, interpolate in polar coordinates
		this.sx = item.getStartX() - this.ax;
		this.sy = item.getStartY() - this.ay;
		this.ex = item.getEndX() - this.ax;
		this.ey = item.getEndY() - this.ay;

		this.sr = Math.sqrt((this.sx * this.sx) + (this.sy * this.sy));
		this.st = Math.atan2(this.sy, this.sx);

		this.er = Math.sqrt((this.ex * this.ex) + (this.ey * this.ey));
		this.et = Math.atan2(this.ey, this.ex);

		this.stt = this.st < 0 ? this.st + MathLib.TWO_PI : this.st;
		this.ett = this.et < 0 ? this.et + MathLib.TWO_PI : this.et;

		this.dt1 = this.et - this.st;
		this.dt2 = this.ett - this.stt;

		if (Math.abs(this.dt1) < Math.abs(this.dt2)) {
			this.t = this.st + (frac * this.dt1);
		} else {
			this.t = this.stt + (frac * this.dt2);
		}
		this.r = this.sr + (frac * (this.er - this.sr));

		this.x = Math.round(this.ax + (this.r * Math.cos(this.t)));
		this.y = Math.round(this.ay + (this.r * Math.sin(this.t)));

		item.setX(this.x);
		item.setY(this.y);
	}

} // end of class PolarLocationAnimator
