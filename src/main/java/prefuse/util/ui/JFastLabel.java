package prefuse.util.ui;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.SwingConstants;

/**
 * Swing component that acts much like a JLabel, but does not revalidate its
 * bounds when updated, making it much faster but suitable only for use in
 * situations where the initial bounds are sufficient.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 * @deprecated replaced by a simple javafx.scene.control.Label.
 */
@Deprecated
public class JFastLabel extends JComponent {

	private String m_text;
	private int m_valign = SwingConstants.TOP;
	private int m_halign = SwingConstants.LEFT;
	private int m_fheight = -1;
	private boolean m_quality = false;

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
		this.m_text = text;
		this.setFont(this.getFont());
	}

	/**
	 * Get the label text.
	 *
	 * @return the label text
	 */
	public String getText() {
		return this.m_text;
	}

	/**
	 * Set the label text
	 *
	 * @param text
	 *            the label text to set
	 */
	public void setText(final String text) {
		this.m_text = text;
		this.repaint();
	}

	/**
	 * @see java.awt.Component#setFont(java.awt.Font)
	 */
	@Override
	public void setFont(final Font f) {
		super.setFont(f);
		this.m_fheight = -1;
	}

	/**
	 * Set the vertical alignment.
	 *
	 * @param align
	 *            the vertical alignment
	 * @see javax.swing.SwingConstants
	 */
	public void setVerticalAlignment(final int align) {
		this.m_valign = align;
		this.m_fheight = -1;
	}

	/**
	 * Set the horizontal alignment.
	 *
	 * @param align
	 *            the horizontal alignment
	 * @see javax.swing.SwingConstants
	 */
	public void setHorizontalAlignment(final int align) {
		this.m_halign = align;
	}

	/**
	 * Get the quality level of this label. High quality results in anti-aliased
	 * rendering.
	 *
	 * @return true for high quality, false otherwise
	 */
	public boolean getHighQuality() {
		return this.m_quality;
	}

	/**
	 * Set the quality level of this label. High quality results in anti-aliased
	 * rendering.
	 *
	 * @param b
	 *            true for high quality, false otherwise
	 */
	public void setHighQuality(final boolean b) {
		this.m_quality = b;
	}

	/**
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(final Graphics g) {
		final Insets ins = this.getInsets();
		int w = this.getWidth() - ins.left - ins.right;
		int h = this.getHeight() - ins.top - ins.bottom;
		if (this.m_fheight == -1) {
			final FontMetrics fm = g.getFontMetrics(this.getFont());
			if (this.m_valign == SwingConstants.BOTTOM) {
				this.m_fheight = fm.getDescent();
			} else if (this.m_valign == SwingConstants.TOP) {
				this.m_fheight = fm.getAscent();
			}
		}
		g.setColor(this.getBackground());
		g.fillRect(ins.left, ins.top, w, h);

		if (this.m_text == null) {
			return;
		}

		g.setFont(this.getFont());
		g.setColor(this.getForeground());
		if (this.m_valign == SwingConstants.BOTTOM) {
			h = h - this.m_fheight - ins.bottom;
		} else {
			h = ins.top + this.m_fheight;
		}

		switch (this.m_halign) {
		case SwingConstants.RIGHT: {
			final FontMetrics fm = g.getFontMetrics(this.getFont());
			w = w - ins.right - fm.stringWidth(this.m_text);
			break;
		}
		case SwingConstants.CENTER: {
			final FontMetrics fm = g.getFontMetrics(this.getFont());
			w = (ins.left + (w / 2)) - (fm.stringWidth(this.m_text) / 2);
			break;
		}
		default:
			w = ins.left;
		}
		if (this.m_quality) {
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		}
		g.drawString(this.m_text, w, h);
	}

} // end of class JFastLabel
