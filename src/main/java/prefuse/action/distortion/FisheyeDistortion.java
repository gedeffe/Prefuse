package prefuse.action.distortion;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

/**
 * <p>
 * Computes a graphical fisheye distortion of a graph view. This distortion
 * allocates more space to items near the layout anchor and less space to items
 * further away, magnifying space near the anchor and demagnifying distant space
 * in a continuous fashion.
 * </p>
 *
 * <p>
 * For more details on this form of transformation, see Manojit Sarkar and Marc
 * H. Brown, "Graphical Fisheye Views of Graphs", in Proceedings of CHI'92,
 * Human Factors in Computing Systems, p. 83-91, 1992. Available online at
 * <a href="http://citeseer.ist.psu.edu/sarkar92graphical.html"> http://citeseer
 * .ist.psu.edu/sarkar92graphical.html</a>.
 * </p>
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class FisheyeDistortion extends Distortion {

	private double dx, dy; // distortion factors
	private final double sz = 3.0; // size factor

	/**
	 * Create a new FisheyeDistortion with default distortion factor.
	 */
	public FisheyeDistortion() {
		this(4);
	}

	/**
	 * Create a new FisheyeDistortion with the given distortion factor for use
	 * along both the x and y directions.
	 * 
	 * @param dfactor
	 *            the distortion factor (same for both axes)
	 */
	public FisheyeDistortion(final double dfactor) {
		this(dfactor, dfactor);
	}

	/**
	 * Create a new FisheyeDistortion with the given distortion factors along
	 * the x and y directions.
	 * 
	 * @param xfactor
	 *            the distortion factor along the x axis
	 * @param yfactor
	 *            the distortion factor along the y axis
	 */
	public FisheyeDistortion(final double xfactor, final double yfactor) {
		super();
		this.dx = xfactor;
		this.dy = yfactor;
		this.m_distortX = this.dx > 0;
		this.m_distortY = this.dy > 0;
	}

	/**
	 * Returns the distortion factor for the x-axis.
	 * 
	 * @return returns the distortion factor for the x-axis.
	 */
	public double getXDistortionFactor() {
		return this.dx;
	}

	/**
	 * Sets the distortion factor for the x-axis.
	 * 
	 * @param d
	 *            The distortion factor to set.
	 */
	public void setXDistortionFactor(final double d) {
		this.dx = d;
		this.m_distortX = this.dx > 0;
	}

	/**
	 * Returns the distortion factor for the y-axis.
	 * 
	 * @return returns the distortion factor for the y-axis.
	 */
	public double getYDistortionFactor() {
		return this.dy;
	}

	/**
	 * Sets the distortion factor for the y-axis.
	 * 
	 * @param d
	 *            The distortion factor to set.
	 */
	public void setYDistortionFactor(final double d) {
		this.dy = d;
		this.m_distortY = this.dy > 0;
	}

	/**
	 * @see prefuse.action.distortion.Distortion#distortX(double,
	 *      javafx.geometry.Point2D, javafx.geometry.Rectangle2D)
	 */
	@Override
	protected double distortX(final double x, final Point2D anchor, final Rectangle2D bounds) {
		return this.fisheye(x, anchor.getX(), this.dx, bounds.getMinX(), bounds.getMaxX());
	}

	/**
	 * @see prefuse.action.distortion.Distortion#distortY(double,
	 *      javafx.geometry.Point2D, javafx.geometry.Rectangle2D)
	 */
	@Override
	protected double distortY(final double y, final Point2D anchor, final Rectangle2D bounds) {
		return this.fisheye(y, anchor.getY(), this.dy, bounds.getMinY(), bounds.getMaxY());
	}

	/**
	 * @see prefuse.action.distortion.Distortion#distortSize(javafx.geometry.Rectangle2D,
	 *      double, double, javafx.geometry.Point2D, javafx.geometry.Rectangle2D)
	 */
	@Override
	protected double distortSize(final Rectangle2D bbox, final double x, final double y, final Point2D anchor,
			final Rectangle2D bounds) {
		if (!this.m_distortX && !this.m_distortY) {
			return 1.;
		}
		double fx = 1, fy = 1;

		if (this.m_distortX) {
			final double ax = anchor.getX();
			final double minX = bbox.getMinX(), maxX = bbox.getMaxX();
			double xx = (Math.abs(minX - ax) > Math.abs(maxX - ax) ? minX : maxX);
			if ((xx < bounds.getMinX()) || (xx > bounds.getMaxX())) {
				xx = (xx == minX ? maxX : minX);
			}
			fx = this.fisheye(xx, ax, this.dx, bounds.getMinX(), bounds.getMaxX());
			fx = Math.abs(x - fx) / bbox.getWidth();
		}

		if (this.m_distortY) {
			final double ay = anchor.getY();
			final double minY = bbox.getMinY(), maxY = bbox.getMaxY();
			double yy = (Math.abs(minY - ay) > Math.abs(maxY - ay) ? minY : maxY);
			if ((yy < bounds.getMinY()) || (yy > bounds.getMaxY())) {
				yy = (yy == minY ? maxY : minY);
			}
			fy = this.fisheye(yy, ay, this.dy, bounds.getMinY(), bounds.getMaxY());
			fy = Math.abs(y - fy) / bbox.getHeight();
		}

		final double sf = (!this.m_distortY ? fx : (!this.m_distortX ? fy : Math.min(fx, fy)));
		if (Double.isInfinite(sf) || Double.isNaN(sf)) {
			return 1.;
		} else {
			return this.sz * sf;
		}
	}

	private double fisheye(final double x, final double a, final double d, final double min, final double max) {
		if (d != 0) {
			final boolean left = x < a;
			double v, m = (left ? a - min : max - a);
			if (m == 0) {
				m = max - min;
			}
			v = Math.abs(x - a) / m;
			v = (d + 1) / (d + (1 / v));
			return ((left ? -1 : 1) * m * v) + a;
		} else {
			return x;
		}
	}

} // end of class FisheyeDistortion
