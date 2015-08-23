package prefuse.render;

import javafx.scene.shape.ClosePath;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import prefuse.Constants;
import prefuse.visual.VisualItem;

/**
 * Renderer for drawing simple shapes. This class provides a number of built-in
 * shapes, selected by an integer value retrieved from a VisualItem.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class ShapeRenderer extends AbstractShapeRenderer {

	private int m_baseSize = 10;

	private final Ellipse m_ellipse = new Ellipse();
	private final Rectangle m_rect = new Rectangle();
	private final Path m_path = new Path();

	/**
	 * Creates a new ShapeRenderer with default base size of 10 pixels.
	 */
	public ShapeRenderer() {
	}

	/**
	 * Creates a new ShapeRenderer with given base size.
	 *
	 * @param size
	 *            the base size in pixels
	 */
	public ShapeRenderer(final int size) {
		this.setBaseSize(size);
	}

	/**
	 * Sets the base size, in pixels, for shapes drawn by this renderer. The
	 * base size is the width and height value used when a VisualItem's size
	 * value is 1. The base size is scaled by the item's size value to arrive at
	 * the final scale used for rendering.
	 *
	 * @param size
	 *            the base size in pixels
	 */
	public void setBaseSize(final int size) {
		this.m_baseSize = size;
	}

	/**
	 * Returns the base size, in pixels, for shapes drawn by this renderer.
	 *
	 * @return the base size in pixels
	 */
	public int getBaseSize() {
		return this.m_baseSize;
	}

	/**
	 * @see prefuse.render.AbstractShapeRenderer#getRawShape(prefuse.visual.VisualItem)
	 */
	@Override
	protected Shape getRawShape(final VisualItem item) {
		final int stype = item.getShape();
		double x = item.getX();
		if (Double.isNaN(x) || Double.isInfinite(x)) {
			x = 0;
		}
		double y = item.getY();
		if (Double.isNaN(y) || Double.isInfinite(y)) {
			y = 0;
		}
		final double width = this.m_baseSize * item.getSize();

		// Center the shape around the specified x and y
		if (width > 1) {
			x = x - (width / 2);
			y = y - (width / 2);
		}

		switch (stype) {
		case Constants.SHAPE_NONE:
			return null;
		case Constants.SHAPE_RECTANGLE:
			return this.rectangle(x, y, width, width);
		case Constants.SHAPE_ELLIPSE:
			return this.ellipse(x, y, width, width);
		case Constants.SHAPE_TRIANGLE_UP:
			return this.triangle_up((float) x, (float) y, (float) width);
		case Constants.SHAPE_TRIANGLE_DOWN:
			return this.triangle_down((float) x, (float) y, (float) width);
		case Constants.SHAPE_TRIANGLE_LEFT:
			return this.triangle_left((float) x, (float) y, (float) width);
		case Constants.SHAPE_TRIANGLE_RIGHT:
			return this.triangle_right((float) x, (float) y, (float) width);
		case Constants.SHAPE_CROSS:
			return this.cross((float) x, (float) y, (float) width);
		case Constants.SHAPE_STAR:
			return this.star((float) x, (float) y, (float) width);
		case Constants.SHAPE_HEXAGON:
			return this.hexagon((float) x, (float) y, (float) width);
		case Constants.SHAPE_DIAMOND:
			return this.diamond((float) x, (float) y, (float) width);
		default:
			throw new IllegalStateException("Unknown shape type: " + stype);
		}
	}

	/**
	 * Returns a rectangle of the given dimensions.
	 */
	public Shape rectangle(final double x, final double y, final double width, final double height) {
		this.m_rect.setX(x);
		this.m_rect.setY(y);
		this.m_rect.setWidth(width);
		this.m_rect.setHeight(height);
		return this.m_rect;
	}

	/**
	 * Returns an ellipse of the given dimensions.
	 */
	public Shape ellipse(final double x, final double y, final double width, final double height) {
		/*
		 * We have to translate a rectangle to a center and radius information.
		 *
		 * So from x the X coordinate of the upper-left corner of the framing
		 * rectangle, y the Y coordinate of the upper-left corner of the framing
		 * rectangle, width the width of the framing rectangle and height the
		 * height of the framing rectangle
		 *
		 * To centerX the horizontal position of the center of the ellipse in
		 * pixels, centerY the vertical position of the center of the ellipse in
		 * pixels, radiusX the horizontal radius of the ellipse in pixels and
		 * radiusY the vertical radius of the ellipse in pixels.
		 */
		final double radiusX = width / 2;
		final double radiusY = height / 2;
		final double centerX = x + radiusX;
		final double centerY = y + radiusY;
		this.m_ellipse.setCenterX(centerX);
		this.m_ellipse.setCenterY(centerY);
		this.m_ellipse.setRadiusX(radiusX);
		this.m_ellipse.setRadiusY(radiusY);
		return this.m_ellipse;
	}

	/**
	 * Returns a up-pointing triangle of the given dimensions.
	 */
	public Shape triangle_up(final float x, final float y, final float height) {
		this.m_path.getElements().clear();
		final MoveTo moveTo = new MoveTo(x, y + height);
		this.m_path.getElements().add(moveTo);
		LineTo lineTo = new LineTo(x + (height / 2), y);
		this.m_path.getElements().add(lineTo);
		lineTo = new LineTo(x + height, (y + height));
		this.m_path.getElements().add(lineTo);
		this.m_path.getElements().add(new ClosePath());
		return this.m_path;
	}

	/**
	 * Returns a down-pointing triangle of the given dimensions.
	 */
	public Shape triangle_down(final float x, final float y, final float height) {
		this.m_path.getElements().clear();
		final MoveTo moveTo = new MoveTo(x, y);
		this.m_path.getElements().add(moveTo);
		LineTo lineTo = new LineTo(x + height, y);
		this.m_path.getElements().add(lineTo);
		lineTo = new LineTo(x + (height / 2), (y + height));
		this.m_path.getElements().add(lineTo);
		this.m_path.getElements().add(new ClosePath());
		return this.m_path;
	}

	/**
	 * Returns a left-pointing triangle of the given dimensions.
	 */
	public Shape triangle_left(final float x, final float y, final float height) {
		this.m_path.getElements().clear();
		final MoveTo moveTo = new MoveTo(x + height, y);
		this.m_path.getElements().add(moveTo);
		LineTo lineTo = new LineTo(x + height, y + height);
		this.m_path.getElements().add(lineTo);
		lineTo = new LineTo(x, y + (height / 2));
		this.m_path.getElements().add(lineTo);
		this.m_path.getElements().add(new ClosePath());
		return this.m_path;
	}

	/**
	 * Returns a right-pointing triangle of the given dimensions.
	 */
	public Shape triangle_right(final float x, final float y, final float height) {
		this.m_path.getElements().clear();
		final MoveTo moveTo = new MoveTo(x, y + height);
		this.m_path.getElements().add(moveTo);
		LineTo lineTo = new LineTo(x + height, y + (height / 2));
		this.m_path.getElements().add(lineTo);
		lineTo = new LineTo(x, y);
		this.m_path.getElements().add(lineTo);
		this.m_path.getElements().add(new ClosePath());
		return this.m_path;
	}

	/**
	 * Returns a cross shape of the given dimensions.
	 */
	public Shape cross(final float x, final float y, final float height) {
		final float h14 = (3 * height) / 8, h34 = (5 * height) / 8;
		this.m_path.getElements().clear();
		final MoveTo moveTo = new MoveTo(x + h14, y);
		this.m_path.getElements().add(moveTo);
		LineTo lineTo = new LineTo(x + h34, y);
		this.m_path.getElements().add(lineTo);
		lineTo = new LineTo(x + h34, y + h14);
		this.m_path.getElements().add(lineTo);
		lineTo = new LineTo(x + height, y + h14);
		this.m_path.getElements().add(lineTo);
		lineTo = new LineTo(x + height, y + h34);
		this.m_path.getElements().add(lineTo);
		lineTo = new LineTo(x + h34, y + h34);
		this.m_path.getElements().add(lineTo);
		lineTo = new LineTo(x + h34, y + height);
		this.m_path.getElements().add(lineTo);
		lineTo = new LineTo(x + h14, y + height);
		this.m_path.getElements().add(lineTo);
		lineTo = new LineTo(x + h14, y + h34);
		this.m_path.getElements().add(lineTo);
		lineTo = new LineTo(x, y + h34);
		this.m_path.getElements().add(lineTo);
		lineTo = new LineTo(x, y + h14);
		this.m_path.getElements().add(lineTo);
		lineTo = new LineTo(x + h14, y + h14);
		this.m_path.getElements().add(lineTo);
		this.m_path.getElements().add(new ClosePath());
		return this.m_path;
	}

	/**
	 * Returns a star shape of the given dimensions.
	 */
	public Shape star(final float x, final float y, final float height) {
		final float s = (float) (height / (2 * Math.sin(Math.toRadians(54))));
		final float shortSide = (float) (height / (2 * Math.tan(Math.toRadians(54))));
		final float mediumSide = (float) (s * Math.sin(Math.toRadians(18)));
		final float longSide = (float) (s * Math.cos(Math.toRadians(18)));
		final float innerLongSide = (float) (s / (2 * Math.cos(Math.toRadians(36))));
		final float innerShortSide = innerLongSide * (float) Math.sin(Math.toRadians(36));
		final float innerMediumSide = innerLongSide * (float) Math.cos(Math.toRadians(36));

		this.m_path.getElements().clear();
		final MoveTo moveTo = new MoveTo(x, y + shortSide);
		this.m_path.getElements().add(moveTo);
		LineTo lineTo = new LineTo((x + innerLongSide), (y + shortSide));
		this.m_path.getElements().add(lineTo);
		lineTo = new LineTo((x + (height / 2)), y);
		this.m_path.getElements().add(lineTo);
		lineTo = new LineTo(((x + height) - innerLongSide), (y + shortSide));
		this.m_path.getElements().add(lineTo);
		lineTo = new LineTo((x + height), (y + shortSide));
		this.m_path.getElements().add(lineTo);
		lineTo = new LineTo(((x + height) - innerMediumSide), (y + shortSide + innerShortSide));
		this.m_path.getElements().add(lineTo);
		lineTo = new LineTo(((x + height) - mediumSide), (y + height));
		this.m_path.getElements().add(lineTo);
		lineTo = new LineTo((x + (height / 2)), ((y + shortSide + longSide) - innerShortSide));
		this.m_path.getElements().add(lineTo);
		lineTo = new LineTo((x + mediumSide), (y + height));
		this.m_path.getElements().add(lineTo);
		lineTo = new LineTo((x + innerMediumSide), (y + shortSide + innerShortSide));
		this.m_path.getElements().add(lineTo);
		this.m_path.getElements().add(new ClosePath());
		return this.m_path;
	}

	/**
	 * Returns a hexagon shape of the given dimensions.
	 */
	public Shape hexagon(final float x, final float y, final float height) {
		final float width = height / 2;

		this.m_path.getElements().clear();
		final MoveTo moveTo = new MoveTo(x, y + (0.5f * height));
		this.m_path.getElements().add(moveTo);
		LineTo lineTo = new LineTo(x + (0.5f * width), y);
		this.m_path.getElements().add(lineTo);
		lineTo = new LineTo(x + (1.5f * width), y);
		this.m_path.getElements().add(lineTo);
		lineTo = new LineTo(x + (2.0f * width), y + (0.5f * height));
		this.m_path.getElements().add(lineTo);
		lineTo = new LineTo(x + (1.5f * width), y + height);
		this.m_path.getElements().add(lineTo);
		lineTo = new LineTo(x + (0.5f * width), y + height);
		this.m_path.getElements().add(lineTo);
		this.m_path.getElements().add(new ClosePath());
		return this.m_path;
	}

	/**
	 * Returns a diamond shape of the given dimensions.
	 */
	public Shape diamond(final float x, final float y, final float height) {
		this.m_path.getElements().clear();
		final MoveTo moveTo = new MoveTo(x, (y + (0.5f * height)));
		this.m_path.getElements().add(moveTo);
		LineTo lineTo = new LineTo((x + (0.5f * height)), y);
		this.m_path.getElements().add(lineTo);
		lineTo = new LineTo((x + height), (y + (0.5f * height)));
		this.m_path.getElements().add(lineTo);
		lineTo = new LineTo((x + (0.5f * height)), (y + height));
		this.m_path.getElements().add(lineTo);
		this.m_path.getElements().add(new ClosePath());
		return this.m_path;
	}

} // end of class ShapeRenderer
