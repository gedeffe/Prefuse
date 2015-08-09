package prefuse.util.display;

import java.util.logging.Logger;

import javafx.geometry.Bounds;
import javafx.scene.transform.Affine;

/**
 * Represents a clipping rectangle in a prefuse <code>Display</code>.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class Clip {

	private static final byte EMPTY = 0;
	private static final byte INUSE = 1;
	private static final byte INVALID = 2;

	private final double[] clip = new double[8];
	private byte status = INVALID;

	/**
	 * Reset the clip to an empty status.
	 */
	public void reset() {
		this.status = EMPTY;
	}

	/**
	 * Invalidate the clip. In this state, the clip contents have no meaning.
	 */
	public void invalidate() {
		this.status = INVALID;
	}

	/**
	 * Set the clip contents, and set the status to valid and in use.
	 *
	 * @param c
	 *            the clip whose contents should be copied
	 */
	public void setClip(final Clip c) {
		this.status = INUSE;
		System.arraycopy(c.clip, 0, this.clip, 0, this.clip.length);
	}

	/**
	 * Set the clip contents, and set the status to valid and in use.
	 *
	 * @param r
	 *            the clip contents to copy
	 */
	public void setClip(final Bounds r) {
		this.setClip(r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight());
	}

	/**
	 * Set the clip contents, and set the status to valid and in use.
	 *
	 * @param x
	 *            the minimum x-coordinate
	 * @param y
	 *            the minimum y-coorindate
	 * @param w
	 *            the clip width
	 * @param h
	 *            the clip height
	 */
	public void setClip(final double x, final double y, final double w, final double h) {
		this.status = INUSE;
		this.clip[0] = x;
		this.clip[1] = y;
		this.clip[6] = x + w;
		this.clip[7] = y + h;
	}

	/**
	 * Transform the clip contents. A new clip region will be created which is
	 * the bounding box of the transformed region.
	 *
	 * @param at
	 *            the affine transform
	 */
	public void transform(final Affine at) {
		// make the extra corner points valid
		this.clip[2] = this.clip[0];
		this.clip[3] = this.clip[7];
		this.clip[4] = this.clip[6];
		this.clip[5] = this.clip[1];

		// transform the points
		at.transform2DPoints(this.clip, 0, this.clip, 0, 4);

		// make safe against rotation
		double xmin = this.clip[0], ymin = this.clip[1];
		double xmax = this.clip[6], ymax = this.clip[7];
		for (int i = 0; i < 7; i += 2) {
			if (this.clip[i] < xmin) {
				xmin = this.clip[i];
			}
			if (this.clip[i] > xmax) {
				xmax = this.clip[i];
			}
			if (this.clip[i + 1] < ymin) {
				ymin = this.clip[i + 1];
			}
			if (this.clip[i + 1] > ymax) {
				ymax = this.clip[i + 1];
			}
		}
		this.clip[0] = xmin;
		this.clip[1] = ymin;
		this.clip[6] = xmax;
		this.clip[7] = ymax;
	}

	/**
	 * Limit the clip such that it fits within the specified region.
	 *
	 * @param x1
	 *            the minimum x-coordinate
	 * @param y1
	 *            the minimum y-coorindate
	 * @param x2
	 *            the maximum x-coordinate
	 * @param y2
	 *            the maximum y-coorindate
	 */
	public void limit(final double x1, final double y1, final double x2, final double y2) {
		this.clip[0] = Math.max(this.clip[0], x1);
		this.clip[1] = Math.max(this.clip[1], y1);
		this.clip[6] = Math.min(this.clip[6], x2);
		this.clip[7] = Math.min(this.clip[7], y2);
	}

	/**
	 * Indicates if this Clip intersects the given rectangle expanded by the
	 * additional margin pace.
	 *
	 * @param r
	 *            the rectangle to test for intersect
	 * @param margin
	 *            additional margin "bleed" to include in the intersection
	 * @return true if the clip intersects the expanded region, false otherwise
	 */
	public boolean intersects(final Bounds r, final double margin) {
		double tw = this.clip[6] - this.clip[0];
		double th = this.clip[7] - this.clip[1];
		double rw = r.getWidth();
		double rh = r.getHeight();
		if ((rw < 0) || (rh < 0) || (tw < 0) || (th < 0)) {
			return false;
		}
		final double tx = this.clip[0];
		final double ty = this.clip[1];
		final double rx = r.getMinX() - margin;
		final double ry = r.getMinY() - margin;
		rw += rx + (2 * margin);
		rh += ry + (2 * margin);
		tw += tx;
		th += ty;
		// overflow || intersect
		return (((rw < rx) || (rw > tx)) && ((rh < ry) || (rh > ty)) && ((tw < tx) || (tw > rx))
				&& ((th < ty) || (th > ry)));
	}

	/**
	 * Union this clip with another clip. As a result, this clip will become a
	 * bounding box around the two original clips.
	 *
	 * @param c
	 *            the clip to union with
	 */
	public void union(final Clip c) {
		if (this.status == INVALID) {
			return;
		}
		if (this.status == EMPTY) {
			this.setClip(c);
			this.status = INUSE;
			return;
		}
		this.clip[0] = Math.min(this.clip[0], c.clip[0]);
		this.clip[1] = Math.min(this.clip[1], c.clip[1]);
		this.clip[6] = Math.max(this.clip[6], c.clip[6]);
		this.clip[7] = Math.max(this.clip[7], c.clip[7]);
	}

	/**
	 * Union this clip with another region. As a result, this clip will become a
	 * bounding box around the two original regions.
	 *
	 * @param r
	 *            the rectangle to union with
	 */
	public void union(final Bounds r) {
		if (this.status == INVALID) {
			return;
		}

		final double minx = r.getMinX();
		final double miny = r.getMinY();
		final double maxx = r.getMaxX();
		final double maxy = r.getMaxY();

		if (Double.isNaN(minx) || Double.isNaN(miny) || Double.isNaN(maxx) || Double.isNaN(maxy)) {
			Logger.getLogger(this.getClass().getName()).warning("Union with invalid clip region: " + r);
			return;
		}

		if (this.status == EMPTY) {
			this.setClip(r);
			this.status = INUSE;
			return;
		}
		this.clip[0] = Math.min(this.clip[0], minx);
		this.clip[1] = Math.min(this.clip[1], miny);
		this.clip[6] = Math.max(this.clip[6], maxx);
		this.clip[7] = Math.max(this.clip[7], maxy);
	}

	/**
	 * Union this clip with another region. As a result, this clip will become a
	 * bounding box around the two original regions.
	 *
	 * @param x
	 *            the x-coordinate of the region to union with
	 * @param y
	 *            the y-coordinate of the region to union with
	 * @param w
	 *            the width of the region to union with
	 * @param h
	 *            the height of the region to union with
	 */
	public void union(final double x, final double y, final double w, final double h) {
		if (this.status == INVALID) {
			return;
		}
		if (this.status == EMPTY) {
			this.setClip(x, y, w, h);
			this.status = INUSE;
			return;
		}
		this.clip[0] = Math.min(this.clip[0], x);
		this.clip[1] = Math.min(this.clip[1], y);
		this.clip[6] = Math.max(this.clip[6], x + w);
		this.clip[7] = Math.max(this.clip[7], y + h);
	}

	/**
	 * Intersect this clip with another region. As a result, this clip will
	 * become the intersecting area of the two regions.
	 *
	 * @param c
	 *            the clip to intersect with
	 */
	public void intersection(final Clip c) {
		if (this.status == INVALID) {
			return;
		}
		if (this.status == EMPTY) {
			this.setClip(c);
			this.status = INUSE;
			return;
		}
		this.clip[0] = Math.max(this.clip[0], c.clip[0]);
		this.clip[1] = Math.max(this.clip[1], c.clip[1]);
		this.clip[6] = Math.min(this.clip[6], c.clip[6]);
		this.clip[7] = Math.min(this.clip[7], c.clip[7]);
	}

	/**
	 * Intersect this clip with another region. As a result, this clip will
	 * become the intersecting area of the two regions.
	 *
	 * @param r
	 *            the rectangle to intersect with
	 */
	public void intersection(final Bounds r) {
		if (this.status == INVALID) {
			return;
		}
		if (this.status == EMPTY) {
			this.setClip(r);
			this.status = INUSE;
			return;
		}
		this.clip[0] = Math.max(this.clip[0], r.getMinX());
		this.clip[1] = Math.max(this.clip[1], r.getMinY());
		this.clip[6] = Math.min(this.clip[6], r.getMaxX());
		this.clip[7] = Math.min(this.clip[7], r.getMaxY());
	}

	/**
	 * Intersect this clip with another region. As a result, this clip will
	 * become the intersecting area of the two regions.
	 *
	 * @param x
	 *            the x-coordinate of the region to intersect with
	 * @param y
	 *            the y-coordinate of the region to intersect with
	 * @param w
	 *            the width of the region to intersect with
	 * @param h
	 *            the height of the region to intersect with
	 */
	public void intersection(final double x, final double y, final double w, final double h) {
		if (this.status == INVALID) {
			return;
		}
		if (this.status == EMPTY) {
			this.setClip(x, y, w, h);
			this.status = INUSE;
			return;
		}
		this.clip[0] = Math.max(this.clip[0], x);
		this.clip[1] = Math.max(this.clip[1], y);
		this.clip[6] = Math.min(this.clip[6], x + w);
		this.clip[7] = Math.min(this.clip[7], y + h);
	}

	/**
	 * Minimally expand the clip such that each coordinate is an integer.
	 */
	public void expandToIntegerLimits() {
		this.clip[0] = Math.floor(this.clip[0]);
		this.clip[1] = Math.floor(this.clip[1]);
		this.clip[6] = Math.ceil(this.clip[6]);
		this.clip[7] = Math.ceil(this.clip[7]);
	}

	/**
	 * Expand the clip in all directions by the given value.
	 *
	 * @param b
	 *            the value to expand by
	 */
	public void expand(final double b) {
		this.clip[0] -= b;
		this.clip[1] -= b;
		this.clip[6] += b;
		this.clip[7] += b;
	}

	/**
	 * Grow the clip width and height by the given value. The minimum
	 * coordinates will be unchanged.
	 *
	 * @param b
	 *            the value to grow the width and height by
	 */
	public void grow(final double b) {
		this.clip[6] += b;
		this.clip[7] += b;
	}

	/**
	 * Get the minimum x-coordinate.
	 *
	 * @return the minimum x-coordinate
	 */
	public double getMinX() {
		return this.clip[0];
	}

	/**
	 * Get the minimum y-coordinate.
	 *
	 * @return the minimum y-coordinate
	 */
	public double getMinY() {
		return this.clip[1];
	}

	/**
	 * Get the maximum x-coordinate.
	 *
	 * @return the maximum x-coordinate
	 */
	public double getMaxX() {
		return this.clip[6];
	}

	/**
	 * Get the maximum y-coordinate.
	 *
	 * @return the maximum y-coordinate
	 */
	public double getMaxY() {
		return this.clip[7];
	}

	/**
	 * Get the clip's width
	 *
	 * @return the clip width
	 */
	public double getWidth() {
		return this.clip[6] - this.clip[0];
	}

	/**
	 * Get the clip's height
	 *
	 * @return the clip height
	 */
	public double getHeight() {
		return this.clip[7] - this.clip[1];
	}

	/**
	 * Indicates if the clip is set to an empty status.
	 *
	 * @return true if the clip is set to empty, false otherwise
	 */
	public boolean isEmpty() {
		return this.status == EMPTY;
	}

	/**
	 * Indicates if the clip is set to an invalid status.
	 *
	 * @return true if the clip is set to invalid, false otherwise
	 */
	public boolean isInvalid() {
		return this.status == INVALID;
	}

	// ------------------------------------------------------------------------

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object o) {
		if (o instanceof Bounds) {
			final Bounds r = (Bounds) o;
			return ((r.getMinX() == this.clip[0]) && (r.getMinY() == this.clip[1]) && (r.getMaxX() == this.clip[6])
					&& (r.getMaxY() == this.clip[7]));
		} else if (o instanceof Clip) {
			final Clip r = (Clip) o;
			if (r.status == this.status) {
				if (this.status == Clip.INUSE) {
					return ((r.clip[0] == this.clip[0]) && (r.clip[1] == this.clip[1]) && (r.clip[6] == this.clip[6])
							&& (r.clip[7] == this.clip[7]));
				} else {
					return true;
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer(20);
		sb.append("Clip[");
		switch (this.status) {
		case INVALID:
			sb.append("invalid");
			break;
		case EMPTY:
			sb.append("empty");
			break;
		default:
			sb.append(this.clip[0]).append(",");
			sb.append(this.clip[1]).append(",");
			sb.append(this.clip[6]).append(",");
			sb.append(this.clip[7]);
		}
		sb.append("]");
		return sb.toString();
	}

} // end of class Clip
