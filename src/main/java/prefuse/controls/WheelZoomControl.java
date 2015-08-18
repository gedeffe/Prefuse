package prefuse.controls;

import javafx.geometry.Point2D;
import javafx.scene.input.ScrollEvent;
import prefuse.Display;
import prefuse.visual.VisualItem;

/**
 * Zooms the display using the mouse scroll wheel, changing the scale of the
 * viewable region.
 *
 * @author Kevin Krumwiede
 * @author bobruney
 * @author mathis ahrens
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class WheelZoomControl extends AbstractZoomControl {

	private Point2D m_point = Point2D.ZERO;
	private final boolean inverted;
	private final boolean atPointer;

	/**
	 * Creates a new <tt>WheelZoomControl</tt>. If <tt>inverted</tt> is true,
	 * scrolling the mouse wheel toward you will make the graph appear smaller.
	 * If <tt>atPointer</tt> is true, zooming will be centered on the mouse
	 * pointer instead of the center of the display.
	 *
	 * @param inverted
	 *            true if the scroll direction should be inverted
	 * @param atPointer
	 *            true if zooming should be centered on the mouse pointer
	 */
	public WheelZoomControl(final boolean inverted, final boolean atPointer) {
		this.inverted = inverted;
		this.atPointer = atPointer;
	}

	/**
	 * Creates a new <tt>WheelZoomControl</tt> with the default zoom direction
	 * and zooming on the center of the display.
	 */
	public WheelZoomControl() {
		this(false, false);
	}

	/**
	 * @see prefuse.controls.Control#itemWheelMoved(prefuse.visual.VisualItem,
	 *      javafx.scene.input.ScrollEvent)
	 */
	@Override
	public void itemWheelMoved(final VisualItem item, final ScrollEvent e) {
		if (this.m_zoomOverItem) {
			this.mouseWheelMoved(e);
		}
	}

	/**
	 * @see java.awt.event.MouseWheelListener#mouseWheelMoved(javafx.scene.input.ScrollEvent)
	 */
	@Override
	public void mouseWheelMoved(final ScrollEvent e) {
		final Display display = (Display) e.getSource();
		if (this.atPointer) {
			this.m_point = new Point2D(e.getX(), e.getY());
		} else {
			this.m_point = new Point2D(display.getWidth() / 2, display.getHeight() / 2);
		}
		if (this.inverted) {
			this.zoom(display, this.m_point, 1 - (0.1f * e.getDeltaX()), false);
		} else {
			this.zoom(display, this.m_point, 1 + (0.1f * e.getDeltaX()), false);
		}
	}

} // end of class WheelZoomControl
