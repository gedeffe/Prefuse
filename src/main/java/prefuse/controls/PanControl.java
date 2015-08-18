package prefuse.controls;

import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import prefuse.Display;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualItem;

/**
 * Pans the display, changing the viewable region of the visualization. By
 * default, panning is accomplished by clicking on the background of a
 * visualization with the left mouse button and then dragging.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class PanControl extends ControlAdapter {

	private final boolean m_panOverItem;
	double m_xDown;
	private double m_yDown;
	private final MouseButton m_button;

	/**
	 * Create a new PanControl.
	 */
	public PanControl() {
		this(LEFT_MOUSE_BUTTON, false);
	}

	/**
	 * Create a new PanControl.
	 *
	 * @param panOverItem
	 *            if true, the panning control will work even while the mouse is
	 *            over a visual item.
	 */
	public PanControl(final boolean panOverItem) {
		this(LEFT_MOUSE_BUTTON, panOverItem);
	}

	/**
	 * Create a new PanControl.
	 *
	 * @param mouseButton
	 *            the mouse button that should initiate a pan. One of
	 *            {@link Control#LEFT_MOUSE_BUTTON},
	 *            {@link Control#MIDDLE_MOUSE_BUTTON}, or
	 *            {@link Control#RIGHT_MOUSE_BUTTON}.
	 */
	public PanControl(final MouseButton mouseButton) {
		this(mouseButton, false);
	}

	/**
	 * Create a new PanControl
	 *
	 * @param mouseButton
	 *            the mouse button that should initiate a pan. One of
	 *            {@link Control#LEFT_MOUSE_BUTTON},
	 *            {@link Control#MIDDLE_MOUSE_BUTTON}, or
	 *            {@link Control#RIGHT_MOUSE_BUTTON}.
	 * @param panOverItem
	 *            if true, the panning control will work even while the mouse is
	 *            over a visual item.
	 */
	public PanControl(final MouseButton mouseButton, final boolean panOverItem) {
		this.m_button = mouseButton;
		this.m_panOverItem = panOverItem;
	}

	// ------------------------------------------------------------------------

	/**
	 * @see java.awt.event.MouseListener#mousePressed(javafx.scene.input.MouseEvent)
	 */
	@Override
	public void mousePressed(final MouseEvent e) {
		if (UILib.isButtonPressed(e, this.m_button)) {
			((Display) e.getSource()).setCursor(Cursor.MOVE);
			this.m_xDown = e.getX();
			this.m_yDown = e.getY();
		}
	}

	/**
	 * @see java.awt.event.MouseMotionListener#mouseDragged(javafx.scene.input.MouseEvent)
	 */
	@Override
	public void mouseDragged(final MouseEvent e) {
		if (UILib.isButtonPressed(e, this.m_button)) {
			final Display display = (Display) e.getSource();
			final double x = e.getX(), y = e.getY();
			final double dx = x - this.m_xDown, dy = y - this.m_yDown;
			display.pan(dx, dy);
			this.m_xDown = x;
			this.m_yDown = y;
			display.repaint();
		}
	}

	/**
	 * @see java.awt.event.MouseListener#mouseReleased(javafx.scene.input.MouseEvent)
	 */
	@Override
	public void mouseReleased(final MouseEvent e) {
		if (UILib.isButtonPressed(e, this.m_button)) {
			((Display) e.getSource()).setCursor(Cursor.DEFAULT);
			this.m_xDown = -1;
			this.m_yDown = -1;
		}
	}

	/**
	 * @see prefuse.controls.Control#itemPressed(prefuse.visual.VisualItem,
	 *      javafx.scene.input.MouseEvent)
	 */
	@Override
	public void itemPressed(final VisualItem item, final MouseEvent e) {
		if (this.m_panOverItem) {
			this.mousePressed(e);
		}
	}

	/**
	 * @see prefuse.controls.Control#itemDragged(prefuse.visual.VisualItem,
	 *      javafx.scene.input.MouseEvent)
	 */
	@Override
	public void itemDragged(final VisualItem item, final MouseEvent e) {
		if (this.m_panOverItem) {
			this.mouseDragged(e);
		}
	}

	/**
	 * @see prefuse.controls.Control#itemReleased(prefuse.visual.VisualItem,
	 *      javafx.scene.input.MouseEvent)
	 */
	@Override
	public void itemReleased(final VisualItem item, final MouseEvent e) {
		if (this.m_panOverItem) {
			this.mouseReleased(e);
		}
	}

} // end of class PanControl
