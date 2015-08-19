package prefuse.controls;

import java.util.Iterator;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import prefuse.Display;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 * Control that changes the location of a whole subtree when dragged on screen.
 * This is similar to the {@link DragControl DragControl} class, except that it
 * moves the entire visible subtree rooted at an item, rather than just the item
 * itself.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class SubtreeDragControl extends ControlAdapter {

	private Point2D down = Point2D.ZERO;
	private Point2D tmp = Point2D.ZERO;
	private boolean wasFixed;

	/**
	 * Creates a new subtree drag control that issues repaint requests as an
	 * item is dragged.
	 */
	public SubtreeDragControl() {
	}

	/**
	 * @see prefuse.controls.Control#itemEntered(prefuse.visual.VisualItem,
	 *      javafx.scene.input.MouseEvent)
	 */
	@Override
	public void itemEntered(final VisualItem item, final MouseEvent e) {
		if (!(item instanceof NodeItem)) {
			return;
		}
		final Display d = (Display) e.getSource();
		d.setCursor(Cursor.HAND);
	}

	/**
	 * @see prefuse.controls.Control#itemExited(prefuse.visual.VisualItem,
	 *      javafx.scene.input.MouseEvent)
	 */
	@Override
	public void itemExited(final VisualItem item, final MouseEvent e) {
		if (!(item instanceof NodeItem)) {
			return;
		}
		final Display d = (Display) e.getSource();
		d.setCursor(Cursor.DEFAULT);
	}

	/**
	 * @see prefuse.controls.Control#itemPressed(prefuse.visual.VisualItem,
	 *      javafx.scene.input.MouseEvent)
	 */
	@Override
	public void itemPressed(final VisualItem item, final MouseEvent e) {
		if (e.getButton() != Control.LEFT_MOUSE_BUTTON) {
			return;
		}
		if (!(item instanceof NodeItem)) {
			return;
		}
		final Display d = (Display) e.getSource();
		this.down = d.getAbsoluteCoordinate(new Point2D(e.getX(), e.getY()));
		this.wasFixed = item.isFixed();
		item.setFixed(true);
	}

	/**
	 * @see prefuse.controls.Control#itemReleased(prefuse.visual.VisualItem,
	 *      javafx.scene.input.MouseEvent)
	 */
	@Override
	public void itemReleased(final VisualItem item, final MouseEvent e) {
		if (e.getButton() != Control.LEFT_MOUSE_BUTTON) {
			return;
		}
		if (!(item instanceof NodeItem)) {
			return;
		}
		item.setFixed(this.wasFixed);
	}

	/**
	 * @see prefuse.controls.Control#itemDragged(prefuse.visual.VisualItem,
	 *      javafx.scene.input.MouseEvent)
	 */
	@Override
	public void itemDragged(final VisualItem item, final MouseEvent e) {
		if (e.getButton() != Control.LEFT_MOUSE_BUTTON) {
			return;
		}
		if (!(item instanceof NodeItem)) {
			return;
		}
		final Display d = (Display) e.getSource();
		this.tmp = d.getAbsoluteCoordinate(new Point2D(e.getX(), e.getY()));
		final double dx = this.tmp.getX() - this.down.getX();
		final double dy = this.tmp.getY() - this.down.getY();
		this.updateLocations((NodeItem) item, dx, dy);
		this.down = this.tmp;
		item.getVisualization().repaint();
	}

	private void updateLocations(final NodeItem n, final double dx, final double dy) {
		double x = n.getX(), y = n.getY();
		n.setStartX(x);
		n.setStartY(y);
		x += dx;
		y += dy;
		n.setX(x);
		n.setY(y);
		n.setEndX(x);
		n.setEndY(y);

		final Iterator children = n.children();
		while (children.hasNext()) {
			this.updateLocations((NodeItem) children.next(), dx, dy);
		}
	}

} // end of class SubtreeDragControl
