package prefuse.util;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Shape;

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

	}
}
