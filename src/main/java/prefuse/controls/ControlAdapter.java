package prefuse.controls;

import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import prefuse.visual.VisualItem;

/**
 * Adapter class for processing prefuse interface events. Subclasses can
 * override the desired methods to perform user interface event handling.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class ControlAdapter implements Control {

	private boolean m_enabled = true;

	/**
	 * @see prefuse.controls.Control#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return this.m_enabled;
	}

	/**
	 * @see prefuse.controls.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(final boolean enabled) {
		this.m_enabled = enabled;
	}

	// ------------------------------------------------------------------------

	/**
	 * @see prefuse.controls.Control#itemDragged(prefuse.visual.VisualItem,
	 *      javafx.scene.input.MouseEvent)
	 */
	@Override
	public void itemDragged(final VisualItem item, final MouseEvent e) {
	}

	/**
	 * @see prefuse.controls.Control#itemMoved(prefuse.visual.VisualItem,
	 *      javafx.scene.input.MouseEvent)
	 */
	@Override
	public void itemMoved(final VisualItem item, final MouseEvent e) {
	}

	/**
	 * @see prefuse.controls.Control#itemWheelMoved(prefuse.visual.VisualItem,
	 *      javafx.scene.input.ScrollEvent)
	 */
	@Override
	public void itemWheelMoved(final VisualItem item, final ScrollEvent e) {
	}

	/**
	 * @see prefuse.controls.Control#itemClicked(prefuse.visual.VisualItem,
	 *      javafx.scene.input.MouseEvent)
	 */
	@Override
	public void itemClicked(final VisualItem item, final MouseEvent e) {
	}

	/**
	 * @see prefuse.controls.Control#itemPressed(prefuse.visual.VisualItem,
	 *      javafx.scene.input.MouseEvent)
	 */
	@Override
	public void itemPressed(final VisualItem item, final MouseEvent e) {
	}

	/**
	 * @see prefuse.controls.Control#itemReleased(prefuse.visual.VisualItem,
	 *      javafx.scene.input.MouseEvent)
	 */
	@Override
	public void itemReleased(final VisualItem item, final MouseEvent e) {
	}

	/**
	 * @see prefuse.controls.Control#itemEntered(prefuse.visual.VisualItem,
	 *      javafx.scene.input.MouseEvent)
	 */
	@Override
	public void itemEntered(final VisualItem item, final MouseEvent e) {
	}

	/**
	 * @see prefuse.controls.Control#itemExited(prefuse.visual.VisualItem,
	 *      javafx.scene.input.MouseEvent)
	 */
	@Override
	public void itemExited(final VisualItem item, final MouseEvent e) {
	}

	/**
	 * @see prefuse.controls.Control#itemKeyPressed(prefuse.visual.VisualItem,
	 *      java.awt.event.KeyEvent)
	 */
	@Override
	public void itemKeyPressed(final VisualItem item, final KeyEvent e) {
	}

	/**
	 * @see prefuse.controls.Control#itemKeyReleased(prefuse.visual.VisualItem,
	 *      java.awt.event.KeyEvent)
	 */
	@Override
	public void itemKeyReleased(final VisualItem item, final KeyEvent e) {
	}

	/**
	 * @see prefuse.controls.Control#itemKeyTyped(prefuse.visual.VisualItem,
	 *      java.awt.event.KeyEvent)
	 */
	@Override
	public void itemKeyTyped(final VisualItem item, final KeyEvent e) {
	}

	/**
	 * @see prefuse.controls.Control#mouseEntered(javafx.scene.input.MouseEvent)
	 */
	@Override
	public void mouseEntered(final MouseEvent e) {
	}

	/**
	 * @see prefuse.controls.Control#mouseExited(javafx.scene.input.MouseEvent)
	 */
	@Override
	public void mouseExited(final MouseEvent e) {
	}

	/**
	 * @see prefuse.controls.Control#mousePressed(javafx.scene.input.MouseEvent)
	 */
	@Override
	public void mousePressed(final MouseEvent e) {
	}

	/**
	 * @see prefuse.controls.Control#mouseReleased(javafx.scene.input.MouseEvent)
	 */
	@Override
	public void mouseReleased(final MouseEvent e) {
	}

	/**
	 * @see prefuse.controls.Control#mouseClicked(javafx.scene.input.MouseEvent)
	 */
	@Override
	public void mouseClicked(final MouseEvent e) {
	}

	/**
	 * @see prefuse.controls.Control#mouseDragged(javafx.scene.input.MouseEvent)
	 */
	@Override
	public void mouseDragged(final MouseEvent e) {
	}

	/**
	 * @see prefuse.controls.Control#mouseMoved(javafx.scene.input.MouseEvent)
	 */
	@Override
	public void mouseMoved(final MouseEvent e) {
	}

	/**
	 * @see prefuse.controls.Control#mouseWheelMoved(javafx.scene.input.ScrollEvent)
	 */
	@Override
	public void mouseWheelMoved(final ScrollEvent e) {
	}

	/**
	 * @see prefuse.controls.Control#keyPressed(java.awt.event.KeyEvent)
	 */
	@Override
	public void keyPressed(final KeyEvent e) {
	}

	/**
	 * @see prefuse.controls.Control#keyReleased(java.awt.event.KeyEvent)
	 */
	@Override
	public void keyReleased(final KeyEvent e) {
	}

	/**
	 * @see prefuse.controls.Control#keyTyped(java.awt.event.KeyEvent)
	 */
	@Override
	public void keyTyped(final KeyEvent e) {
	}

} // end of class ControlAdapter
