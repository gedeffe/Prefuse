package prefuse.util.ui;

import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.TextAlignment;
import prefuse.util.StringLib;

/**
 * JavaFX component that acts much like a Label, but does not revalidate its
 * bounds when updated, making it much faster but suitable only for use in
 * situations where the initial bounds are sufficient.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 * @deprecated replaced by a simple javafx.scene.control.Label.
 */
@Deprecated
public class JFastLabel extends Canvas {

	private String text;
	private VPos valign = VPos.BASELINE;
	private TextAlignment halign = TextAlignment.LEFT;
	private int fheight = -1;
	private boolean quality = false;
	private Font font = Font.getDefault();
	private Insets insets = Insets.EMPTY;
	private Paint foregroundColor = Color.BLACK;
	private Paint backgroundColor = Color.WHITE;

	/**
	 * Create a new JFastLabel with no text.
	 */
	public JFastLabel() {
		this(null);
	}

	/**
	 * Create a new JFastLabel with the given text.
	 *
	 * @param text
	 *            the label text.
	 */
	public JFastLabel(final String text) {
		this.text = text;
	}

	/**
	 * Get the label text.
	 *
	 * @return the label text
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * Set the label text
	 *
	 * @param text
	 *            the label text to set
	 */
	public void setText(final String text) {
		this.text = text;
		this.repaint();
	}

	/**
	 * @see java.awt.Component#setFont(javafx.scene.text.Font)
	 */
	public void setFont(final Font f) {
		this.font = f;
		this.fheight = -1;
	}

	/**
	 * Set the vertical alignment.
	 *
	 * @param align
	 *            the vertical alignment
	 * @see javafx.geometry.VPos
	 */
	public void setVerticalAlignment(final VPos align) {
		this.valign = align;
		this.fheight = -1;
	}

	/**
	 * Set the horizontal alignment.
	 *
	 * @param align
	 *            the horizontal alignment
	 * @see javafx.scene.text.TextAlignment
	 */
	public void setHorizontalAlignment(final TextAlignment align) {
		this.halign = align;
	}

	/**
	 * Get the quality level of this label. High quality results in anti-aliased
	 * rendering.
	 *
	 * @return true for high quality, false otherwise
	 */
	public boolean getHighQuality() {
		return this.quality;
	}

	/**
	 * Set the quality level of this label. High quality results in anti-aliased
	 * rendering.
	 *
	 * @param b
	 *            true for high quality, false otherwise
	 */
	public void setHighQuality(final boolean b) {
		this.quality = b;
	}

	/**
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	public void paintComponent(final GraphicsContext g) {
		final Insets ins = this.getInsets();
		double w = this.getWidth() - ins.getLeft() - ins.getRight();
		double h = this.getHeight() - ins.getTop() - ins.getBottom();
		if (this.fheight == -1) {
			if (this.valign == VPos.BOTTOM) {
				this.fheight = (int) this.font.getSize();
			} else if (this.valign == VPos.TOP) {
				this.fheight = 0;
			}
		}
		g.setFill(this.getBackgroundColor());
		g.fillRect(ins.getLeft(), ins.getTop(), w, h);

		if (this.text == null) {
			return;
		}

		g.setFont(this.font);
		g.setFill(this.getForegroundColor());
		if (this.valign == VPos.BOTTOM) {
			h = h - this.fheight - ins.getBottom();
		} else {
			h = ins.getTop() + this.fheight;
		}

		switch (this.halign) {
		case RIGHT: {
			w = w - ins.getRight() - StringLib.computeStringWidth(this.text, this.font);
			break;
		}
		case CENTER: {
			w = (ins.getLeft() + (w / 2)) - (StringLib.computeStringWidth(this.text, this.font) / 2);
			break;
		}
		default:
			w = ins.getLeft();
		}
		if (this.quality) {
			// as anti-aliasing is always on, we try to increase its quality
			g.setFontSmoothingType(FontSmoothingType.LCD);
		}
		g.fillText(this.text, w, h);
	}

	private Paint getForegroundColor() {
		return this.foregroundColor;
	}

	private Paint getBackgroundColor() {
		return this.backgroundColor;
	}

	public Insets getInsets() {
		return this.insets;
	}

	/**
	 * Temporary method to be able to call smoothly the paintComponent with
	 * internal GraphicsContext
	 *
	 * @deprecated should be replace by nothing as this component should handle
	 *             itself the need of repainting what is on screen.
	 */
	@Deprecated
	public void repaint() {
		this.paintComponent(this.getGraphicsContext2D());
	}

	public void setInsets(final Insets insets) {
		this.insets = insets;
	}

	public void setForegroundColor(final Paint foregroundColor) {
		this.foregroundColor = foregroundColor;
	}

	public void setBackgroundColor(final Paint backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

} // end of class JFastLabel
