package prefuse.render;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import prefuse.Constants;
import prefuse.util.ColorLib;
import prefuse.util.GraphicsLib;
import prefuse.visual.VisualItem;

/**
 * Renderer for drawing an axis tick mark and label.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 * @author jgood
 */
public class AxisRenderer extends AbstractShapeRenderer {

	private final Line m_line = new Line();
	private Rectangle m_box = new Rectangle();

	private int m_xalign;
	private int m_yalign;

	/**
	 * Create a new AxisRenderer. By default, axis labels are drawn along the
	 * left edge and underneath the tick marks.
	 */
	public AxisRenderer() {
		this(Constants.LEFT, Constants.BOTTOM);
	}

	/**
	 * Create a new AxisRenderer.
	 *
	 * @param xalign
	 *            the horizontal alignment for the axis label. One of
	 *            {@link prefuse.Constants#LEFT},
	 *            {@link prefuse.Constants#RIGHT}, or
	 *            {@link prefuse.Constants#CENTER}.
	 * @param yalign
	 *            the vertical alignment for the axis label. One of
	 *            {@link prefuse.Constants#TOP},
	 *            {@link prefuse.Constants#BOTTOM}, or
	 *            {@link prefuse.Constants#CENTER}.
	 */
	public AxisRenderer(final int xalign, final int yalign) {
		this.m_xalign = xalign;
		this.m_yalign = yalign;
	}

	/**
	 * Set the horizontal alignment of axis labels.
	 *
	 * @param xalign
	 *            the horizontal alignment for the axis label. One of
	 *            {@link prefuse.Constants#LEFT},
	 *            {@link prefuse.Constants#RIGHT}, or
	 *            {@link prefuse.Constants#CENTER}.
	 */
	public void setHorizontalAlignment(final int xalign) {
		this.m_xalign = xalign;
	}

	/**
	 * Set the vertical alignment of axis labels.
	 *
	 * @param yalign
	 *            the vertical alignment for the axis label. One of
	 *            {@link prefuse.Constants#TOP},
	 *            {@link prefuse.Constants#BOTTOM}, or
	 *            {@link prefuse.Constants#CENTER}.
	 */
	public void setVerticalAlignment(final int yalign) {
		this.m_yalign = yalign;
	}

	/**
	 * @see prefuse.render.AbstractShapeRenderer#getRawShape(prefuse.visual.VisualItem)
	 */
	@Override
	protected Shape getRawShape(final VisualItem item) {
		final double x1 = item.getDouble(VisualItem.X);
		final double y1 = item.getDouble(VisualItem.Y);
		final double x2 = item.getDouble(VisualItem.X2);
		final double y2 = item.getDouble(VisualItem.Y2);
		this.m_line.setStartX(x1);
		this.m_line.setStartY(y1);
		this.m_line.setEndX(x2);
		this.m_line.setEndY(y2);

		if (!item.canGetString(VisualItem.LABEL)) {
			return this.m_line;
		}

		final String label = item.getString(VisualItem.LABEL);
		if (label == null) {
			return this.m_line;
		}

		/*
		 * to compute text size with JavaFX, we have to work around ... so we
		 * will use a Text element with provided font, and get its bounds.
		 */
		final Text text = new Text(label);
		text.setFont(item.getFont());
		final double height = text.getBoundsInLocal().getHeight();
		final double width = text.getBoundsInLocal().getWidth();

		double tx, ty;

		// get text x-coord
		switch (this.m_xalign) {
		case Constants.FAR_RIGHT:
			tx = x2 + 2;
			break;
		case Constants.FAR_LEFT:
			tx = x1 - width - 2;
			break;
		case Constants.CENTER:
			tx = (x1 + ((x2 - x1) / 2)) - (width / 2);
			break;
		case Constants.RIGHT:
			tx = x2 - width;
			break;
		case Constants.LEFT:
		default:
			tx = x1;
		}
		// get text y-coord
		switch (this.m_yalign) {
		case Constants.FAR_TOP:
			ty = y1 - height;
			break;
		case Constants.FAR_BOTTOM:
			ty = y2;
			break;
		case Constants.CENTER:
			ty = (y1 + ((y2 - y1) / 2)) - (height / 2);
			break;
		case Constants.TOP:
			ty = y1;
			break;
		case Constants.BOTTOM:
		default:
			ty = y2 - height;
		}
		this.m_box = new Rectangle(tx, ty, width, height);
		return this.m_box;
	}

	/**
	 * @see prefuse.render.Renderer#render(javafx.scene.canvas.GraphicsContext,
	 *      prefuse.visual.VisualItem)
	 */
	@Override
	public void render(final GraphicsContext g, final VisualItem item) {
		final Shape s = this.getShape(item);
		GraphicsLib.paint(g, item, this.m_line, this.getStroke(item), this.getRenderType(item));

		// check if we have a text label, if so, render it
		if (item.canGetString(VisualItem.LABEL)) {
			final float x = (float) this.m_box.getX();
			final float y = (float) this.m_box.getY();

			// draw label background
			GraphicsLib.paint(g, item, s, null, RENDER_TYPE_FILL);

			final String str = item.getString(VisualItem.LABEL);
			final Affine origTransform = g.getTransform();
			final Affine transform = this.getTransform(item);
			if (transform != null) {
				g.setTransform(transform);
			}

			g.setFont(item.getFont());
			g.setFill(ColorLib.getColor(item.getTextColor()));
			g.fillText(str, x, y);

			if (transform != null) {
				g.setTransform(origTransform);
			}
		}
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
		} else if ((s == this.m_box) && this.m_box.contains(p)) {
			return true;
		} else {
			final double width = Math.max(2, item.getSize());
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
		} else if (shape == this.m_line) {
			GraphicsLib.setBounds(item, shape, this.getStroke(item));
		} else {
			this.expandCurrentBox(this.m_line.getStartX(), this.m_line.getStartY());
			this.expandCurrentBox(this.m_line.getEndX(), this.m_line.getEndY());
			item.setBounds(this.m_box.getX(), this.m_box.getY(), this.m_box.getWidth(), this.m_box.getHeight());
		}
	}

	/**
	 * Extract from java.awt.geom.Rectangle2D
	 * 
	 * Adds a point, specified by the double precision arguments
	 * <code>newx</code> and <code>newy</code>, to this <code>Rectangle2D</code>
	 * . The resulting <code>Rectangle2D</code> is the smallest
	 * <code>Rectangle2D</code> that contains both the original
	 * <code>Rectangle2D</code> and the specified point.
	 * <p>
	 * After adding a point, a call to <code>contains</code> with the added
	 * point as an argument does not necessarily return <code>true</code>. The
	 * <code>contains</code> method does not return <code>true</code> for points
	 * on the right or bottom edges of a rectangle. Therefore, if the added
	 * point falls on the left or bottom edge of the enlarged rectangle,
	 * <code>contains</code> returns <code>false</code> for that point.
	 * 
	 * @param newx
	 *            the X coordinate of the new point
	 * @param newy
	 *            the Y coordinate of the new point
	 */
	private void expandCurrentBox(final double newx, final double newy) {
		final double x1 = Math.min(this.m_box.getX(), newx);
		final double x2 = Math.max(this.m_box.getX() + this.m_box.getWidth(), newx);
		final double y1 = Math.min(this.m_box.getY(), newy);
		final double y2 = Math.max(this.m_box.getY() + this.m_box.getHeight(), newy);

		this.m_box.setX(x1);
		this.m_box.setY(y1);
		this.m_box.setWidth(x2 - x1);
		this.m_box.setHeight(y2 - y1);
	}
} // end of class AxisRenderer
