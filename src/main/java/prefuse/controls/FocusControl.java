package prefuse.controls;

import java.util.logging.Logger;

import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.data.expression.Predicate;
import prefuse.data.tuple.TupleSet;
import prefuse.util.StringLib;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualItem;

/**
 * <p>
 * Updates the contents of a TupleSet of focus items in response to mouse
 * actions. For example, clicking a node or double-clicking a node could update
 * its focus status. This Control supports monitoring a specified number of
 * clicks to executing a focus change. By default a click pattern will cause a
 * VisualItem to become the sole member of the focus group. Hold down the
 * control key while clicking to add an item to a group without removing the
 * current members.
 * </p>
 *
 * <p>
 * Updating a focus group does not necessarily cause the display to change. For
 * this functionality, either register an action with this control, or register
 * a TupleSetListener with the focus group.
 * </p>
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class FocusControl extends ControlAdapter {

	private String group = Visualization.FOCUS_ITEMS;
	protected String activity;
	protected VisualItem curFocus;
	protected int ccount;
	protected MouseButton button = Control.LEFT_MOUSE_BUTTON;
	protected Predicate filter = null;

	/**
	 * Creates a new FocusControl that changes the focus to another item when
	 * that item is clicked once.
	 */
	public FocusControl() {
		this(1);
	}

	/**
	 * Creates a new FocusControl that changes the focus to another item when
	 * that item is clicked once.
	 * 
	 * @param focusGroup
	 *            the name of the focus group to use
	 */
	public FocusControl(final String focusGroup) {
		this(1);
		this.group = focusGroup;
	}

	/**
	 * Creates a new FocusControl that changes the focus when an item is clicked
	 * the specified number of times. A click value of zero indicates that the
	 * focus should be changed in response to mouse-over events.
	 * 
	 * @param clicks
	 *            the number of clicks needed to switch the focus.
	 */
	public FocusControl(final int clicks) {
		this.ccount = clicks;
	}

	/**
	 * Creates a new FocusControl that changes the focus when an item is clicked
	 * the specified number of times. A click value of zero indicates that the
	 * focus should be changed in response to mouse-over events.
	 * 
	 * @param focusGroup
	 *            the name of the focus group to use
	 * @param clicks
	 *            the number of clicks needed to switch the focus.
	 */
	public FocusControl(final String focusGroup, final int clicks) {
		this.ccount = clicks;
		this.group = focusGroup;
	}

	/**
	 * Creates a new FocusControl that changes the focus when an item is clicked
	 * the specified number of times. A click value of zero indicates that the
	 * focus should be changed in response to mouse-over events.
	 * 
	 * @param clicks
	 *            the number of clicks needed to switch the focus.
	 * @param act
	 *            an action run to upon focus change
	 */
	public FocusControl(final int clicks, final String act) {
		this.ccount = clicks;
		this.activity = act;
	}

	/**
	 * Creates a new FocusControl that changes the focus when an item is clicked
	 * the specified number of times. A click value of zero indicates that the
	 * focus should be changed in response to mouse-over events.
	 * 
	 * @param focusGroup
	 *            the name of the focus group to use
	 * @param clicks
	 *            the number of clicks needed to switch the focus.
	 * @param act
	 *            an action run to upon focus change
	 */
	public FocusControl(final String focusGroup, final int clicks, final String act) {
		this.ccount = clicks;
		this.activity = act;
		this.group = focusGroup;
	}

	// ------------------------------------------------------------------------

	/**
	 * Set a filter for processing items by this focus control. Only items for
	 * which the predicate returns true (or doesn't throw an exception) will be
	 * considered by this control. A null value indicates that no filtering
	 * should be applied. That is, all items will be considered.
	 * 
	 * @param p
	 *            the filtering predicate to apply
	 */
	public void setFilter(final Predicate p) {
		this.filter = p;
	}

	/**
	 * Get the filter for processing items by this focus control. Only items for
	 * which the predicate returns true (or doesn't throw an exception) are
	 * considered by this control. A null value indicates that no filtering is
	 * applied.
	 * 
	 * @return the filtering predicate
	 */
	public Predicate getFilter() {
		return this.filter;
	}

	/**
	 * Perform a filtering check on the input item.
	 * 
	 * @param item
	 *            the item to check against the filter
	 * @return true if the item should be considered, false otherwise
	 */
	protected boolean filterCheck(final VisualItem item) {
		if (this.filter == null) {
			return true;
		}

		try {
			return this.filter.getBoolean(item);
		} catch (final Exception e) {
			Logger.getLogger(this.getClass().getName()).warning(e.getMessage() + "\n" + StringLib.getStackTrace(e));
			return false;
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * @see prefuse.controls.Control#itemEntered(prefuse.visual.VisualItem,
	 *      javafx.scene.input.MouseEvent)
	 */
	@Override
	public void itemEntered(final VisualItem item, final MouseEvent e) {
		if (!this.filterCheck(item)) {
			return;
		}
		final Display d = (Display) e.getSource();
		d.setCursor(Cursor.HAND);
		if (this.ccount == 0) {
			final Visualization vis = item.getVisualization();
			final TupleSet ts = vis.getFocusGroup(this.group);
			ts.setTuple(item);
			this.curFocus = item;
			this.runActivity(vis);
		}
	}

	/**
	 * @see prefuse.controls.Control#itemExited(prefuse.visual.VisualItem,
	 *      javafx.scene.input.MouseEvent)
	 */
	@Override
	public void itemExited(final VisualItem item, final MouseEvent e) {
		if (!this.filterCheck(item)) {
			return;
		}
		final Display d = (Display) e.getSource();
		d.setCursor(Cursor.DEFAULT);
		if (this.ccount == 0) {
			this.curFocus = null;
			final Visualization vis = item.getVisualization();
			final TupleSet ts = vis.getFocusGroup(this.group);
			ts.removeTuple(item);
			this.runActivity(vis);
		}
	}

	/**
	 * @see prefuse.controls.Control#itemClicked(prefuse.visual.VisualItem,
	 *      javafx.scene.input.MouseEvent)
	 */
	@Override
	public void itemClicked(final VisualItem item, final MouseEvent e) {
		if (!this.filterCheck(item)) {
			return;
		}
		if (UILib.isButtonPressed(e, this.button) && (e.getClickCount() == this.ccount)) {
			if (item != this.curFocus) {
				final Visualization vis = item.getVisualization();
				final TupleSet ts = vis.getFocusGroup(this.group);

				final boolean ctrl = e.isControlDown();
				if (!ctrl) {
					this.curFocus = item;
					ts.setTuple(item);
				} else if (ts.containsTuple(item)) {
					ts.removeTuple(item);
				} else {
					ts.addTuple(item);
				}
				this.runActivity(vis);

			} else if (e.isControlDown()) {
				final Visualization vis = item.getVisualization();
				final TupleSet ts = vis.getFocusGroup(this.group);
				ts.removeTuple(item);
				this.curFocus = null;
				this.runActivity(vis);
			}
		}
	}

	private void runActivity(final Visualization vis) {
		if (this.activity != null) {
			vis.run(this.activity);
		}
	}

} // end of class FocusControl
