package prefuse.util;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Affine;
import prefuse.render.AbstractShapeRenderer;
import prefuse.visual.VisualItem;

/**
 * Library of useful computer graphics routines such as geometry routines for
 * computing the intersection of different shapes and rendering methods for
 * computing bounds and performing optimized drawing.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class GraphicsLib {

    /** Indicates only one intersection point available */
    public static final int INTERSECTION = 1;
    /** Indicates no intersection between shapes */
    public static final int NO_INTERSECTION = 0;
    /** Indicates full intersection between shapes */
    public static final int COINCIDENT = -1;
    /** Indicates two lines are parallel */
    public static final int PARALLEL = -2;

    /**
     * Compute the intersection of two line segments.
     *
     * @param a
     *            the first line segment
     * @param b
     *            the second line segment
     * @return the intersection status. With an intersection code, one of
     *         {@link GraphicsLib#INTERSECTION}
     *         {@link GraphicsLib#NO_INTERSECTION},
     *         {@link GraphicsLib#COINCIDENT}, or {@link GraphicsLib#PARALLEL}.
     *         It might contains also the intersection point (only if
     *         intersection code is {@link GraphicsLib#INTERSECTION}).
     */
    public static IntersectionCheck intersectLineLine(final Line a, final Line b) {
        final double a1x = a.getStartX(), a1y = a.getStartY();
        final double a2x = a.getEndX(), a2y = a.getEndY();
        final double b1x = b.getStartX(), b1y = b.getStartY();
        final double b2x = b.getEndX(), b2y = b.getEndY();
        return GraphicsLib.intersectLineLine(a1x, a1y, a2x, a2y, b1x, b1y, b2x, b2y);
    }

    /**
     * Compute the intersection of two line segments.
     *
     * @param a1x
     *            the x-coordinate of the first endpoint of the first line
     * @param a1y
     *            the y-coordinate of the first endpoint of the first line
     * @param a2x
     *            the x-coordinate of the second endpoint of the first line
     * @param a2y
     *            the y-coordinate of the second endpoint of the first line
     * @param b1x
     *            the x-coordinate of the first endpoint of the second line
     * @param b1y
     *            the y-coordinate of the first endpoint of the second line
     * @param b2x
     *            the x-coordinate of the second endpoint of the second line
     * @param b2y
     *            the y-coordinate of the second endpoint of the second line
     * @param intersect
     *            a Point in which to store the intersection point
     * @return the intersection status. With an intersection code, one of
     *         {@link GraphicsLib#INTERSECTION}
     *         {@link GraphicsLib#NO_INTERSECTION},
     *         {@link GraphicsLib#COINCIDENT}, or {@link GraphicsLib#PARALLEL}.
     *         It might contains also the intersection point (only if
     *         intersection code is {@link GraphicsLib#INTERSECTION}).
     */
    public static IntersectionCheck intersectLineLine(final double a1x, final double a1y, final double a2x,
            final double a2y, final double b1x, final double b1y, final double b2x, final double b2y) {
        final double ua_t = ((b2x - b1x) * (a1y - b1y)) - ((b2y - b1y) * (a1x - b1x));
        final double ub_t = ((a2x - a1x) * (a1y - b1y)) - ((a2y - a1y) * (a1x - b1x));
        final double u_b = ((b2y - b1y) * (a2x - a1x)) - ((b2x - b1x) * (a2y - a1y));

        final IntersectionCheck result = new IntersectionCheck();

        if (u_b != 0) {
            final double ua = ua_t / u_b;
            final double ub = ub_t / u_b;

            if ((0 <= ua) && (ua <= 1) && (0 <= ub) && (ub <= 1)) {
                result.setIntersectionPoint(new Point2D(a1x + (ua * (a2x - a1x)), a1y + (ua * (a2y - a1y))));
                result.setIntersectionCode(GraphicsLib.INTERSECTION);
            } else {
                result.setIntersectionCode(GraphicsLib.NO_INTERSECTION);
            }
        } else {
            if ((ua_t == 0) || (ub_t == 0)) {
                result.setIntersectionCode(GraphicsLib.COINCIDENT);
            } else {
                result.setIntersectionCode(GraphicsLib.PARALLEL);
            }
            ;
        }
        return result;
    }

    /**
     * Compute the intersection of a line and a rectangle.
     *
     * @param a1
     *            the first endpoint of the line
     * @param a2
     *            the second endpoint of the line
     * @param r
     *            the rectangle
     * @param pts
     *            a length 2 or greater array of points in which to store the
     *            results
     * @return the intersection code. One of {@link #NO_INTERSECTION},
     *         {@link #COINCIDENT}, or {@link #PARALLEL} or the number of
     *         intersection points (in case of the value is greater than 0).
     */
    public static int intersectLineRectangle(final Point2D a1, final Point2D a2, final Rectangle2D r,
            final Point2D[] pts) {
        final double a1x = a1.getX(), a1y = a1.getY();
        final double a2x = a2.getX(), a2y = a2.getY();
        final double mxx = r.getMaxX(), mxy = r.getMaxY();
        final double mnx = r.getMinX(), mny = r.getMinY();

        if (pts[0] == null) {
            pts[0] = Point2D.ZERO;
        }
        if (pts[1] == null) {
            pts[1] = Point2D.ZERO;
        }

        int i = 0;
        IntersectionCheck intersectionCheck = GraphicsLib.intersectLineLine(mnx, mny, mxx, mny, a1x, a1y, a2x, a2y);
        if (intersectionCheck.getIntersectionCode() == GraphicsLib.INTERSECTION) {
            pts[i] = intersectionCheck.getIntersectionPoint();
            i++;
        }
        intersectionCheck = GraphicsLib.intersectLineLine(mxx, mny, mxx, mxy, a1x, a1y, a2x, a2y);
        if (intersectionCheck.getIntersectionCode() == GraphicsLib.INTERSECTION) {
            pts[i] = intersectionCheck.getIntersectionPoint();
            i++;
        }
        if (i == 2) {
            return i;
        }
        intersectionCheck = GraphicsLib.intersectLineLine(mxx, mxy, mnx, mxy, a1x, a1y, a2x, a2y);
        if (intersectionCheck.getIntersectionCode() == GraphicsLib.INTERSECTION) {
            pts[i] = intersectionCheck.getIntersectionPoint();
            i++;
        }
        if (i == 2) {
            return i;
        }
        if (intersectionCheck.getIntersectionCode() == GraphicsLib.INTERSECTION) {
            pts[i] = intersectionCheck.getIntersectionPoint();
            i++;
        }
        return i;
    }

    /**
     * Compute the intersection of a line and a rectangle.
     *
     * @param l
     *            the line
     * @param r
     *            the rectangle
     * @param pts
     *            a length 2 or greater array of points in which to store the
     *            results
     * @return the intersection code. One of {@link #NO_INTERSECTION},
     *         {@link #COINCIDENT}, or {@link #PARALLEL} or the number of
     *         intersection points (in case of the value is greater than 0).
     */
    public static int intersectLineRectangle(final Line l, final Rectangle2D r, final Point2D[] pts) {
        final double a1x = l.getStartX(), a1y = l.getStartY();
        final double a2x = l.getEndX(), a2y = l.getEndY();
        final double mxx = r.getMaxX(), mxy = r.getMaxY();
        final double mnx = r.getMinX(), mny = r.getMinY();

        if (pts[0] == null) {
            pts[0] = Point2D.ZERO;
        }
        if (pts[1] == null) {
            pts[1] = Point2D.ZERO;
        }

        int i = 0;
        IntersectionCheck intersectionCheck = GraphicsLib.intersectLineLine(mnx, mny, mxx, mny, a1x, a1y, a2x, a2y);
        if (intersectionCheck.getIntersectionCode() == GraphicsLib.INTERSECTION) {
            pts[i] = intersectionCheck.getIntersectionPoint();
            i++;
        }
        intersectionCheck = GraphicsLib.intersectLineLine(mxx, mny, mxx, mxy, a1x, a1y, a2x, a2y);
        if (intersectionCheck.getIntersectionCode() == GraphicsLib.INTERSECTION) {
            pts[i] = intersectionCheck.getIntersectionPoint();
            i++;
        }
        if (i == 2) {
            return i;
        }
        intersectionCheck = GraphicsLib.intersectLineLine(mxx, mxy, mnx, mxy, a1x, a1y, a2x, a2y);
        if (intersectionCheck.getIntersectionCode() == GraphicsLib.INTERSECTION) {
            pts[i] = intersectionCheck.getIntersectionPoint();
            i++;
        }
        if (i == 2) {
            return i;
        }
        intersectionCheck = GraphicsLib.intersectLineLine(mnx, mxy, mnx, mny, a1x, a1y, a2x, a2y);
        if (intersectionCheck.getIntersectionCode() == GraphicsLib.INTERSECTION) {
            pts[i] = intersectionCheck.getIntersectionPoint();
            i++;
        }
        return i;
    }

    /**
     * Computes the 2D convex hull of a set of points using Graham's scanning
     * algorithm. The algorithm has been implemented as described in Cormen,
     * Leiserson, and Rivest's Introduction to Algorithms.
     *
     * The running time of this algorithm is O(n log n), where n is the number
     * of input points.
     *
     * @param pts
     *            the input points in [x0,y0,x1,y1,...] order
     * @param len
     *            the length of the pts array to consider (2 * #points)
     * @return the convex hull of the input points
     */
    public static double[] convexHull(final double[] pts, final int len) {
        if (len < 6) {
            throw new IllegalArgumentException("Input must have at least 3 points");
        }
        final int plen = (len / 2) - 1;
        final float[] angles = new float[plen];
        final int[] idx = new int[plen];
        final int[] stack = new int[len / 2];
        return GraphicsLib.convexHull(pts, len, angles, idx, stack);
    }

    /**
     * Computes the 2D convex hull of a set of points using Graham's scanning
     * algorithm. The algorithm has been implemented as described in Cormen,
     * Leiserson, and Rivest's Introduction to Algorithms.
     *
     * The running time of this algorithm is O(n log n), where n is the number
     * of input points.
     *
     * @param pts
     * @return the convex hull of the input points
     */
    public static double[] convexHull(final double[] pts, final int len, final float[] angles, final int[] idx,
            final int[] stack) {
        // check arguments
        final int plen = (len / 2) - 1;
        if (len < 6) {
            throw new IllegalArgumentException("Input must have at least 3 points");
        }
        if ((angles.length < plen) || (idx.length < plen) || (stack.length < (len / 2))) {
            throw new IllegalArgumentException("Pre-allocated data structure too small");
        }

        int i0 = 0;
        // find the starting ref point: leftmost point with the minimum y coord
        for (int i = 2; i < len; i += 2) {
            if (pts[i + 1] < pts[i0 + 1]) {
                i0 = i;
            } else if (pts[i + 1] == pts[i0 + 1]) {
                i0 = (pts[i] < pts[i0] ? i : i0);
            }
        }

        // calculate polar angles from ref point and sort
        for (int i = 0, j = 0; i < len; i += 2) {
            if (i == i0) {
                continue;
            }
            angles[j] = (float) Math.atan2(pts[i + 1] - pts[i0 + 1], pts[i] - pts[i0]);
            idx[j++] = i;
        }
        ArrayLib.sort(angles, idx, plen);

        // toss out duplicated angles
        float angle = angles[0];
        int ti = 0, tj = idx[0];
        for (int i = 1; i < plen; i++) {
            final int j = idx[i];
            if (angle == angles[i]) {
                // keep whichever angle corresponds to the most distant
                // point from the reference point
                final double x1 = pts[tj] - pts[i0];
                final double y1 = pts[tj + 1] - pts[i0 + 1];
                final double x2 = pts[j] - pts[i0];
                final double y2 = pts[j + 1] - pts[i0 + 1];
                final double d1 = (x1 * x1) + (y1 * y1);
                final double d2 = (x2 * x2) + (y2 * y2);
                if (d1 >= d2) {
                    idx[i] = -1;
                } else {
                    idx[ti] = -1;
                    angle = angles[i];
                    ti = i;
                    tj = j;
                }
            } else {
                angle = angles[i];
                ti = i;
                tj = j;
            }
        }

        // initialize our stack
        int sp = 0;
        stack[sp++] = i0;
        int j = 0;
        for (int k = 0; k < 2; j++) {
            if (idx[j] != -1) {
                stack[sp++] = idx[j];
                k++;
            }
        }

        // do graham's scan
        for (; j < plen; j++) {
            if (idx[j] == -1) {
                continue; // skip tossed out points
            }
            while (GraphicsLib.isNonLeft(i0, stack[sp - 2], stack[sp - 1], idx[j], pts)) {
                sp--;
            }
            stack[sp++] = idx[j];
        }

        // construct the hull
        final double[] hull = new double[2 * sp];
        for (int i = 0; i < sp; i++) {
            hull[2 * i] = pts[stack[i]];
            hull[(2 * i) + 1] = pts[stack[i] + 1];
        }

        return hull;
    }

    /**
     * Convex hull helper method for detecting a non left turn about 3 points
     */
    private static boolean isNonLeft(final int i0, final int i1, final int i2, final int i3, final double[] pts) {
        double l1, l2, l4, l5, l6, angle1, angle2, angle;

        l1 = Math.sqrt(Math.pow(pts[i2 + 1] - pts[i1 + 1], 2) + Math.pow(pts[i2] - pts[i1], 2));
        l2 = Math.sqrt(Math.pow(pts[i3 + 1] - pts[i2 + 1], 2) + Math.pow(pts[i3] - pts[i2], 2));
        l4 = Math.sqrt(Math.pow(pts[i3 + 1] - pts[i0 + 1], 2) + Math.pow(pts[i3] - pts[i0], 2));
        l5 = Math.sqrt(Math.pow(pts[i1 + 1] - pts[i0 + 1], 2) + Math.pow(pts[i1] - pts[i0], 2));
        l6 = Math.sqrt(Math.pow(pts[i2 + 1] - pts[i0 + 1], 2) + Math.pow(pts[i2] - pts[i0], 2));

        angle1 = Math.acos((((l2 * l2) + (l6 * l6)) - (l4 * l4)) / (2 * l2 * l6));
        angle2 = Math.acos((((l6 * l6) + (l1 * l1)) - (l5 * l5)) / (2 * l6 * l1));

        angle = (Math.PI - angle1) - angle2;

        if (angle <= 0.0) {
            return (true);
        } else {
            return (false);
        }
    }

    /**
     * Computes the mean, or centroid, of a set of points
     *
     * @param pts
     *            the points array, in x1, y1, x2, y2, ... arrangement.
     * @param len
     *            the length of the array to consider
     * @return the centroid as a length-2 float array
     */
    public static float[] centroid(final float pts[], final int len) {
        final float[] c = new float[] { 0, 0 };
        for (int i = 0; i < len; i += 2) {
            c[0] += pts[i];
            c[1] += pts[i + 1];
        }
        c[0] /= len / 2;
        c[1] /= len / 2;
        return c;
    }

    /**
     * Expand a polygon by adding the given distance along the line from the
     * centroid of the polyong.
     *
     * @param pts
     *            the polygon to expand, a set of points in a float array
     * @param len
     *            the length of the range of the array to consider
     * @param amt
     *            the amount by which to expand the polygon, each point will be
     *            moved this distance along the line from the centroid of the
     *            polygon to the given point.
     */
    public static void growPolygon(final float pts[], final int len, final float amt) {
        final float[] c = GraphicsLib.centroid(pts, len);
        for (int i = 0; i < len; i += 2) {
            final float vx = pts[i] - c[0];
            final float vy = pts[i + 1] - c[1];
            final float norm = (float) Math.sqrt((vx * vx) + (vy * vy));
            pts[i] += (amt * vx) / norm;
            pts[i + 1] += (amt * vy) / norm;
        }
    }

    /**
     * Compute a cardinal spline, a series of cubic Bezier splines smoothly
     * connecting a set of points. Cardinal splines maintain C(1) continuity,
     * ensuring the connected spline segments form a differentiable curve,
     * ensuring at least a minimum level of smoothness.
     *
     * @param pts
     *            the points to interpolate with a cardinal spline
     * @param slack
     *            a parameter controlling the "tightness" of the spline to the
     *            control points, 0.10 is a typically suitable value
     * @param closed
     *            true if the cardinal spline should be closed (i.e. return to
     *            the starting point), false for an open curve
     * @return the cardinal spline as a Java2D {@link java.awt.geom.GeneralPath}
     *         instance.
     */
    public static Path cardinalSpline(final float pts[], final float slack, final boolean closed) {
        final Path path = new Path();
        final MoveTo moveTo = new MoveTo(pts[0], pts[1]);
        path.getElements().add(moveTo);
        return GraphicsLib.cardinalSpline(path, pts, slack, closed, 0f, 0f);
    }

    /**
     * Compute a cardinal spline, a series of cubic Bezier splines smoothly
     * connecting a set of points. Cardinal splines maintain C(1) continuity,
     * ensuring the connected spline segments form a differentiable curve,
     * ensuring at least a minimum level of smoothness.
     *
     * @param pts
     *            the points to interpolate with a cardinal spline
     * @param start
     *            the starting index from which to read points
     * @param npoints
     *            the number of points to consider
     * @param slack
     *            a parameter controlling the "tightness" of the spline to the
     *            control points, 0.10 is a typically suitable value
     * @param closed
     *            true if the cardinal spline should be closed (i.e. return to
     *            the starting point), false for an open curve
     * @return the cardinal spline as a Java2D {@link java.awt.geom.GeneralPath}
     *         instance.
     */
    public static Path cardinalSpline(final float pts[], final int start, final int npoints, final float slack,
            final boolean closed) {
        final Path path = new Path();
        final MoveTo moveTo = new MoveTo(pts[start], pts[start + 1]);
        path.getElements().add(moveTo);
        return GraphicsLib.cardinalSpline(path, pts, start, npoints, slack, closed, 0f, 0f);
    }

    /**
     * Compute a cardinal spline, a series of cubic Bezier splines smoothly
     * connecting a set of points. Cardinal splines maintain C(1) continuity,
     * ensuring the connected spline segments form a differentiable curve,
     * ensuring at least a minimum level of smoothness.
     *
     * @param p
     *            the GeneralPath instance to use to store the result
     * @param pts
     *            the points to interpolate with a cardinal spline
     * @param slack
     *            a parameter controlling the "tightness" of the spline to the
     *            control points, 0.10 is a typically suitable value
     * @param closed
     *            true if the cardinal spline should be closed (i.e. return to
     *            the starting point), false for an open curve
     * @param tx
     *            a value by which to translate the curve along the x-dimension
     * @param ty
     *            a value by which to translate the curve along the y-dimension
     * @return the cardinal spline as a Java2D {@link java.awt.geom.GeneralPath}
     *         instance.
     */
    public static Path cardinalSpline(final Path p, final float pts[], final float slack, final boolean closed,
            final float tx, final float ty) {
        int npoints = 0;
        for (; npoints < pts.length; ++npoints) {
            if (Float.isNaN(pts[npoints])) {
                break;
            }
        }
        return GraphicsLib.cardinalSpline(p, pts, 0, npoints / 2, slack, closed, tx, ty);
    }

    /**
     * Compute a cardinal spline, a series of cubic Bezier splines smoothly
     * connecting a set of points. Cardinal splines maintain C(1) continuity,
     * ensuring the connected spline segments form a differentiable curve,
     * ensuring at least a minimum level of smoothness.
     *
     * @param path
     *            the GeneralPath instance to use to store the result
     * @param pts
     *            the points to interpolate with a cardinal spline
     * @param start
     *            the starting index from which to read points
     * @param npoints
     *            the number of points to consider
     * @param slack
     *            a parameter controlling the "tightness" of the spline to the
     *            control points, 0.10 is a typically suitable value
     * @param closed
     *            true if the cardinal spline should be closed (i.e. return to
     *            the starting point), false for an open curve
     * @param tx
     *            a value by which to translate the curve along the x-dimension
     * @param ty
     *            a value by which to translate the curve along the y-dimension
     * @return the cardinal spline as a Java2D {@link java.awt.geom.GeneralPath}
     *         instance.
     */
    public static Path cardinalSpline(final Path path, final float pts[], final int start, final int npoints,
            final float slack, final boolean closed, final float tx, final float ty) {
        // compute the size of the path
        final int len = 2 * npoints;
        final int end = start + len;

        if (len < 6) {
            throw new IllegalArgumentException("To create spline requires at least 3 points");
        }

        float dx1, dy1, dx2, dy2;

        // compute first control point
        if (closed) {
            dx2 = pts[start + 2] - pts[end - 2];
            dy2 = pts[start + 3] - pts[end - 1];
        } else {
            dx2 = pts[start + 4] - pts[start];
            dy2 = pts[start + 5] - pts[start + 1];
        }

        // repeatedly compute next control point and append curve
        int i;
        for (i = start + 2; i < (end - 2); i += 2) {
            dx1 = dx2;
            dy1 = dy2;
            dx2 = pts[i + 2] - pts[i - 2];
            dy2 = pts[i + 3] - pts[i - 1];
            final CubicCurveTo cubicCurveTo = new CubicCurveTo(tx + pts[i - 2] + (slack * dx1),
                    ty + pts[i - 1] + (slack * dy1), (tx + pts[i]) - (slack * dx2), (ty + pts[i + 1]) - (slack * dy2),
                    tx + pts[i], ty + pts[i + 1]);
            path.getElements().add(cubicCurveTo);
        }

        // compute last control point
        if (closed) {
            dx1 = dx2;
            dy1 = dy2;
            dx2 = pts[start] - pts[i - 2];
            dy2 = pts[start + 1] - pts[i - 1];
            CubicCurveTo cubicCurveTo = new CubicCurveTo(tx + pts[i - 2] + (slack * dx1),
                    ty + pts[i - 1] + (slack * dy1), (tx + pts[i]) - (slack * dx2), (ty + pts[i + 1]) - (slack * dy2),
                    tx + pts[i], ty + pts[i + 1]);
            path.getElements().add(cubicCurveTo);

            dx1 = dx2;
            dy1 = dy2;
            dx2 = pts[start + 2] - pts[end - 2];
            dy2 = pts[start + 3] - pts[end - 1];
            cubicCurveTo = new CubicCurveTo(tx + pts[end - 2] + (slack * dx1), ty + pts[end - 1] + (slack * dy1),
                    (tx + pts[0]) - (slack * dx2), (ty + pts[1]) - (slack * dy2), tx + pts[0], ty + pts[1]);
            path.getElements().add(cubicCurveTo);
            path.getElements().add(new ClosePath());
        } else {
            final CubicCurveTo cubicCurveTo = new CubicCurveTo(tx + pts[i - 2] + (slack * dx2),
                    ty + pts[i - 1] + (slack * dy2), (tx + pts[i]) - (slack * dx2), (ty + pts[i + 1]) - (slack * dy2),
                    tx + pts[i], ty + pts[i + 1]);
            path.getElements().add(cubicCurveTo);
        }
        return path;
    }

    /**
     * Computes a set of curves using the cardinal spline approach, but using
     * straight lines for completely horizontal or vertical segments.
     *
     * @param p
     *            the GeneralPath instance to use to store the result
     * @param pts
     *            the points to interpolate with the spline
     * @param epsilon
     *            threshold value under which to treat the difference between
     *            two values to be zero. Used to determine which segments to
     *            treat as lines rather than curves.
     * @param slack
     *            a parameter controlling the "tightness" of the spline to the
     *            control points, 0.10 is a typically suitable value
     * @param closed
     *            true if the spline should be closed (i.e. return to the
     *            starting point), false for an open curve
     * @param tx
     *            a value by which to translate the curve along the x-dimension
     * @param ty
     *            a value by which to translate the curve along the y-dimension
     * @return the stack spline as a Java2D {@link java.awt.geom.GeneralPath}
     *         instance.
     */
    public static Path stackSpline(final Path p, final float[] pts, final float epsilon, final float slack,
            final boolean closed, final float tx, final float ty) {
        int npoints = 0;
        for (; npoints < pts.length; ++npoints) {
            if (Float.isNaN(pts[npoints])) {
                break;
            }
        }
        return GraphicsLib.stackSpline(p, pts, 0, npoints / 2, epsilon, slack, closed, tx, ty);
    }

    /**
     * Computes a set of curves using the cardinal spline approach, but using
     * straight lines for completely horizontal or vertical segments.
     *
     * @param path
     *            the GeneralPath instance to use to store the result
     * @param pts
     *            the points to interpolate with the spline
     * @param start
     *            the starting index from which to read points
     * @param npoints
     *            the number of points to consider
     * @param epsilon
     *            threshold value under which to treat the difference between
     *            two values to be zero. Used to determine which segments to
     *            treat as lines rather than curves.
     * @param slack
     *            a parameter controlling the "tightness" of the spline to the
     *            control points, 0.10 is a typically suitable value
     * @param closed
     *            true if the spline should be closed (i.e. return to the
     *            starting point), false for an open curve
     * @param tx
     *            a value by which to translate the curve along the x-dimension
     * @param ty
     *            a value by which to translate the curve along the y-dimension
     * @return the stack spline as a Java2D {@link java.awt.geom.GeneralPath}
     *         instance.
     */
    public static Path stackSpline(final Path path, final float pts[], final int start, final int npoints,
            final float epsilon, final float slack, final boolean closed, final float tx, final float ty) {
        // compute the size of the path
        final int len = 2 * npoints;
        final int end = start + len;

        if (len < 6) {
            throw new IllegalArgumentException("To create spline requires at least 3 points");
        }

        float dx1, dy1, dx2, dy2;
        // compute first control point
        if (closed) {
            dx2 = pts[start + 2] - pts[end - 2];
            dy2 = pts[start + 3] - pts[end - 1];
        } else {
            dx2 = pts[start + 4] - pts[start];
            dy2 = pts[start + 5] - pts[start + 1];
        }

        // repeatedly compute next control point and append curve
        int i;
        for (i = start + 2; i < (end - 2); i += 2) {
            dx1 = dx2;
            dy1 = dy2;
            dx2 = pts[i + 2] - pts[i - 2];
            dy2 = pts[i + 3] - pts[i - 1];
            if ((Math.abs(pts[i] - pts[i - 2]) < epsilon) || (Math.abs(pts[i + 1] - pts[i - 1]) < epsilon)) {
                final LineTo lineTo = new LineTo(tx + pts[i], ty + pts[i + 1]);
                path.getElements().add(lineTo);
            } else {
                final CubicCurveTo cubicCurveTo = new CubicCurveTo(tx + pts[i - 2] + (slack * dx1),
                        ty + pts[i - 1] + (slack * dy1), (tx + pts[i]) - (slack * dx2),
                        (ty + pts[i + 1]) - (slack * dy2), tx + pts[i], ty + pts[i + 1]);
                path.getElements().add(cubicCurveTo);
            }
        }

        // compute last control point
        dx1 = dx2;
        dy1 = dy2;
        dx2 = pts[start] - pts[i - 2];
        dy2 = pts[start + 1] - pts[i - 1];
        if ((Math.abs(pts[i] - pts[i - 2]) < epsilon) || (Math.abs(pts[i + 1] - pts[i - 1]) < epsilon)) {
            final LineTo lineTo = new LineTo(tx + pts[i], ty + pts[i + 1]);
            path.getElements().add(lineTo);
        } else {
            final CubicCurveTo cubicCurveTo = new CubicCurveTo(tx + pts[i - 2] + (slack * dx1),
                    ty + pts[i - 1] + (slack * dy1), (tx + pts[i]) - (slack * dx2), (ty + pts[i + 1]) - (slack * dy2),
                    tx + pts[i], ty + pts[i + 1]);
            path.getElements().add(cubicCurveTo);
        }

        // close the curve if requested
        if (closed) {
            if ((Math.abs(pts[end - 2] - pts[0]) < epsilon) || (Math.abs(pts[end - 1] - pts[1]) < epsilon)) {
                final LineTo lineTo = new LineTo(tx + pts[0], ty + pts[1]);
                path.getElements().add(lineTo);
            } else {
                dx1 = dx2;
                dy1 = dy2;
                dx2 = pts[start + 2] - pts[end - 2];
                dy2 = pts[start + 3] - pts[end - 1];
                final CubicCurveTo cubicCurveTo = new CubicCurveTo(tx + pts[end - 2] + (slack * dx1),
                        ty + pts[end - 1] + (slack * dy1), (tx + pts[0]) - (slack * dx2), (ty + pts[1]) - (slack * dy2),
                        tx + pts[0], ty + pts[1]);
                path.getElements().add(cubicCurveTo);
            }
            path.getElements().add(new ClosePath());
        }
        return path;
    }

    /**
     * Expand a rectangle by the given amount.
     *
     * @param r
     *            the rectangle to expand
     * @param amount
     *            the amount by which to expand the rectangle
     */
    public static Rectangle2D expand(final Rectangle2D r, final double amount) {
        final Rectangle2D result = new Rectangle2D(r.getMinX() - amount, r.getMinY() - amount,
                r.getWidth() + (2 * amount), r.getHeight() + (2 * amount));
        return result;
    }

    // ------------------------------------------------------------------------

    /**
     * Sets a VisualItem's bounds based on its shape and stroke type. This
     * method is optimized to avoid calling .getBounds2D where it can, thus
     * avoiding object initialization and reducing object churn.
     *
     * @param item
     *            the VisualItem whose bounds are to be set
     * @param shape
     *            a Shape from which to determine the item bounds
     * @param stroke
     *            the stroke type that will be used for drawing the object, and
     *            may affect the final bounds. A null value indicates the
     *            default (line width = 1) stroke is used.
     */
    public static void setBounds(final VisualItem item, final Shape shape, final Shape stroke) {
        double x, y, w, h, lw;
        final double lw2;

        if (shape instanceof Rectangle) {
            // this covers rectangle, rounded rectangle, ellipse, and arcs
            final Rectangle r = (Rectangle) shape;
            x = r.getX();
            y = r.getY();
            w = r.getWidth();
            h = r.getHeight();
        } else if (shape instanceof Line) {
            // this covers straight lines
            final Line l = (Line) shape;
            x = l.getStartX();
            y = l.getStartY();
            w = l.getEndX();
            h = l.getEndY();
            if (w < x) {
                lw = x;
                x = w;
                w = lw - x;
            } else {
                w = w - x;
            }
            if (h < y) {
                lw = y;
                y = h;
                h = lw - y;
            } else {
                h = h - y;
            }
        } else {
            // this covers any other arbitrary shapes, but
            // takes a small object allocation / garbage collection hit
            final Bounds r = shape.getBoundsInLocal();
            x = r.getMinX();
            y = r.getMinY();
            w = r.getWidth();
            h = r.getHeight();
        }

        // adjust boundary for stroke length as necessary
        if ((stroke != null) && ((lw = stroke.getStrokeWidth()) > 1)) {
            lw2 = lw / 2.0;
            x -= lw2;
            y -= lw2;
            w += lw;
            h += lw;
        }
        item.setBounds(x, y, w, h);
    }

    /**
     * Render a shape associated with a VisualItem into a graphics context. This
     * method uses the {@link java.awt.Graphics} interface methods when it can,
     * as opposed to the {@link javafx.scene.canvas.GraphicsContext} methods
     * such as {@link javafx.scene.canvas.GraphicsContext#draw(java.awt.Shape)}
     * and {@link javafx.scene.canvas.GraphicsContext#fill(java.awt.Shape)},
     * resulting in a significant performance increase on the Windows platform,
     * particularly for rectangle and line drawing calls.
     *
     * @param g
     *            the graphics context to render to
     * @param item
     *            the item being represented by the shape, this instance is used
     *            to get the correct color values for the drawing
     * @param shape
     *            the shape to render
     * @param stroke
     *            the stroke type to use for drawing the object.
     * @param type
     *            the rendering type indicating if the shape should be drawn,
     *            filled, or both. One of
     *            {@link prefuse.render.AbstractShapeRenderer#RENDER_TYPE_DRAW},
     *            {@link prefuse.render.AbstractShapeRenderer#RENDER_TYPE_FILL},
     *            {@link prefuse.render.AbstractShapeRenderer#RENDER_TYPE_DRAW_AND_FILL}
     *            , or
     *            {@link prefuse.render.AbstractShapeRenderer#RENDER_TYPE_NONE}.
     */
    public static void paint(final GraphicsContext g, final VisualItem item, final Shape shape, final Shape stroke,
            final int type) {
        // if render type is NONE, then there is nothing to do
        if (type == AbstractShapeRenderer.RENDER_TYPE_NONE) {
            return;
        }

        // set up colors
        final Color strokeColor = ColorLib.getColor(item.getStrokeColor());
        final Color fillColor = ColorLib.getColor(item.getFillColor());
        final boolean sdraw = ((type == AbstractShapeRenderer.RENDER_TYPE_DRAW)
                || (type == AbstractShapeRenderer.RENDER_TYPE_DRAW_AND_FILL)) && (strokeColor.getOpacity() != 0);
        final boolean fdraw = ((type == AbstractShapeRenderer.RENDER_TYPE_FILL)
                || (type == AbstractShapeRenderer.RENDER_TYPE_DRAW_AND_FILL)) && (fillColor.getOpacity() != 0);
        if (!(sdraw || fdraw)) {
            return;
        }

        Paint origStroke = null;
        if (sdraw) {
            origStroke = g.getStroke();
            g.setStroke(stroke.getStroke());
        }

        int x, y, w, h;
        double xx, yy, ww, hh, aw, ah;

        // see if an optimized (non-shape) rendering call is available for us
        // these can speed things up significantly on the windows JRE
        // it is stupid we have to do this, but we do what we must
        // if we are zoomed in, we have no choice but to use
        // full precision rendering methods.
        final Affine at = g.getTransform();
        final double scale = Math.max(at.getMxx(), at.getMyy());
        if (scale > 1.5) {
            if (fdraw) {
                g.setFill(fillColor);
                // TODO we do not use shape information, and we should ...
                g.fill();
            }
            if (sdraw) {
                g.setStroke(strokeColor);
                // TODO we do not use shape information, and we should ...
                g.stroke();
            }
        } else if (shape instanceof Rectangle) {
            final Rectangle r = (Rectangle) shape;
            xx = r.getX();
            ww = r.getWidth();
            yy = r.getY();
            hh = r.getHeight();

            x = (int) xx;
            y = (int) yy;
            w = (int) ((ww + xx) - x);
            h = (int) ((hh + yy) - y);

            // check rounded rectangle or not ...
            if ((r.getArcHeight() > 0) && (r.getArcWidth() > 0)) {
                aw = r.getArcWidth();
                ah = r.getArcHeight();
                if (fdraw) {
                    g.setFill(fillColor);
                    g.fillRoundRect(x, y, w, h, aw, ah);
                }
                if (sdraw) {
                    g.setStroke(strokeColor);
                    g.strokeRoundRect(x, y, w, h, aw, ah);
                }
            } else {
                if (fdraw) {
                    g.setFill(fillColor);
                    g.fillRect(x, y, w, h);
                }
                if (sdraw) {
                    g.setStroke(strokeColor);
                    g.strokeRect(x, y, w, h);
                }
            }
        } else if (shape instanceof Ellipse) {
            final Ellipse ellipse = (Ellipse) shape;
            x = (int) (ellipse.getCenterX() - ellipse.getRadiusX());
            y = (int) (ellipse.getCenterY() - ellipse.getRadiusY());
            w = (int) (2 * ellipse.getRadiusX());
            h = (int) (2 * ellipse.getRadiusY());
            if (fdraw) {
                g.setFill(fillColor);
                g.fillOval(x, y, w, h);
            }
            if (sdraw) {
                g.setStroke(strokeColor);
                g.strokeOval(x, y, w, h);
            }
        } else if (shape instanceof Line) {
            if (sdraw) {
                final Line l = (Line) shape;
                x = (int) (l.getStartX() + 0.5);
                y = (int) (l.getStartY() + 0.5);
                w = (int) (l.getEndX() + 0.5);
                h = (int) (l.getEndY() + 0.5);
                g.setStroke(strokeColor);
                g.strokeLine(x, y, w, h);
            }
        } else {
            if (fdraw) {
                g.setFill(fillColor);
                // TODO we do not use shape information, and we should ...
                g.fill();
            }
            if (sdraw) {
                g.setStroke(strokeColor);
                g.stroke();
            }
        }
        // TODO add missing cases for Shape : Arc, Circle, Polygon, Polyline,
        // Text
        // we might just add provided shape to parent Canvas and let it do the
        // job.
        if (sdraw) {
            g.setStroke(origStroke);
        }
    }

} // end of class GraphicsLib
