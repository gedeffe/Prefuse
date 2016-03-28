package prefuse.render;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Affine;
import prefuse.Constants;
import prefuse.util.ColorLib;
import prefuse.util.GraphicsLib;
import prefuse.util.JavaFxLib;
import prefuse.util.StrokeLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;

/**
 * <p>
 * Renderer that draws edges as lines connecting nodes. Both straight and curved
 * lines are supported. Curved lines are drawn using cubic Bezier curves.
 * Subclasses can override the
 * {@link #getCurveControlPoints(EdgeItem, Point2D[], double, double, double, double)}
 * method to provide custom control point assignment for such curves.
 * </p>
 *
 * <p>
 * This class also supports arrows for directed edges. See the
 * {@link #setArrowType(int)} method for more.
 * </p>
 *
 * @version 1.0
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class EdgeRenderer extends AbstractShapeRenderer {

    public static final String EDGE_TYPE = "edgeType";

    protected static final double HALF_PI = Math.PI / 2;

    protected Line m_line = new Line();
    protected CubicCurve m_cubic = new CubicCurve();

    protected int m_edgeType = Constants.EDGE_TYPE_LINE;
    protected int m_xAlign1 = Constants.CENTER;
    protected int m_yAlign1 = Constants.CENTER;
    protected int m_xAlign2 = Constants.CENTER;
    protected int m_yAlign2 = Constants.CENTER;
    protected double m_width = 1;
    protected float m_curWidth = 1;
    protected Point2D m_tmpPoints[] = new Point2D[2];
    protected Point2D m_ctrlPoints[] = new Point2D[2];
    protected Point2D m_isctPoints[] = new Point2D[2];

    // arrow head handling
    protected int m_edgeArrow = Constants.EDGE_ARROW_FORWARD;
    protected int m_arrowWidth = 8;
    protected int m_arrowHeight = 12;
    protected Polygon m_arrowHead = this.updateArrowHead(this.m_arrowWidth, this.m_arrowHeight);
    protected Affine m_arrowTrans = new Affine();
    protected Shape m_curArrow;

    /**
     * Create a new EdgeRenderer.
     */
    public EdgeRenderer() {
        this.m_tmpPoints[0] = Point2D.ZERO;
        this.m_tmpPoints[1] = Point2D.ZERO;
        this.m_ctrlPoints[0] = Point2D.ZERO;
        this.m_ctrlPoints[1] = Point2D.ZERO;
        this.m_isctPoints[0] = Point2D.ZERO;
        this.m_isctPoints[1] = Point2D.ZERO;
    }

    /**
     * Create a new EdgeRenderer with the given edge type.
     *
     * @param edgeType
     *            the edge type, one of {@link prefuse.Constants#EDGE_TYPE_LINE}
     *            or {@link prefuse.Constants#EDGE_TYPE_CURVE}.
     */
    public EdgeRenderer(final int edgeType) {
        this(edgeType, Constants.EDGE_ARROW_FORWARD);
    }

    /**
     * Create a new EdgeRenderer with the given edge and arrow types.
     *
     * @param edgeType
     *            the edge type, one of {@link prefuse.Constants#EDGE_TYPE_LINE}
     *            or {@link prefuse.Constants#EDGE_TYPE_CURVE}.
     * @param arrowType
     *            the arrow type, one of
     *            {@link prefuse.Constants#EDGE_ARROW_FORWARD},
     *            {@link prefuse.Constants#EDGE_ARROW_REVERSE}, or
     *            {@link prefuse.Constants#EDGE_ARROW_NONE}.
     * @see #setArrowType(int)
     */
    public EdgeRenderer(final int edgeType, final int arrowType) {
        this();
        this.setEdgeType(edgeType);
        this.setArrowType(arrowType);
    }

    /**
     * @see prefuse.render.AbstractShapeRenderer#getRenderType(prefuse.visual.VisualItem)
     */
    @Override
    public int getRenderType(final VisualItem item) {
        return AbstractShapeRenderer.RENDER_TYPE_DRAW;
    }

    /**
     * @see prefuse.render.AbstractShapeRenderer#getRawShape(prefuse.visual.VisualItem)
     */
    @Override
    protected Shape getRawShape(final VisualItem item) {
        final EdgeItem edge = (EdgeItem) item;
        final VisualItem item1 = edge.getSourceItem();
        final VisualItem item2 = edge.getTargetItem();

        final int type = this.m_edgeType;

        this.m_tmpPoints[0] = EdgeRenderer.getAlignedPoint(item1.getBounds(), this.m_xAlign1, this.m_yAlign1);
        this.m_tmpPoints[1] = EdgeRenderer.getAlignedPoint(item2.getBounds(), this.m_xAlign2, this.m_yAlign2);
        this.m_curWidth = (float) (this.m_width * this.getLineWidth(item));

        // create the arrow head, if needed
        final EdgeItem e = (EdgeItem) item;
        if (e.isDirected() && (this.m_edgeArrow != Constants.EDGE_ARROW_NONE)) {
            // get starting and ending edge endpoints
            final boolean forward = (this.m_edgeArrow == Constants.EDGE_ARROW_FORWARD);
            Point2D start = null, end = null;
            start = this.m_tmpPoints[forward ? 0 : 1];
            end = this.m_tmpPoints[forward ? 1 : 0];

            // compute the intersection with the target bounding box
            final VisualItem dest = forward ? e.getTargetItem() : e.getSourceItem();
            final Bounds bounds = dest.getBounds();
            final Rectangle2D rectangle = new Rectangle2D(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(),
                    bounds.getHeight());
            final int i = GraphicsLib.intersectLineRectangle(start, end, rectangle, this.m_isctPoints);
            if (i > 0) {
                end = this.m_isctPoints[0];
            }

            // create the arrow head shape
            final Affine at = this.getArrowTrans(start, end, this.m_curWidth);
            this.m_arrowHead.getTransforms().add(at);

            // update the endpoints for the edge shape
            // need to bias this by arrow head size
            Point2D lineEnd = this.m_tmpPoints[forward ? 1 : 0];
            lineEnd = new Point2D(0, -this.m_arrowHeight);
            lineEnd = at.transform(lineEnd);
        } else {
            this.m_curArrow = null;
        }

        // create the edge shape
        Shape shape = null;
        final double n1x = this.m_tmpPoints[0].getX();
        final double n1y = this.m_tmpPoints[0].getY();
        final double n2x = this.m_tmpPoints[1].getX();
        final double n2y = this.m_tmpPoints[1].getY();
        switch (type) {
        case Constants.EDGE_TYPE_LINE:
            this.m_line = new Line(n1x, n1y, n2x, n2y);
            shape = this.m_line;
            break;
        case Constants.EDGE_TYPE_CURVE:
            this.getCurveControlPoints(edge, this.m_ctrlPoints, n1x, n1y, n2x, n2y);
            this.m_cubic = new CubicCurve(n1x, n1y, this.m_ctrlPoints[0].getX(), this.m_ctrlPoints[0].getY(),
                    this.m_ctrlPoints[1].getX(), this.m_ctrlPoints[1].getY(), n2x, n2y);
            shape = this.m_cubic;
            break;
        default:
            throw new IllegalStateException("Unknown edge type");
        }

        // return the edge shape
        return shape;
    }

    /**
     * @see prefuse.render.Renderer#render(javafx.scene.canvas.GraphicsContext,
     *      prefuse.visual.VisualItem)
     */
    @Override
    public void render(final GraphicsContext g, final VisualItem item) {
        // render the edge line
        super.render(g, item);
        // render the edge arrow head, if appropriate
        if (this.m_curArrow != null) {
            g.setFill(ColorLib.getColor(item.getFillColor()));
            JavaFxLib.drawShape(this.m_curArrow, g);
        }
    }

    /**
     * Returns an affine transformation that maps the arrowhead shape to the
     * position and orientation specified by the provided line segment end
     * points.
     */
    protected Affine getArrowTrans(final Point2D p1, final Point2D p2, final double width) {
        this.m_arrowTrans.setToIdentity();
        this.m_arrowTrans.appendTranslation(p2.getX(), p2.getY());
        this.m_arrowTrans.appendRotation(-EdgeRenderer.HALF_PI + Math.atan2(p2.getY() - p1.getY(), p2.getX() - p1.getX()));
        if (width > 1) {
            final double scalar = width / 4;
            this.m_arrowTrans.appendScale(scalar, scalar);
        }
        return this.m_arrowTrans;
    }

    /**
     * Update the dimensions of the arrow head, creating a new arrow head if
     * necessary. The return value is also set as the member variable
     * <code>m_arrowHead</code>
     *
     * @param w
     *            the width of the untransformed arrow head base, in pixels
     * @param h
     *            the height of the untransformed arrow head, in pixels
     * @return the untransformed arrow head shape
     */
    protected Polygon updateArrowHead(final int w, final int h) {
        this.m_arrowHead = new Polygon(0, 0, -w / 2, -h, w / 2, -h, 0, 0);
        return this.m_arrowHead;
    }

    /**
     * @see prefuse.render.AbstractShapeRenderer#getTransform(prefuse.visual.VisualItem)
     */
    @Override
    protected Affine getTransform(final VisualItem item) {
        return null;
    }

    /**
     * @see prefuse.render.Renderer#locatePoint(javafx.geometry.Point2D,
     *      prefuse.visual.VisualItem)
     */
    @Override
    public boolean locatePoint(final Point2D p, final VisualItem item) {
        final Shape s = this.getShape(item);
        if (s == null) {
            return false;
        } else {
            final double width = Math.max(2, this.getLineWidth(item));
            final double halfWidth = width / 2.0;
            return s.intersects(p.getX() - halfWidth, p.getY() - halfWidth, width, width);
        }
    }

    /**
     * @see prefuse.render.Renderer#setBounds(prefuse.visual.VisualItem)
     */
    @Override
    public void setBounds(final VisualItem item) {
        if (!this.m_manageBounds) {
            return;
        }
        final Shape shape = this.getShape(item);
        if (shape == null) {
            item.setBounds(item.getX(), item.getY(), 0, 0);
            return;
        }
        GraphicsLib.setBounds(item, shape, this.getStroke(item));
        if (this.m_curArrow != null) {
            final Bounds bbox = (Bounds) item.get(VisualItem.BOUNDS);
            final Rectangle rectangle = new Rectangle(bbox.getMinX(), bbox.getMinY(), bbox.getWidth(),
                    bbox.getHeight());
            final Bounds boundsInLocal = Shape.union(rectangle, this.m_curArrow).getBoundsInLocal();
            item.setBounds(boundsInLocal.getMinX(), boundsInLocal.getMinY(), boundsInLocal.getWidth(),
                    boundsInLocal.getHeight());
        }
    }

    /**
     * Returns the line width to be used for this VisualItem. By default,
     * returns the base width value set using the
     * {@link #setDefaultLineWidth(double)} method, scaled by the item size
     * returned by {@link VisualItem#getSize()}. Subclasses can override this
     * method to perform custom line width determination, however, the preferred
     * method is to change the item size value itself.
     *
     * @param item
     *            the VisualItem for which to determine the line width
     * @return the desired line width, in pixels
     */
    protected double getLineWidth(final VisualItem item) {
        return item.getSize();
    }

    /**
     * Returns the stroke value returned by {@link VisualItem#getStroke()},
     * scaled by the current line width determined by the
     * {@link #getLineWidth(VisualItem)} method. Subclasses may override this
     * method to perform custom stroke assignment, but should respect the line
     * width parameter stored in the {@link #m_curWidth} member variable, which
     * caches the result of <code>getLineWidth</code>.
     *
     * @see prefuse.render.AbstractShapeRenderer#getStroke(prefuse.visual.VisualItem)
     */
    @Override
    protected Shape getStroke(final VisualItem item) {
        return StrokeLib.getDerivedStroke(item.getStroke(), this.m_curWidth);
    }

    /**
     * Determines the control points to use for cubic (Bezier) curve edges.
     * Override this method to provide custom curve specifications. To reduce
     * object initialization, the entries of the Point2D array are already
     * initialized, so use the <tt>Point2D.setLocation()</tt> method rather than
     * <tt>Point2D.ZERO</tt> to more efficiently set custom control points.
     *
     * @param eitem
     *            the EdgeItem we are determining the control points for
     * @param cp
     *            array of Point2D's (length >= 2) in which to return the
     *            control points
     * @param x1
     *            the x co-ordinate of the first node this edge connects to
     * @param y1
     *            the y co-ordinate of the first node this edge connects to
     * @param x2
     *            the x co-ordinate of the second node this edge connects to
     * @param y2
     *            the y co-ordinate of the second node this edge connects to
     */
    protected void getCurveControlPoints(final EdgeItem eitem, final Point2D[] cp, final double x1, final double y1,
            final double x2, final double y2) {
        final double dx = x2 - x1, dy = y2 - y1;
        cp[0] = new Point2D(x1 + ((2 * dx) / 3), y1);
        cp[1] = new Point2D(x2 - (dx / 8), y2 - (dy / 8));
    }

    /**
     * Helper method, which calculates the top-left co-ordinate of a rectangle
     * given the rectangle's alignment.
     */
    protected static Point2D getAlignedPoint(final Bounds r, final int xAlign, final int yAlign) {
        double x = r.getMinX(), y = r.getMinY();
        final double w = r.getWidth(), h = r.getHeight();
        if (xAlign == Constants.CENTER) {
            x = x + (w / 2);
        } else if (xAlign == Constants.RIGHT) {
            x = x + w;
        }
        if (yAlign == Constants.CENTER) {
            y = y + (h / 2);
        } else if (yAlign == Constants.BOTTOM) {
            y = y + h;
        }
        return new Point2D(x, y);
    }

    /**
     * Returns the type of the drawn edge. This is one of
     * {@link prefuse.Constants#EDGE_TYPE_LINE} or
     * {@link prefuse.Constants#EDGE_TYPE_CURVE}.
     *
     * @return the edge type
     */
    public int getEdgeType() {
        return this.m_edgeType;
    }

    /**
     * Sets the type of the drawn edge. This must be one of
     * {@link prefuse.Constants#EDGE_TYPE_LINE} or
     * {@link prefuse.Constants#EDGE_TYPE_CURVE}.
     *
     * @param type
     *            the new edge type
     */
    public void setEdgeType(final int type) {
        if ((type < 0) || (type >= Constants.EDGE_TYPE_COUNT)) {
            throw new IllegalArgumentException("Unrecognized edge curve type: " + type);
        }
        this.m_edgeType = type;
    }

    /**
     * Returns the type of the drawn edge. This is one of
     * {@link prefuse.Constants#EDGE_ARROW_FORWARD},
     * {@link prefuse.Constants#EDGE_ARROW_REVERSE}, or
     * {@link prefuse.Constants#EDGE_ARROW_NONE}.
     *
     * @return the edge type
     */
    public int getArrowType() {
        return this.m_edgeArrow;
    }

    /**
     * Sets the type of the drawn edge. This is either
     * {@link prefuse.Constants#EDGE_ARROW_NONE} for no edge arrows,
     * {@link prefuse.Constants#EDGE_ARROW_FORWARD} for arrows from source to
     * target on directed edges, or {@link prefuse.Constants#EDGE_ARROW_REVERSE}
     * for arrows from target to source on directed edges.
     *
     * @param type
     *            the new arrow type
     */
    public void setArrowType(final int type) {
        if ((type < 0) || (type >= Constants.EDGE_ARROW_COUNT)) {
            throw new IllegalArgumentException("Unrecognized edge arrow type: " + type);
        }
        this.m_edgeArrow = type;
    }

    /**
     * Sets the dimensions of an arrow head for a directed edge. This specifies
     * the pixel dimensions when both the zoom level and the size factor (a
     * combination of item size value and default stroke width) are 1.0.
     *
     * @param width
     *            the untransformed arrow head width, in pixels. This specifies
     *            the span of the base of the arrow head.
     * @param height
     *            the untransformed arrow head height, in pixels. This specifies
     *            the distance from the point of the arrow to its base.
     */
    public void setArrowHeadSize(final int width, final int height) {
        this.m_arrowWidth = width;
        this.m_arrowHeight = height;
        this.m_arrowHead = this.updateArrowHead(width, height);
    }

    /**
     * Get the height of the untransformed arrow head. This is the distance, in
     * pixels, from the tip of the arrow to its base.
     *
     * @return the default arrow head height
     */
    public int getArrowHeadHeight() {
        return this.m_arrowHeight;
    }

    /**
     * Get the width of the untransformed arrow head. This is the length, in
     * pixels, of the base of the arrow head.
     *
     * @return the default arrow head width
     */
    public int getArrowHeadWidth() {
        return this.m_arrowWidth;
    }

    /**
     * Get the horizontal aligment of the edge mount point with the first node.
     *
     * @return the horizontal alignment, one of {@link prefuse.Constants#LEFT},
     *         {@link prefuse.Constants#RIGHT}, or
     *         {@link prefuse.Constants#CENTER}.
     */
    public int getHorizontalAlignment1() {
        return this.m_xAlign1;
    }

    /**
     * Get the vertical aligment of the edge mount point with the first node.
     *
     * @return the vertical alignment, one of {@link prefuse.Constants#TOP},
     *         {@link prefuse.Constants#BOTTOM}, or
     *         {@link prefuse.Constants#CENTER}.
     */
    public int getVerticalAlignment1() {
        return this.m_yAlign1;
    }

    /**
     * Get the horizontal aligment of the edge mount point with the second node.
     *
     * @return the horizontal alignment, one of {@link prefuse.Constants#LEFT},
     *         {@link prefuse.Constants#RIGHT}, or
     *         {@link prefuse.Constants#CENTER}.
     */
    public int getHorizontalAlignment2() {
        return this.m_xAlign2;
    }

    /**
     * Get the vertical aligment of the edge mount point with the second node.
     *
     * @return the vertical alignment, one of {@link prefuse.Constants#TOP},
     *         {@link prefuse.Constants#BOTTOM}, or
     *         {@link prefuse.Constants#CENTER}.
     */
    public int getVerticalAlignment2() {
        return this.m_yAlign2;
    }

    /**
     * Set the horizontal aligment of the edge mount point with the first node.
     *
     * @param align
     *            the horizontal alignment, one of
     *            {@link prefuse.Constants#LEFT},
     *            {@link prefuse.Constants#RIGHT}, or
     *            {@link prefuse.Constants#CENTER}.
     */
    public void setHorizontalAlignment1(final int align) {
        this.m_xAlign1 = align;
    }

    /**
     * Set the vertical aligment of the edge mount point with the first node.
     *
     * @param align
     *            the vertical alignment, one of {@link prefuse.Constants#TOP},
     *            {@link prefuse.Constants#BOTTOM}, or
     *            {@link prefuse.Constants#CENTER}.
     */
    public void setVerticalAlignment1(final int align) {
        this.m_yAlign1 = align;
    }

    /**
     * Set the horizontal aligment of the edge mount point with the second node.
     *
     * @param align
     *            the horizontal alignment, one of
     *            {@link prefuse.Constants#LEFT},
     *            {@link prefuse.Constants#RIGHT}, or
     *            {@link prefuse.Constants#CENTER}.
     */
    public void setHorizontalAlignment2(final int align) {
        this.m_xAlign2 = align;
    }

    /**
     * Set the vertical aligment of the edge mount point with the second node.
     *
     * @param align
     *            the vertical alignment, one of {@link prefuse.Constants#TOP},
     *            {@link prefuse.Constants#BOTTOM}, or
     *            {@link prefuse.Constants#CENTER}.
     */
    public void setVerticalAlignment2(final int align) {
        this.m_yAlign2 = align;
    }

    /**
     * Sets the default width of lines. This width value will be scaled by the
     * value of an item's size data field. The default base width is 1.
     *
     * @param w
     *            the desired default line width, in pixels
     */
    public void setDefaultLineWidth(final double w) {
        this.m_width = w;
    }

    /**
     * Gets the default width of lines. This width value that will be scaled by
     * the value of an item's size data field. The default base width is 1.
     *
     * @return the default line width, in pixels
     */
    public double getDefaultLineWidth() {
        return this.m_width;
    }

} // end of class EdgeRenderer
