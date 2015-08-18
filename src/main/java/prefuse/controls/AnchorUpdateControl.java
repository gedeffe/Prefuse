package prefuse.controls;

import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import prefuse.Display;
import prefuse.action.layout.Layout;
import prefuse.visual.VisualItem;

/**
 * Follows the mouse cursor, updating the anchor parameter for any number of
 * layout instances to match the current cursor position. Will also run a given
 * activity in response to cursor updates.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class AnchorUpdateControl extends ControlAdapter {

	private final boolean m_anchorOverItem;
	private final Layout[] m_layouts;
	private final String m_action;
	private Point2D m_tmp = Point2D.ZERO;

	/**
	 * Create a new AnchorUpdateControl.
	 *
	 * @param layout
	 *            the layout for which to update the anchor point
	 */
	public AnchorUpdateControl(final Layout layout) {
		this(layout, null);
	}

	/**
	 * Create a new AnchorUpdateControl.
	 *
	 * @param layout
	 *            the layout for which to update the anchor point
	 * @param action
	 *            the name of an action to run upon anchor updates
	 */
	public AnchorUpdateControl(final Layout layout, final String action) {
		this(new Layout[] { layout }, action);
	}

	/**
	 * Create a new AnchorUpdateControl.
	 *
	 * @param layout
	 *            the layout for which to update the anchor point
	 * @param action
	 *            the name of an action to run upon anchor updates
	 * @param overItem
	 *            indicates if anchor update events should be processed while
	 *            the mouse cursor is hovered over a VisualItem.
	 */
	public AnchorUpdateControl(final Layout layout, final String action, final boolean overItem) {
		this(new Layout[] { layout }, action, overItem);
	}

	/**
	 * Create a new AnchorUpdateControl.
	 *
	 * @param layout
	 *            the layouts for which to update the anchor point
	 * @param action
	 *            the name of an action to run upon anchor updates
	 */
	public AnchorUpdateControl(final Layout[] layout, final String action) {
		this(layout, action, true);
	}

	/**
	 * Create a new AnchorUpdateControl.
	 *
	 * @param layout
	 *            the layouts for which to update the anchor point
	 * @param action
	 *            the name of an action to run upon anchor updates
	 * @param overItem
	 *            indicates if anchor update events should be processed while
	 *            the mouse cursor is hovered over a VisualItem.
	 */
	public AnchorUpdateControl(final Layout[] layout, final String action, final boolean overItem) {
		this.m_layouts = layout.clone();
		this.m_action = action;
		this.m_anchorOverItem = overItem;
	}

	// ------------------------------------------------------------------------

	/**
	 * @see java.awt.event.MouseListener#mouseExited(javafx.scene.input.MouseEvent)
	 */
	@Override
	public void mouseExited(final MouseEvent e) {
		for (int i = 0; i < this.m_layouts.length; i++) {
			this.m_layouts[i].setLayoutAnchor(null);
		}
		this.runAction(e);
	}

	/**
	 * @see java.awt.event.MouseMotionListener#mouseMoved(javafx.scene.input.MouseEvent)
	 */
	@Override
	public void mouseMoved(final MouseEvent e) {
		this.moveEvent(e);
	}

	/**
	 * @see java.awt.event.MouseMotionListener#mouseDragged(javafx.scene.input.MouseEvent)
	 */
	@Override
	public void mouseDragged(final MouseEvent e) {
		this.moveEvent(e);
	}

	/**
	 * @see prefuse.controls.Control#itemDragged(prefuse.visual.VisualItem,
	 *      javafx.scene.input.MouseEvent)
	 */
	@Override
	public void itemDragged(final VisualItem item, final MouseEvent e) {
		if (this.m_anchorOverItem) {
			this.moveEvent(e);
		}
	}

	/**
	 * @see prefuse.controls.Control#itemMoved(prefuse.visual.VisualItem,
	 *      javafx.scene.input.MouseEvent)
	 */
	@Override
	public void itemMoved(final VisualItem item, final MouseEvent e) {
		if (this.m_anchorOverItem) {
			this.moveEvent(e);
		}
	}

	/**
	 * Registers a mouse move event, updating the anchor point for all
	 * registered layout instances.
	 *
	 * @param e
	 *            the MouseEvent
	 */
	public void moveEvent(final MouseEvent e) {
		final Display d = (Display) e.getSource();
		this.m_tmp = d.getAbsoluteCoordinate(new Point2D(e.getX(), e.getY()));
		for (int i = 0; i < this.m_layouts.length; i++) {
			this.m_layouts[i].setLayoutAnchor(this.m_tmp);
		}
		this.runAction(e);
	}

	/**
	 * Runs an optional action upon anchor update.
	 *
	 * @param e
	 *            MouseEvent
	 */
	private void runAction(final MouseEvent e) {
		if (this.m_action != null) {
			final Display d = (Display) e.getSource();
			d.getVisualization().run(this.m_action);
		}
	}

} // end of class AnchorUpdateControl
