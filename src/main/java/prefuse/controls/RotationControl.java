package prefuse.controls;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import prefuse.Display;
import prefuse.util.ui.UILib;

/**
 * Control that can be used to rotate the display. This results in a
 * transformation of the display itself, such that all aspects are rotated. For
 * example, after a rotation of 180 degrees, upright text strings will
 * subsequently upside down. To rotate item positions but leave other aspects
 * such as orientation intact, you can instead create a new
 * {@link prefuse.action.Action} module that rotates just the item co-ordinates.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class RotationControl extends ControlAdapter {

	private Point2D down = Point2D.ZERO;
	private double baseAngle = 0; // the baseline angle of the rotation
	private final MouseButton m_button; // the mouse button to use

	/**
	 * Create a new RotateControl. Rotations will be initiated by dragging the
	 * mouse with the left mouse button pressed.
	 */
	public RotationControl() {
		this(Control.LEFT_MOUSE_BUTTON);
	}

	/**
	 * Create a new RotateControl
	 *
	 * @param mouseButton
	 *            the mouse button that should initiate a rotation. One of
	 *            {@link Control#LEFT_MOUSE_BUTTON},
	 *            {@link Control#MIDDLE_MOUSE_BUTTON}, or
	 *            {@link Control#RIGHT_MOUSE_BUTTON}.
	 */
	public RotationControl(final MouseButton mouseButton) {
		this.m_button = mouseButton;
	}

	/**
	 * @see java.awt.event.MouseListener#mousePressed(javafx.scene.input.MouseEvent)
	 */
	@Override
	public void mousePressed(final MouseEvent e) {
		if (UILib.isButtonPressed(e, this.m_button)) {
			final Display display = (Display) e.getSource();
			display.setCursor(Cursor.E_RESIZE);
			this.down = new Point2D(e.getX(), e.getY());
			this.baseAngle = Double.NaN;
		}
	}

	/**
	 * @see java.awt.event.MouseMotionListener#mouseDragged(javafx.scene.input.MouseEvent)
	 */
	@Override
	public void mouseDragged(final MouseEvent e) {
		if (UILib.isButtonPressed(e, this.m_button)) {
			final double dy = e.getY() - this.down.getY();
			final double dx = e.getX() - this.down.getX();
			final double angle = Math.atan2(dy, dx);

			// only rotate once the base angle has been established
			if (!Double.isNaN(this.baseAngle)) {
				final Display display = (Display) e.getSource();
				display.rotate(this.down, angle - this.baseAngle);
			}
			this.baseAngle = angle;
		}
	}

	/**
	 * @see java.awt.event.MouseListener#mouseReleased(javafx.scene.input.MouseEvent)
	 */
	@Override
	public void mouseReleased(final MouseEvent e) {
		if (UILib.isButtonPressed(e, this.m_button)) {
			((Display) e.getSource()).setCursor(Cursor.DEFAULT);
		}
	}

} // end of class RotationControl
