package prefuse.render;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Affine;
import prefuse.util.GraphicsLib;
import prefuse.visual.VisualItem;

/**
 * <p>
 * Abstract base class implementation of the Renderer interface for supporting
 * the drawing of basic shapes. Subclasses should override the
 * {@link #getRawShape(VisualItem) getRawShape} method, which returns the shape
 * to draw. Optionally, subclasses can also override the
 * {@link #getTransform(VisualItem) getTransform} method to apply a desired
 * <code>AffineTransform</code> to the shape.
 * </p>
 *
 * <p>
 * <b>NOTE:</b> For more efficient rendering, subclasses should use a single
 * shape instance in memory, and update its parameters on each call to
 * getRawShape, rather than allocating a new Shape object each time. Otherwise,
 * a new object will be allocated every time something needs to be drawn, and
 * then subsequently be arbage collected. This can significantly reduce
 * performance, especially when there are many things to draw.
 * </p>
 *
 * @version 1.0
 * @author alan newberger
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public abstract class AbstractShapeRenderer implements Renderer {

	public static final int RENDER_TYPE_NONE = 0;
	public static final int RENDER_TYPE_DRAW = 1;
	public static final int RENDER_TYPE_FILL = 2;
	public static final int RENDER_TYPE_DRAW_AND_FILL = 3;

	private int m_renderType = RENDER_TYPE_DRAW_AND_FILL;
	protected Affine m_transform = new Affine();
	protected boolean m_manageBounds = true;

	public void setManageBounds(final boolean b) {
		this.m_manageBounds = b;
	}

	/**
	 * @see prefuse.render.Renderer#render(javafx.scene.canvas.GraphicsContext,
	 *      prefuse.visual.VisualItem)
	 */
	@Override
	public void render(final GraphicsContext g, final VisualItem item) {
		final Shape shape = this.getShape(item);
		if (shape != null) {
			this.drawShape(g, item, shape);
		}
	}

	/**
	 * Draws the specified shape into the provided Graphics context, using
	 * stroke and fill color values from the specified VisualItem. This method
	 * can be called by subclasses in custom rendering routines.
	 */
	protected void drawShape(final GraphicsContext g, final VisualItem item, final Shape shape) {
		GraphicsLib.paint(g, item, shape, this.getStroke(item), this.getRenderType(item));
	}

	/**
	 * Returns the shape describing the boundary of an item. The shape's
	 * coordinates should be in abolute (item-space) coordinates.
	 *
	 * @param item
	 *            the item for which to get the Shape
	 */
	public Shape getShape(final VisualItem item) {
		final Affine at = this.getTransform(item);
		final Shape rawShape = this.getRawShape(item);
		if ((at != null) && (rawShape != null)) {
			// we should not add the transformation several times
			if (!rawShape.getTransforms().contains(rawShape)) {
				rawShape.getTransforms().add(at);
			}
		}
		return rawShape;
	}

	/**
	 * Retursn the stroke to use for drawing lines and shape outlines. By
	 * default returns the value of {@link VisualItem#getStroke()}. Subclasses
	 * can override this method to implement custom stroke assignment, though
	 * changing the <code>VisualItem</code>'s stroke value is preferred.
	 *
	 * @param item
	 *            the VisualItem
	 * @return the strok to use for drawing lines and shape outlines
	 */
	protected Paint getStroke(final VisualItem item) {
		return item.getStroke();
	}

	/**
	 * Return a non-transformed shape for the visual representation of the item.
	 * Subclasses must implement this method.
	 *
	 * @param item
	 *            the VisualItem being drawn
	 * @return the "raw", untransformed shape.
	 */
	protected abstract Shape getRawShape(VisualItem item);

	/**
	 * Return the graphics space transform applied to this item's shape, if any.
	 * Subclasses can implement this method, otherwise it will return null to
	 * indicate no transformation is needed.
	 *
	 * @param item
	 *            the VisualItem
	 * @return the graphics space transform, or null if none
	 */
	protected Affine getTransform(final VisualItem item) {
		return null;
	}

	/**
	 * Returns a value indicating if a shape is drawn by its outline, by a fill,
	 * or both. The default is to draw both.
	 *
	 * @return the rendering type
	 */
	public int getRenderType(final VisualItem item) {
		return this.m_renderType;
	}

	/**
	 * Sets a value indicating if a shape is drawn by its outline, by a fill, or
	 * both. The default is to draw both.
	 *
	 * @param type
	 *            the new rendering type. Should be one of
	 *            {@link #RENDER_TYPE_NONE}, {@link #RENDER_TYPE_DRAW},
	 *            {@link #RENDER_TYPE_FILL}, or
	 *            {@link #RENDER_TYPE_DRAW_AND_FILL}.
	 */
	public void setRenderType(final int type) {
		if ((type < RENDER_TYPE_NONE) || (type > RENDER_TYPE_DRAW_AND_FILL)) {
			throw new IllegalArgumentException("Unrecognized render type.");
		}
		this.m_renderType = type;
	}

	/**
	 * @see prefuse.render.Renderer#locatePoint(javafx.geometry.Point2D,
	 *      prefuse.visual.VisualItem)
	 */
	@Override
	public boolean locatePoint(final Point2D p, final VisualItem item) {
		if (item.getBounds().contains(p)) {
			// if within bounds, check within shape outline
			final Shape s = this.getShape(item);
			return (s != null ? s.contains(p) : false);
		} else {
			return false;
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
		} else {
			GraphicsLib.setBounds(item, shape, this.getStroke(item));
		}
	}

} // end of abstract class AbstractShapeRenderer
