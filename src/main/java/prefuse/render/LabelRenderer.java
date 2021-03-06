package prefuse.render;

import java.awt.Dimension;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.transform.Affine;
import prefuse.Constants;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.GraphicsLib;
import prefuse.util.StringLib;
import prefuse.visual.VisualItem;

/**
 * Renderer that draws a label, which consists of a text string, an image, or
 * both.
 *
 * <p>
 * When created using the default constructor, the renderer attempts to use text
 * from the "label" field. To use a different field, use the appropriate
 * constructor or use the {@link #setTextField(String)} method. To perform
 * custom String selection, subclass this Renderer and override the
 * {@link #getText(VisualItem)} method. When the text field is <code>null</code>
 * , no text label will be shown. Labels can span multiple lines of text,
 * determined by the presence of newline characters ('\n') within the text
 * string.
 * </p>
 *
 * <p>
 * By default, no image is shown. To show an image, the image field needs to be
 * set, either using the appropriate constructor or the
 * {@link #setImageField(String)} method. The value of the image field should be
 * a text string indicating the location of the image file to use. The string
 * should be either a URL, a file located on the current classpath, or a file on
 * the local filesystem. If found, the image will be managed internally by an
 * {@link ImageFactory} instance, which maintains a cache of loaded images.
 * </p>
 *
 * <p>
 * The position of the image relative to text can be set using the
 * {@link #setImagePosition(int)} method. Images can be placed to the left,
 * right, above, or below the text. The horizontal and vertical alignments of
 * either the text or the image can be set explicitly using the appropriate
 * methods of this class (e.g., {@link #setHorizontalTextAlignment(int)}). By
 * default, both the text and images are centered along both the horizontal and
 * vertical directions.
 * </p>
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class LabelRenderer extends AbstractShapeRenderer {

	protected ImageFactory m_images = null;
	protected String m_delim = "\n";

	protected String m_labelName = "label";
	protected String m_imageName = null;

	protected int m_xAlign = Constants.CENTER;
	protected int m_yAlign = Constants.CENTER;
	protected int m_hTextAlign = Constants.CENTER;
	protected int m_vTextAlign = Constants.CENTER;
	protected int m_hImageAlign = Constants.CENTER;
	protected int m_vImageAlign = Constants.CENTER;
	protected int m_imagePos = Constants.LEFT;

	protected int m_horizBorder = 2;
	protected int m_vertBorder = 0;
	protected int m_imageMargin = 2;
	protected int m_arcWidth = 0;
	protected int m_arcHeight = 0;

	protected int m_maxTextWidth = -1;

	/** Transform used to scale and position images */
	Affine m_transform = new Affine();

	/** The holder for the currently computed bounding box */
	protected Rectangle m_bbox = new Rectangle();
	protected Point2D m_pt = Point2D.ZERO; // temp point
	protected Font m_font; // temp font holder
	protected String m_text; // label text
	protected Dimension m_textDim = new Dimension(); // text width / height

	/**
	 * Create a new LabelRenderer. By default the field "label" is used as the
	 * field name for looking up text, and no image is used.
	 */
	public LabelRenderer() {
	}

	/**
	 * Create a new LabelRenderer. Draws a text label using the given text data
	 * field and does not draw an image.
	 *
	 * @param textField
	 *            the data field for the text label.
	 */
	public LabelRenderer(final String textField) {
		this.setTextField(textField);
	}

	/**
	 * Create a new LabelRenderer. Draws a text label using the given text data
	 * field, and draws the image at the location reported by the given image
	 * data field.
	 *
	 * @param textField
	 *            the data field for the text label
	 * @param imageField
	 *            the data field for the image location. This value in the data
	 *            field should be a URL, a file within the current classpath, a
	 *            file on the filesystem, or null for no image. If the
	 *            <code>imageField</code> parameter is null, no images at all
	 *            will be drawn.
	 */
	public LabelRenderer(final String textField, final String imageField) {
		this.setTextField(textField);
		this.setImageField(imageField);
	}

	// ------------------------------------------------------------------------

	/**
	 * Rounds the corners of the bounding rectangle in which the text string is
	 * rendered. This will only be seen if either the stroke or fill color is
	 * non-transparent.
	 *
	 * @param arcWidth
	 *            the width of the curved corner
	 * @param arcHeight
	 *            the height of the curved corner
	 */
	public void setRoundedCorner(final int arcWidth, final int arcHeight) {
		this.m_arcWidth = arcWidth;
		this.m_arcHeight = arcHeight;
		this.m_bbox.setArcWidth(arcWidth);
		this.m_bbox.setArcHeight(arcHeight);
	}

	/**
	 * Get the field name to use for text labels.
	 *
	 * @return the data field for text labels, or null for no text
	 */
	public String getTextField() {
		return this.m_labelName;
	}

	/**
	 * Set the field name to use for text labels.
	 *
	 * @param textField
	 *            the data field for text labels, or null for no text
	 */
	public void setTextField(final String textField) {
		this.m_labelName = textField;
	}

	/**
	 * Sets the maximum width that should be allowed of the text label. A value
	 * of -1 specifies no limit (this is the default).
	 *
	 * @param maxWidth
	 *            the maximum width of the text or -1 for no limit
	 */
	public void setMaxTextWidth(final int maxWidth) {
		this.m_maxTextWidth = maxWidth;
	}

	/**
	 * Returns the text to draw. Subclasses can override this class to perform
	 * custom text selection.
	 *
	 * @param item
	 *            the item to represent as a <code>String</code>
	 * @return a <code>String</code> to draw
	 */
	protected String getText(final VisualItem item) {
		final String s = null;
		if (item.canGetString(this.m_labelName)) {
			return item.getString(this.m_labelName);
		}
		return s;
	}

	// ------------------------------------------------------------------------
	// Image Handling

	/**
	 * Get the data field for image locations. The value stored in the data
	 * field should be a URL, a file within the current classpath, a file on the
	 * filesystem, or null for no image.
	 *
	 * @return the data field for image locations, or null for no images
	 */
	public String getImageField() {
		return this.m_imageName;
	}

	/**
	 * Set the data field for image locations. The value stored in the data
	 * field should be a URL, a file within the current classpath, a file on the
	 * filesystem, or null for no image. If the <code>imageField</code>
	 * parameter is null, no images at all will be drawn.
	 *
	 * @param imageField
	 *            the data field for image locations, or null for no images
	 */
	public void setImageField(final String imageField) {
		if (imageField != null) {
			this.m_images = new ImageFactory();
		}
		this.m_imageName = imageField;
	}

	/**
	 * Sets the maximum image dimensions, used to control scaling of loaded
	 * images. This scaling is enforced immediately upon loading of the image.
	 *
	 * @param width
	 *            the maximum width of images (-1 for no limit)
	 * @param height
	 *            the maximum height of images (-1 for no limit)
	 */
	public void setMaxImageDimensions(final int width, final int height) {
		if (this.m_images == null) {
			this.m_images = new ImageFactory();
		}
		this.m_images.setMaxImageDimensions(width, height);
	}

	/**
	 * Returns a location string for the image to draw. Subclasses can override
	 * this class to perform custom image selection beyond looking up the value
	 * from a data field.
	 *
	 * @param item
	 *            the item for which to select an image to draw
	 * @return the location string for the image to use, or null for no image
	 */
	protected String getImageLocation(final VisualItem item) {
		return item.canGetString(this.m_imageName) ? item.getString(this.m_imageName) : null;
	}

	/**
	 * Get the image to include in the label for the given VisualItem.
	 *
	 * @param item
	 *            the item to get an image for
	 * @return the image for the item, or null for no image
	 */
	protected Image getImage(final VisualItem item) {
		final String imageLoc = this.getImageLocation(item);
		return (imageLoc == null ? null : this.m_images.getImage(imageLoc));
	}

	// ------------------------------------------------------------------------
	// Rendering

	private String computeTextDimensions(final VisualItem item, final String text, final double size) {
		// put item font in temp member variable
		this.m_font = item.getFont();
		// scale the font as needed
		if (size != 1) {
			this.m_font = FontLib.getFont(this.m_font.getFamily(), size * this.m_font.getSize());
		}

		StringBuffer str = null;

		// compute the number of lines and the maximum width
		int nlines = 1, w = 0, start = 0, end = text.indexOf(this.m_delim);
		this.m_textDim.width = 0;
		String line;
		for (; end >= 0; ++nlines) {
			w = StringLib.computeStringWidth(line = text.substring(start, end), this.m_font);
			// abbreviate line as needed
			if ((this.m_maxTextWidth > -1) && (w > this.m_maxTextWidth)) {
				if (str == null) {
					str = new StringBuffer(text.substring(0, start));
				}
				str.append(StringLib.abbreviate(line, this.m_font, this.m_maxTextWidth));
				str.append(this.m_delim);
				w = this.m_maxTextWidth;
			} else if (str != null) {
				str.append(line).append(this.m_delim);
			}
			// update maximum width and substring indices
			this.m_textDim.width = Math.max(this.m_textDim.width, w);
			start = end + 1;
			end = text.indexOf(this.m_delim, start);
		}
		w = StringLib.computeStringWidth(line = text.substring(start), this.m_font);
		// abbreviate line as needed
		if ((this.m_maxTextWidth > -1) && (w > this.m_maxTextWidth)) {
			if (str == null) {
				str = new StringBuffer(text.substring(0, start));
			}
			str.append(StringLib.abbreviate(line, this.m_font, this.m_maxTextWidth));
			w = this.m_maxTextWidth;
		} else if (str != null) {
			str.append(line);
		}
		// update maximum width
		this.m_textDim.width = Math.max(this.m_textDim.width, w);

		// compute the text height
		this.m_textDim.height = (int) (this.m_font.getSize() * nlines);

		return str == null ? text : str.toString();
	}

	/**
	 * @see prefuse.render.AbstractShapeRenderer#getRawShape(prefuse.visual.VisualItem)
	 */
	@Override
	protected Shape getRawShape(final VisualItem item) {
		this.m_text = this.getText(item);
		final Image img = this.getImage(item);
		final double size = item.getSize();

		// get image dimensions
		double iw = 0, ih = 0;
		if (img != null) {
			ih = img.getHeight();
			iw = img.getWidth();
		}

		// get text dimensions
		int tw = 0, th = 0;
		if (this.m_text != null) {
			this.m_text = this.computeTextDimensions(item, this.m_text, size);
			th = this.m_textDim.height;
			tw = this.m_textDim.width;
		}

		// get bounding box dimensions
		double w = 0, h = 0;
		switch (this.m_imagePos) {
		case Constants.LEFT:
		case Constants.RIGHT:
			w = tw + (size * (iw + (2 * this.m_horizBorder) + ((tw > 0) && (iw > 0) ? this.m_imageMargin : 0)));
			h = Math.max(th, size * ih) + (size * 2 * this.m_vertBorder);
			break;
		case Constants.TOP:
		case Constants.BOTTOM:
			w = Math.max(tw, size * iw) + (size * 2 * this.m_horizBorder);
			h = th + (size * (ih + (2 * this.m_vertBorder) + ((th > 0) && (ih > 0) ? this.m_imageMargin : 0)));
			break;
		default:
			throw new IllegalStateException("Unrecognized image alignment setting.");
		}

		// get the top-left point, using the current alignment settings
		this.m_pt = getAlignedPoint(item, w, h, this.m_xAlign, this.m_yAlign);

		this.m_bbox.setX(this.m_pt.getX());
		this.m_bbox.setY(this.m_pt.getY());
		this.m_bbox.setWidth(w);
		this.m_bbox.setHeight(h);
		/*
		 * even if there is no rounded information we compute it. What it the
		 * fastest ? check if arcWith and arcHeight are equals to zero or do two
		 * multiplications with zero ?
		 */
		this.m_bbox.setArcWidth(size * this.m_arcWidth);
		this.m_bbox.setArcHeight(size * this.m_arcHeight);
		return this.m_bbox;
	}

	/**
	 * Helper method, which calculates the top-left co-ordinate of an item given
	 * the item's alignment.
	 */
	protected static Point2D getAlignedPoint(final VisualItem item, final double w, final double h, final int xAlign,
			final int yAlign) {
		double x = item.getX(), y = item.getY();
		if (Double.isNaN(x) || Double.isInfinite(x)) {
			x = 0; // safety check
		}
		if (Double.isNaN(y) || Double.isInfinite(y)) {
			y = 0; // safety check
		}

		if (xAlign == Constants.CENTER) {
			x = x - (w / 2);
		} else if (xAlign == Constants.RIGHT) {
			x = x - w;
		}
		if (yAlign == Constants.CENTER) {
			y = y - (h / 2);
		} else if (yAlign == Constants.BOTTOM) {
			y = y - h;
		}
		return new Point2D(x, y);
	}

	/**
	 * @see prefuse.render.Renderer#render(javafx.scene.canvas.GraphicsContext,
	 *      prefuse.visual.VisualItem)
	 */
	@Override
	public void render(final GraphicsContext g, final VisualItem item) {
		final Rectangle shape = (Rectangle) this.getShape(item);
		if (shape == null) {
			return;
		}

		// fill the shape, if requested
		final int type = this.getRenderType(item);
		if ((type == RENDER_TYPE_FILL) || (type == RENDER_TYPE_DRAW_AND_FILL)) {
			GraphicsLib.paint(g, item, shape, this.getStroke(item), RENDER_TYPE_FILL);
		}

		// now render the image and text
		final String text = this.m_text;
		final Image img = this.getImage(item);

		if ((text == null) && (img == null)) {
			return;
		}

		final double size = item.getSize();
		final boolean useInt = 1.5 > Math.max(g.getTransform().getMxx(), g.getTransform().getMyy());
		double x = shape.getX() + (size * this.m_horizBorder);
		double y = shape.getY() + (size * this.m_vertBorder);

		// render image
		if (img != null) {
			final double w = size * img.getWidth();
			final double h = size * img.getHeight();
			double ix = x, iy = y;

			// determine one co-ordinate based on the image position
			switch (this.m_imagePos) {
			case Constants.LEFT:
				x += w + (size * this.m_imageMargin);
				break;
			case Constants.RIGHT:
				ix = (shape.getX() + shape.getWidth()) - (size * this.m_horizBorder) - w;
				break;
			case Constants.TOP:
				y += h + (size * this.m_imageMargin);
				break;
			case Constants.BOTTOM:
				iy = (shape.getY() + shape.getHeight()) - (size * this.m_vertBorder) - h;
				break;
			default:
				throw new IllegalStateException("Unrecognized image alignment setting.");
			}

			// determine the other coordinate based on image alignment
			switch (this.m_imagePos) {
			case Constants.LEFT:
			case Constants.RIGHT:
				// need to set image y-coordinate
				switch (this.m_vImageAlign) {
				case Constants.TOP:
					break;
				case Constants.BOTTOM:
					iy = (shape.getY() + shape.getHeight()) - (size * this.m_vertBorder) - h;
					break;
				case Constants.CENTER:
					iy = (shape.getY() + (shape.getHeight() / 2)) - (h / 2);
					break;
				}
				break;
			case Constants.TOP:
			case Constants.BOTTOM:
				// need to set image x-coordinate
				switch (this.m_hImageAlign) {
				case Constants.LEFT:
					break;
				case Constants.RIGHT:
					ix = (shape.getX() + shape.getWidth()) - (size * this.m_horizBorder) - w;
					break;
				case Constants.CENTER:
					ix = (shape.getX() + (shape.getWidth() / 2)) - (w / 2);
					break;
				}
				break;
			}

			if (useInt && (size == 1.0)) {
				// if possible, use integer precision
				// results in faster, flicker-free image rendering
				g.drawImage(img, ix, iy);
			} else {
				this.m_transform.setToTransform(size, 0, ix, 0, size, iy);
				g.setTransform(this.m_transform);
				g.drawImage(img, ix, iy);
			}
		}

		// render text
		final int textColor = item.getTextColor();
		if ((text != null) && (ColorLib.alpha(textColor) > 0)) {
			g.setFill(ColorLib.getColor(textColor));
			g.setFont(this.m_font);

			// compute available width
			double tw;
			switch (this.m_imagePos) {
			case Constants.TOP:
			case Constants.BOTTOM:
				tw = shape.getWidth() - (2 * size * this.m_horizBorder);
				break;
			default:
				tw = this.m_textDim.width;
			}

			// compute available height
			double th;
			switch (this.m_imagePos) {
			case Constants.LEFT:
			case Constants.RIGHT:
				th = shape.getHeight() - (2 * size * this.m_vertBorder);
				break;
			default:
				th = this.m_textDim.height;
			}

			// compute starting y-coordinate
			y += this.m_font.getSize();
			switch (this.m_vTextAlign) {
			case Constants.TOP:
				break;
			case Constants.BOTTOM:
				y += th - this.m_textDim.height;
				break;
			case Constants.CENTER:
				y += (th - this.m_textDim.height) / 2;
			}

			// render each line of text
			final int lh = (int) this.m_font.getSize(); // the line height
			int start = 0, end = text.indexOf(this.m_delim);
			for (; end >= 0; y += lh) {
				this.drawString(g, this.m_font, text.substring(start, end), x, y, tw);
				start = end + 1;
				end = text.indexOf(this.m_delim, start);
			}
			this.drawString(g, this.m_font, text.substring(start), x, y, tw);
		}

		// draw border
		if ((type == RENDER_TYPE_DRAW) || (type == RENDER_TYPE_DRAW_AND_FILL)) {
			GraphicsLib.paint(g, item, shape, this.getStroke(item), RENDER_TYPE_DRAW);
		}
	}

	private final void drawString(final GraphicsContext g, final Font font, final String text, final double x,
			final double y, final double w) {
		// compute the x-coordinate
		double tx;
		switch (this.m_hTextAlign) {
		case Constants.LEFT:
			tx = x;
			break;
		case Constants.RIGHT:
			tx = (x + w) - StringLib.computeStringWidth(text, font);
			break;
		case Constants.CENTER:
			tx = x + ((w - StringLib.computeStringWidth(text, font)) / 2);
			break;
		default:
			throw new IllegalStateException("Unrecognized text alignment setting.");
		}
		g.setFont(font);
		g.fillText(text, tx, y);
	}

	/**
	 * Returns the image factory used by this renderer.
	 *
	 * @return the image factory
	 */
	public ImageFactory getImageFactory() {
		if (this.m_images == null) {
			this.m_images = new ImageFactory();
		}
		return this.m_images;
	}

	/**
	 * Sets the image factory used by this renderer.
	 *
	 * @param ifact
	 *            the image factory
	 */
	public void setImageFactory(final ImageFactory ifact) {
		this.m_images = ifact;
	}

	// ------------------------------------------------------------------------

	/**
	 * Get the horizontal text alignment within the layout. One of
	 * {@link prefuse.Constants#LEFT}, {@link prefuse.Constants#RIGHT}, or
	 * {@link prefuse.Constants#CENTER}. The default is centered text.
	 *
	 * @return the horizontal text alignment
	 */
	public int getHorizontalTextAlignment() {
		return this.m_hTextAlign;
	}

	/**
	 * Set the horizontal text alignment within the layout. One of
	 * {@link prefuse.Constants#LEFT}, {@link prefuse.Constants#RIGHT}, or
	 * {@link prefuse.Constants#CENTER}. The default is centered text.
	 *
	 * @param halign
	 *            the desired horizontal text alignment
	 */
	public void setHorizontalTextAlignment(final int halign) {
		if ((halign != Constants.LEFT) && (halign != Constants.RIGHT) && (halign != Constants.CENTER)) {
			throw new IllegalArgumentException("Illegal horizontal text alignment value.");
		}
		this.m_hTextAlign = halign;
	}

	/**
	 * Get the vertical text alignment within the layout. One of
	 * {@link prefuse.Constants#TOP}, {@link prefuse.Constants#BOTTOM}, or
	 * {@link prefuse.Constants#CENTER}. The default is centered text.
	 *
	 * @return the vertical text alignment
	 */
	public int getVerticalTextAlignment() {
		return this.m_vTextAlign;
	}

	/**
	 * Set the vertical text alignment within the layout. One of
	 * {@link prefuse.Constants#TOP}, {@link prefuse.Constants#BOTTOM}, or
	 * {@link prefuse.Constants#CENTER}. The default is centered text.
	 *
	 * @param valign
	 *            the desired vertical text alignment
	 */
	public void setVerticalTextAlignment(final int valign) {
		if ((valign != Constants.TOP) && (valign != Constants.BOTTOM) && (valign != Constants.CENTER)) {
			throw new IllegalArgumentException("Illegal vertical text alignment value.");
		}
		this.m_vTextAlign = valign;
	}

	/**
	 * Get the horizontal image alignment within the layout. One of
	 * {@link prefuse.Constants#LEFT}, {@link prefuse.Constants#RIGHT}, or
	 * {@link prefuse.Constants#CENTER}. The default is a centered image.
	 *
	 * @return the horizontal image alignment
	 */
	public int getHorizontalImageAlignment() {
		return this.m_hImageAlign;
	}

	/**
	 * Set the horizontal image alignment within the layout. One of
	 * {@link prefuse.Constants#LEFT}, {@link prefuse.Constants#RIGHT}, or
	 * {@link prefuse.Constants#CENTER}. The default is a centered image.
	 *
	 * @param halign
	 *            the desired horizontal image alignment
	 */
	public void setHorizontalImageAlignment(final int halign) {
		if ((halign != Constants.LEFT) && (halign != Constants.RIGHT) && (halign != Constants.CENTER)) {
			throw new IllegalArgumentException("Illegal horizontal text alignment value.");
		}
		this.m_hImageAlign = halign;
	}

	/**
	 * Get the vertical image alignment within the layout. One of
	 * {@link prefuse.Constants#TOP}, {@link prefuse.Constants#BOTTOM}, or
	 * {@link prefuse.Constants#CENTER}. The default is a centered image.
	 *
	 * @return the vertical image alignment
	 */
	public int getVerticalImageAlignment() {
		return this.m_vImageAlign;
	}

	/**
	 * Set the vertical image alignment within the layout. One of
	 * {@link prefuse.Constants#TOP}, {@link prefuse.Constants#BOTTOM}, or
	 * {@link prefuse.Constants#CENTER}. The default is a centered image.
	 *
	 * @param valign
	 *            the desired vertical image alignment
	 */
	public void setVerticalImageAlignment(final int valign) {
		if ((valign != Constants.TOP) && (valign != Constants.BOTTOM) && (valign != Constants.CENTER)) {
			throw new IllegalArgumentException("Illegal vertical text alignment value.");
		}
		this.m_vImageAlign = valign;
	}

	/**
	 * Get the image position, determining where the image is placed with
	 * respect to the text. One of {@link Constants#LEFT},
	 * {@link Constants#RIGHT}, {@link Constants#TOP}, or
	 * {@link Constants#BOTTOM}. The default is left.
	 *
	 * @return the image position
	 */
	public int getImagePosition() {
		return this.m_imagePos;
	}

	/**
	 * Set the image position, determining where the image is placed with
	 * respect to the text. One of {@link Constants#LEFT},
	 * {@link Constants#RIGHT}, {@link Constants#TOP}, or
	 * {@link Constants#BOTTOM}. The default is left.
	 *
	 * @param pos
	 *            the desired image position
	 */
	public void setImagePosition(final int pos) {
		if ((pos != Constants.TOP) && (pos != Constants.BOTTOM) && (pos != Constants.LEFT) && (pos != Constants.RIGHT)
				&& (pos != Constants.CENTER)) {
			throw new IllegalArgumentException("Illegal image position value.");
		}
		this.m_imagePos = pos;
	}

	// ------------------------------------------------------------------------

	/**
	 * Get the horizontal alignment of this node with respect to its x, y
	 * coordinates.
	 *
	 * @return the horizontal alignment, one of {@link prefuse.Constants#LEFT},
	 *         {@link prefuse.Constants#RIGHT}, or
	 *         {@link prefuse.Constants#CENTER}.
	 */
	public int getHorizontalAlignment() {
		return this.m_xAlign;
	}

	/**
	 * Get the vertical alignment of this node with respect to its x, y
	 * coordinates.
	 *
	 * @return the vertical alignment, one of {@link prefuse.Constants#TOP},
	 *         {@link prefuse.Constants#BOTTOM}, or
	 *         {@link prefuse.Constants#CENTER}.
	 */
	public int getVerticalAlignment() {
		return this.m_yAlign;
	}

	/**
	 * Set the horizontal alignment of this node with respect to its x, y
	 * coordinates.
	 *
	 * @param align
	 *            the horizontal alignment, one of
	 *            {@link prefuse.Constants#LEFT},
	 *            {@link prefuse.Constants#RIGHT}, or
	 *            {@link prefuse.Constants#CENTER}.
	 */
	public void setHorizontalAlignment(final int align) {
		this.m_xAlign = align;
	}

	/**
	 * Set the vertical alignment of this node with respect to its x, y
	 * coordinates.
	 *
	 * @param align
	 *            the vertical alignment, one of {@link prefuse.Constants#TOP},
	 *            {@link prefuse.Constants#BOTTOM}, or
	 *            {@link prefuse.Constants#CENTER}.
	 */
	public void setVerticalAlignment(final int align) {
		this.m_yAlign = align;
	}

	/**
	 * Returns the amount of padding in pixels between the content and the
	 * border of this item along the horizontal dimension.
	 *
	 * @return the horizontal padding
	 */
	public int getHorizontalPadding() {
		return this.m_horizBorder;
	}

	/**
	 * Sets the amount of padding in pixels between the content and the border
	 * of this item along the horizontal dimension.
	 *
	 * @param xpad
	 *            the horizontal padding to set
	 */
	public void setHorizontalPadding(final int xpad) {
		this.m_horizBorder = xpad;
	}

	/**
	 * Returns the amount of padding in pixels between the content and the
	 * border of this item along the vertical dimension.
	 *
	 * @return the vertical padding
	 */
	public int getVerticalPadding() {
		return this.m_vertBorder;
	}

	/**
	 * Sets the amount of padding in pixels between the content and the border
	 * of this item along the vertical dimension.
	 *
	 * @param ypad
	 *            the vertical padding
	 */
	public void setVerticalPadding(final int ypad) {
		this.m_vertBorder = ypad;
	}

	/**
	 * Get the padding, in pixels, between an image and text.
	 *
	 * @return the padding between an image and text
	 */
	public int getImageTextPadding() {
		return this.m_imageMargin;
	}

	/**
	 * Set the padding, in pixels, between an image and text.
	 *
	 * @param pad
	 *            the padding to use between an image and text
	 */
	public void setImageTextPadding(final int pad) {
		this.m_imageMargin = pad;
	}

} // end of class LabelRenderer
