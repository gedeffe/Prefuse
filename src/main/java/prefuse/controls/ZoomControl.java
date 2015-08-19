package prefuse.controls;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import prefuse.Display;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualItem;

/**
 * Zooms the display, changing the scale of the viewable region. By default,
 * zooming is achieved by pressing the right mouse button on the background of
 * the visualization and dragging the mouse up or down. Moving the mouse up
 * zooms out the display around the spot the mouse was originally pressed.
 * Moving the mouse down similarly zooms in the display, making items larger.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class ZoomControl extends AbstractZoomControl {

	private double yLast;
	private Point2D down = Point2D.ZERO;
	private MouseButton button = RIGHT_MOUSE_BUTTON;

	/**
	 * Create a new zoom control.
	 */
	public ZoomControl() {
		// do nothing
	}

	/**
	 * Create a new zoom control.
	 *
	 * @param mouseButton
	 *            the mouse button that should initiate a zoom. One of
	 *            {@link Control#LEFT_MOUSE_BUTTON},
	 *            {@link Control#MIDDLE_MOUSE_BUTTON}, or
	 *            {@link Control#RIGHT_MOUSE_BUTTON}.
	 */
	public ZoomControl(final MouseButton mouseButton) {
		this.button = mouseButton;
	}

	/**
	 * @see java.awt.event.MouseListener#mousePressed(javafx.scene.input.MouseEvent)
	 */
	@Override
	public void mousePressed(final MouseEvent e) {
		if (UILib.isButtonPressed(e, this.button)) {
			final Display display = (Display) e.getSource();
			if (display.isTranformInProgress()) {
				this.yLast = -1;
				System.err.println("can't move");
				return;
			}
			display.setCursor(Cursor.N_RESIZE);
			this.down = display.getAbsoluteCoordinate(new Point2D(e.getX(), e.getY()));
			this.yLast = e.getY();
		}
	}

	/**
	 * @see java.awt.event.MouseMotionListener#mouseDragged(javafx.scene.input.MouseEvent)
	 */
	@Override
	public void mouseDragged(final MouseEvent e) {
		if (UILib.isButtonPressed(e, this.button)) {
			final Display display = (Display) e.getSource();
			if (display.isTranformInProgress() || (this.yLast == -1)) {
				this.yLast = -1;
				return;
			}

			final double y = e.getY();
			final double dy = y - this.yLast;
			final double zoom = 1 + ((dy) / 100);

			final int status = this.zoom(display, this.down, zoom, true);
			Cursor cursor = Cursor.N_RESIZE;
			if (status == NO_ZOOM) {
				cursor = Cursor.WAIT;
			}
			display.setCursor(cursor);

			this.yLast = y;
		}
	}

	/**
	 * @see java.awt.event.MouseListener#mouseReleased(javafx.scene.input.MouseEvent)
	 */
	@Override
	public void mouseReleased(final MouseEvent e) {
		if (UILib.isButtonPressed(e, this.button)) {
			((Display) e.getSource()).setCursor(Cursor.DEFAULT);
		}
	}

	/**
	 * @see prefuse.controls.Control#itemPressed(prefuse.visual.VisualItem,
	 *      javafx.scene.input.MouseEvent)
	 */
	@Override
	public void itemPressed(final VisualItem item, final MouseEvent e) {
		if (this.m_zoomOverItem) {
			this.mousePressed(e);
		}
	}

	/**
	 * @see prefuse.controls.Control#itemDragged(prefuse.visual.VisualItem,
	 *      javafx.scene.input.MouseEvent)
	 */
	@Override
	public void itemDragged(final VisualItem item, final MouseEvent e) {
		if (this.m_zoomOverItem) {
			this.mouseDragged(e);
		}
	}

	/**
	 * @see prefuse.controls.Control#itemReleased(prefuse.visual.VisualItem,
	 *      javafx.scene.input.MouseEvent)
	 */
	@Override
	public void itemReleased(final VisualItem item, final MouseEvent e) {
		if (this.m_zoomOverItem) {
			this.mouseReleased(e);
		}
	}

} // end of class ZoomControl
