package prefuse.util;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.QuadCurveTo;
import javafx.scene.shape.Shape;
import javafx.scene.shape.VLineTo;

/**
 * Library to render a Shape used to store and compute information with a
 * GraphicsContext. It means that we have not to add the Shape into a Canvas or
 * another Node, and manage the status of the Shape if we want to replace it.
 *
 * For instance, if we start with a Rectangle instance, then we would like to
 * translate it. In standard JavaFX, we would just add the Affine tranformation
 * into collection of Transform into the Rectangle instance (using
 * getTransforms()).
 *
 * In Prefuse, as we have an internal representation of a VisualItem, and in
 * order to migrate it smoothly, we will use this backport to draw a Shape
 * manually using the GraphicsContext.
 */
public class JavaFxLib {

	/**
	 * To draw a shape manually with a provided GraphicsContext. It means, that
	 * current Canvas (which provided the GraphicsContext instance) has not been
	 * used to host provided Shape instance.
	 *
	 * The Shape instance is only here as a datastore.
	 *
	 * @param shape
	 *            data to use to draw an element with the GraphicsContext.
	 * @param graphicsContext
	 *            instance associated to main canvas.
	 */
	public static void drawShape(final Shape shape, final GraphicsContext graphicsContext) {
		if (shape instanceof Path) {
			drawPath((Path) shape, graphicsContext);
		}
	}

	/**
	 * Derived method from https://github.com/Xenoage/Zong/blob/
	 * 0f0d6a721fb027d1d70dc049d3fe0c2350be4596/renderer-javafx/src/com/xenoage/
	 * zong/renderer/javafx/symbols/JfxPath.java
	 *
	 * @param path
	 * @param context
	 */
	public static void drawPath(final Path path, final GraphicsContext context) {
		context.beginPath();
		for (final PathElement e : path.getElements()) {
			if (e instanceof ClosePath) {
				context.closePath();
			} else if (e instanceof CubicCurveTo) {
				final CubicCurveTo c = (CubicCurveTo) e;
				context.bezierCurveTo(c.getControlX1(), c.getControlY1(), c.getControlX2(), c.getControlY2(), c.getX(),
						c.getY());
			} else if (e instanceof LineTo) {
				final LineTo l = (LineTo) e;
				context.lineTo(l.getX(), l.getY());
			} else if (e instanceof MoveTo) {
				final MoveTo m = (MoveTo) e;
				context.moveTo(m.getX(), m.getY());
			} else if (e instanceof QuadCurveTo) {
				final QuadCurveTo q = (QuadCurveTo) e;
				context.quadraticCurveTo(q.getControlX(), q.getControlY(), q.getX(), q.getY());
			} else if (e instanceof ArcTo) {
				final ArcTo q = (ArcTo) e;
				// well, I am not sure about this backport ...
				context.arcTo(q.getX(), q.getY(), q.getRadiusX(), q.getRadiusY(), q.getXAxisRotation());
			} else if (e instanceof HLineTo) {
				final HLineTo q = (HLineTo) e;
				// how to draw an horizontal line if we don't know where we are
				// ?
				context.lineTo(q.getX(), 0);
			} else if (e instanceof VLineTo) {
				final VLineTo q = (VLineTo) e;
				// how to draw a vertical line if we don't know where we are ?
				context.lineTo(0, q.getY());
			}
		}
	}

}
