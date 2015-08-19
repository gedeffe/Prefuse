package prefuse.controls;

import javafx.scene.input.MouseEvent;
import prefuse.Display;
import prefuse.visual.VisualItem;

/**
 * Control that enables a tooltip display for items based on mouse hover.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class ToolTipControl extends ControlAdapter {

	private final String[] label;
	private StringBuffer sbuf;

	/**
	 * Create a new ToolTipControl.
	 * 
	 * @param field
	 *            the field name to use for the tooltip text
	 */
	public ToolTipControl(final String field) {
		this(new String[] { field });
	}

	/**
	 * Create a new ToolTipControl.
	 * 
	 * @param fields
	 *            the field names to use for the tooltip text. The values of
	 *            each field will be concatenated to form the tooltip.
	 */
	public ToolTipControl(final String[] fields) {
		this.label = fields;
		if (fields.length > 1) {
			this.sbuf = new StringBuffer();
		}
	}

	/**
	 * @see prefuse.controls.Control#itemEntered(prefuse.visual.VisualItem,
	 *      javafx.scene.input.MouseEvent)
	 */
	@Override
	public void itemEntered(final VisualItem item, final MouseEvent e) {
		final Display d = (Display) e.getSource();
		if (this.label.length == 1) {
			// optimize the simple case
			if (item.canGetString(this.label[0])) {
				d.getCustomToolTip().setText(item.getString(this.label[0]));
			}
		} else {
			this.sbuf.delete(0, this.sbuf.length());
			for (int i = 0; i < this.label.length; ++i) {
				if (item.canGetString(this.label[i])) {
					if (this.sbuf.length() > 0) {
						this.sbuf.append("; ");
					}
					this.sbuf.append(item.getString(this.label[i]));
				}
			}
			// show tool tip only, if at least one field is available
			if (this.sbuf.length() > 0) {
				d.getCustomToolTip().setText(this.sbuf.toString());
			}
		}
	}

	/**
	 * @see prefuse.controls.Control#itemExited(prefuse.visual.VisualItem,
	 *      javafx.scene.input.MouseEvent)
	 */
	@Override
	public void itemExited(final VisualItem item, final MouseEvent e) {
		final Display d = (Display) e.getSource();
		d.getCustomToolTip().setText(null);
	}

} // end of class ToolTipControl
