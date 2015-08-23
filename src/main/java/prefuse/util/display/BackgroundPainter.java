/**
 * Copyright (c) 2004-2006 Regents of the University of California.
 * See "license-prefuse.txt" for licensing terms.
 */
package prefuse.util.display;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.transform.Affine;
import prefuse.Display;
import prefuse.util.io.IOLib;

/**
 * Paints a background image in a display. The image can either pan and zoom
 * along with the display or stay stationary. Additionally, the image can be
 * optionally tiled across the Display space. This class is used by the
 * {@link prefuse.Display} class in response to the
 * {@link prefuse.Display#setBackgroundImage(Image, boolean, boolean)} and
 * {@link prefuse.Display#setBackgroundImage(String, boolean, boolean)} methods.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class BackgroundPainter implements PaintListener {

	private static final double THRESH = 0.01;

	private final Image image;
	private final boolean fixed;
	private final boolean tiled;

	private Affine identity;
	private Clip clip;

	/**
	 * Create a new BackgroundPainter.
	 *
	 * @param imageLocation
	 *            a location String of where to retrieve the image file from.
	 *            Uses {@link prefuse.util.io.IOLib#urlFromString(String)} to
	 *            resolve the String.
	 * @param fixed
	 *            true if the background image should stay in a fixed position,
	 *            invariant to panning, zooming, or rotation; false if the image
	 *            should be subject to view transforms
	 * @param tile
	 *            true to tile the image across the visible background, false to
	 *            only include the image once
	 */
	public BackgroundPainter(final String imageLocation, final boolean fixed, final boolean tile) {
		this(new Image(IOLib.urlFromString(imageLocation).toString()), fixed, tile);
	}

	/**
	 * Create a new BackgroundPainter.
	 *
	 * @param image
	 *            the background Image
	 * @param fixed
	 *            true if the background image should stay in a fixed position,
	 *            invariant to panning, zooming, or rotation; false if the image
	 *            should be subject to view transforms
	 * @param tile
	 *            true to tile the image across the visible background, false to
	 *            only include the image once
	 */
	public BackgroundPainter(final Image image, final boolean fixed, final boolean tile) {
		this.image = image;

		// make sure the image is completely loaded
		if (this.image.isBackgroundLoading()) {
			while (this.image.getProgress() != 1) {
				try {
					this.wait();
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		this.fixed = fixed;
		this.tiled = tile;
	}

	/**
	 * Paint the background.
	 *
	 * @see prefuse.util.display.PaintListener#prePaint(prefuse.Display,
	 *      javafx.scene.canvas.GraphicsContext)
	 */
	@Override
	public void prePaint(final Display d, final GraphicsContext g) {
		final Affine at = g.getTransform();
		final boolean translate = isTranslation(at);

		if (this.fixed || translate) {
			// if the background is fixed, we can unset the transform.
			// if we have no scaling component, we draw the image directly
			// rather than run it through the transform.
			// this avoids rendering artifacts on Java 1.5 on Win32.

			final int tx = this.fixed ? 0 : (int) at.getTx();
			final int ty = this.fixed ? 0 : (int) at.getTy();

			g.setTransform(this.getIdentity());
			if (this.tiled) {
				// if tiled, compute visible background region and draw tiles
				final double w = d.getWidth(), iw = this.image.getWidth();
				final double h = d.getHeight(), ih = this.image.getHeight();

				double sx = this.fixed ? 0 : tx % iw;
				double sy = this.fixed ? 0 : ty % ih;
				if (sx > 0) {
					sx -= iw;
				}
				if (sy > 0) {
					sy -= ih;
				}

				for (double x = sx; x < (w - sx); x += iw) {
					for (double y = sy; y < (h - sy); y += ih) {
						g.drawImage(this.image, x, y);
					}
				}
			} else {
				// if not tiled, simply draw the image at the translated origin
				g.drawImage(this.image, tx, ty);
			}
			g.setTransform(at);
		} else {
			// run the image through the display transform
			if (this.tiled) {
				final double iw = this.image.getWidth();
				final double ih = this.image.getHeight();

				// get the screen region and map it into item-space
				final Clip c = this.getClip();
				c.setClip(0, 0, d.getWidth(), d.getHeight());
				c.transform(d.getInverseTransform());

				// get the bounding region for image tiles
				int w = (int) Math.ceil(c.getWidth());
				int h = (int) Math.ceil(c.getHeight());
				int tx = (int) c.getMinX();
				int ty = (int) c.getMinY();
				final double dw = (tx % iw) + iw;
				final double dh = (ty % ih) + ih;
				tx -= dw;
				w += dw;
				ty -= dh;
				h += dh;

				// draw the image tiles
				for (double x = tx; x < (tx + w); x += iw) {
					for (double y = ty; y < (ty + h); y += ih) {
						g.drawImage(this.image, x, y);
					}
				}
			} else {
				// if not tiled, simply draw the image
				g.drawImage(this.image, 0, 0);
			}
		}

	}

	/**
	 * Check if the given AffineTransform is a translation (within thresholds --
	 * see {@link #THRESH}.
	 */
	private static boolean isTranslation(final Affine at) {
		return ((Math.abs(at.getTx() - 1.0) < THRESH) && (Math.abs(at.getTy() - 1.0) < THRESH)
				&& (Math.abs(at.getMxy()) < THRESH) && (Math.abs(at.getMyx()) < THRESH));
	}

	/**
	 * Get an identity transform (creating it if necessary)
	 */
	private Affine getIdentity() {
		if (this.identity == null) {
			this.identity = new Affine();
		}
		return this.identity;
	}

	/**
	 * Get a clip instance (creating it if necessary)
	 */
	private Clip getClip() {
		if (this.clip == null) {
			this.clip = new Clip();
		}
		return this.clip;
	}

	/**
	 * Does nothing.
	 *
	 * @see prefuse.util.display.PaintListener#postPaint(prefuse.Display,
	 *      javafx.scene.canvas.GraphicsContext)
	 */
	@Override
	public void postPaint(final Display d, final GraphicsContext g) {
		// do nothing
	}

} // end of class BackgroundPainter
