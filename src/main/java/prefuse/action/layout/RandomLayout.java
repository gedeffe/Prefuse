package prefuse.action.layout;

import java.util.Iterator;
import java.util.Random;

import javafx.geometry.Rectangle2D;
import prefuse.visual.VisualItem;

/**
 * Performs a random layout of items within the layout bounds.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class RandomLayout extends Layout {

	private final Random r = new Random(12345678L);

	/**
	 * Create a new RandomLayout that processes all items.
	 */
	public RandomLayout() {
		super();
	}

	/**
	 * Create a new RandomLayout.
	 *
	 * @param group
	 *            the data group to layout
	 */
	public RandomLayout(final String group) {
		super(group);
	}

	/**
	 * Set the seed value for the random number generator.
	 *
	 * @param seed
	 *            the random seed value
	 */
	public void setRandomSeed(final long seed) {
		this.r.setSeed(seed);
	}

	/**
	 * @see prefuse.action.Action#run(double)
	 */
	@Override
	public void run(final double frac) {
		final Rectangle2D b = this.getLayoutBounds();
		double x, y;
		final double w = b.getWidth();
		final double h = b.getHeight();
		final Iterator iter = this.getVisualization().visibleItems(this.m_group);
		while (iter.hasNext()) {
			final VisualItem item = (VisualItem) iter.next();
			x = (int) (b.getMinX() + (this.r.nextDouble() * w));
			y = (int) (b.getMinY() + (this.r.nextDouble() * h));
			this.setX(item, null, x);
			this.setY(item, null, y);
		}
	}

} // end of class RandomLayout
