package prefuse.controls;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import prefuse.Display;
import prefuse.data.Table;
import prefuse.data.event.EventConstants;
import prefuse.data.event.TableListener;
import prefuse.visual.VisualItem;

/**
 * Changes a node's location when dragged on screen. Other effects include
 * fixing a node's position when the mouse if over it, and changing the mouse
 * cursor to a hand when the mouse passes over an item.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class DragControl extends ControlAdapter implements TableListener {

	private VisualItem activeItem;
	protected String action;
	protected Point2D down = Point2D.ZERO;
	protected Point2D temp = Point2D.ZERO;
	protected boolean dragged, wasFixed, resetItem;
	private boolean fixOnMouseOver = true;
	protected boolean repaint = true;

	/**
	 * Creates a new drag control that issues repaint requests as an item is
	 * dragged.
	 */
	public DragControl() {
	}

	/**
	 * Creates a new drag control that optionally issues repaint requests as an
	 * item is dragged.
	 *
	 * @param repaint
	 *            indicates whether or not repaint requests are issued as drag
	 *            events occur. This can be set to false if other activities
	 *            (for example, a continuously running force simulation) are
	 *            already issuing repaint events.
	 */
	public DragControl(final boolean repaint) {
		this.repaint = repaint;
	}

	/**
	 * Creates a new drag control that optionally issues repaint requests as an
	 * item is dragged.
	 *
	 * @param repaint
	 *            indicates whether or not repaint requests are issued as drag
	 *            events occur. This can be set to false if other activities
	 *            (for example, a continuously running force simulation) are
	 *            already issuing repaint events.
	 * @param fixOnMouseOver
	 *            indicates if object positions should become fixed (made
	 *            stationary) when the mouse pointer is over an item.
	 */
	public DragControl(final boolean repaint, final boolean fixOnMouseOver) {
		this.repaint = repaint;
		this.fixOnMouseOver = fixOnMouseOver;
	}

	/**
	 * Creates a new drag control that invokes an action upon drag events.
	 *
	 * @param action
	 *            the action to run when drag events occur.
	 */
	public DragControl(final String action) {
		this.repaint = false;
		this.action = action;
	}

	/**
	 * Creates a new drag control that invokes an action upon drag events.
	 *
	 * @param action
	 *            the action to run when drag events occur
	 * @param fixOnMouseOver
	 *            indicates if object positions should become fixed (made
	 *            stationary) when the mouse pointer is over an item.
	 */
	public DragControl(final String action, final boolean fixOnMouseOver) {
		this.repaint = false;
		this.fixOnMouseOver = fixOnMouseOver;
		this.action = action;
	}

	/**
	 * Determines whether or not an item should have it's position fixed when
	 * the mouse moves over it.
	 *
	 * @param s
	 *            whether or not item position should become fixed upon mouse
	 *            over.
	 */
	public void setFixPositionOnMouseOver(final boolean s) {
		this.fixOnMouseOver = s;
	}

	/**
	 * @see prefuse.controls.Control#itemEntered(prefuse.visual.VisualItem,
	 *      javafx.scene.input.MouseEvent)
	 */
	@Override
	public void itemEntered(final VisualItem item, final MouseEvent e) {
		final Display d = (Display) e.getSource();
		d.setCursor(Cursor.HAND);
		this.activeItem = item;
		if (this.fixOnMouseOver) {
			this.wasFixed = item.isFixed();
			this.resetItem = true;
			item.setFixed(true);
			item.getTable().addTableListener(this);
		}
	}

	/**
	 * @see prefuse.controls.Control#itemExited(prefuse.visual.VisualItem,
	 *      javafx.scene.input.MouseEvent)
	 */
	@Override
	public void itemExited(final VisualItem item, final MouseEvent e) {
		if (this.activeItem == item) {
			this.activeItem = null;
			item.getTable().removeTableListener(this);
			if (this.resetItem) {
				item.setFixed(this.wasFixed);
			}
		}
		final Display d = (Display) e.getSource();
		d.setCursor(Cursor.DEFAULT);
	} //

	/**
	 * @see prefuse.controls.Control#itemPressed(prefuse.visual.VisualItem,
	 *      javafx.scene.input.MouseEvent)
	 */
	@Override
	public void itemPressed(final VisualItem item, final MouseEvent e) {
		if (e.getButton() != MouseButton.PRIMARY) {
			return;
		}
		if (!this.fixOnMouseOver) {
			this.wasFixed = item.isFixed();
			this.resetItem = true;
			item.setFixed(true);
			item.getTable().addTableListener(this);
		}
		this.dragged = false;
		final Display d = (Display) e.getSource();
		this.down = d.getAbsoluteCoordinate(new Point2D(e.getX(), e.getY()));
	}

	/**
	 * @see prefuse.controls.Control#itemReleased(prefuse.visual.VisualItem,
	 *      javafx.scene.input.MouseEvent)
	 */
	@Override
	public void itemReleased(final VisualItem item, final MouseEvent e) {
		if (e.getButton() != MouseButton.PRIMARY) {
			return;
		}
		if (this.dragged) {
			this.activeItem = null;
			item.getTable().removeTableListener(this);
			if (this.resetItem) {
				item.setFixed(this.wasFixed);
			}
			this.dragged = false;
		}
	}

	/**
	 * @see prefuse.controls.Control#itemDragged(prefuse.visual.VisualItem,
	 *      javafx.scene.input.MouseEvent)
	 */
	@Override
	public void itemDragged(final VisualItem item, final MouseEvent e) {
		if (e.getButton() != MouseButton.PRIMARY) {
			return;
		}
		this.dragged = true;
		final Display d = (Display) e.getSource();
		this.temp = d.getAbsoluteCoordinate(new Point2D(e.getX(), e.getY()));
		final double dx = this.temp.getX() - this.down.getX();
		final double dy = this.temp.getY() - this.down.getY();
		final double x = item.getX();
		final double y = item.getY();

		item.setStartX(x);
		item.setStartY(y);
		item.setX(x + dx);
		item.setY(y + dy);
		item.setEndX(x + dx);
		item.setEndY(y + dy);

		if (this.repaint) {
			item.getVisualization().repaint();
		}

		this.down = this.temp;
		if (this.action != null) {
			d.getVisualization().run(this.action);
		}
	}

	/**
	 * @see prefuse.data.event.TableListener#tableChanged(prefuse.data.Table,
	 *      int, int, int, int)
	 */
	@Override
	public void tableChanged(final Table t, final int start, final int end, final int col, final int type) {
		if ((this.activeItem == null) || (type != EventConstants.UPDATE)
				|| (col != t.getColumnNumber(VisualItem.FIXED))) {
			return;
		}
		final int row = this.activeItem.getRow();
		if ((row >= start) && (row <= end)) {
			this.resetItem = false;
		}
	}

} // end of class DragControl
