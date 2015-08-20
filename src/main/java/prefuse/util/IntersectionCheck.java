package prefuse.util;

import javafx.geometry.Point2D;

/**
 * Status of computation to check if two lines segment intersect or not. It
 * contains a flag which indicates an intersection code, One of
 * {@link GraphicsLib#INTERSECTION} {@link GraphicsLib#NO_INTERSECTION},
 * {@link GraphicsLib#COINCIDENT}, or {@link GraphicsLib#PARALLEL}. It might
 * contains also the intersection point (if there is one).
 */
public class IntersectionCheck {
	private Point2D intersectionPoint = null;
	private int intersectionCode;

	/**
	 * To be able to retrieve a Point in which to store the intersection point.
	 *
	 * @return the intersectionPoint only if intersectionCode is
	 *         {@link GraphicsLib#INTERSECTION}, other else, it is always null.
	 */
	public Point2D getIntersectionPoint() {
		return this.intersectionPoint;
	}

	/**
	 * @param intersectionPoint
	 *            the intersectionPoint to set
	 */
	public void setIntersectionPoint(final Point2D intersectionPoint) {
		this.intersectionPoint = intersectionPoint;
	}

	/**
	 * @return the intersectionCode, One of {@link GraphicsLib#INTERSECTION}
	 *         {@link GraphicsLib#NO_INTERSECTION},
	 *         {@link GraphicsLib#COINCIDENT}, or {@link GraphicsLib#PARALLEL}.
	 */
	public int getIntersectionCode() {
		return this.intersectionCode;
	}

	/**
	 * @param intersectionCode
	 *            the intersectionCode to set
	 */
	public void setIntersectionCode(final int intersectionCode) {
		this.intersectionCode = intersectionCode;
	}

}
