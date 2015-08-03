package prefuse;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JTextArea;
import javax.swing.JToolTip;

import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import prefuse.activity.Activity;
import prefuse.activity.SlowInSlowOutPacer;
import prefuse.controls.Control;
import prefuse.data.expression.AndPredicate;
import prefuse.data.expression.BooleanLiteral;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.render.Renderer;
import prefuse.util.ColorLib;
import prefuse.util.StringLib;
import prefuse.util.UpdateListener;
import prefuse.util.collections.CopyOnWriteArrayList;
import prefuse.util.display.BackgroundPainter;
import prefuse.util.display.Clip;
import prefuse.util.display.DebugStatsPainter;
import prefuse.util.display.ExportDisplayAction;
import prefuse.util.display.ItemBoundsListener;
import prefuse.util.display.PaintListener;
import prefuse.util.display.RenderingQueue;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.VisiblePredicate;
import prefuse.visual.sort.ItemSorter;

/**
 * A first replacement of Display into JavaFX would be a Canvas or a SceneGraph.
 * However, we might change the techniques used to improve performance and avoid
 * screen flickering, as JavaFX stuff should handle this, we might just use the
 * canvas as it is (without an off screen buffer).
 *
 * <p>
 * User interface component that provides an interactive view onto a
 * visualization. The Display is responsible for drawing items to the screen and
 * providing callbacks for user interface actions such as mouse and keyboard
 * events. A Display must be associated with an {@link prefuse.Visualization}
 * from which it pulls the items to visualize.
 * </p>
 *
 * <p>
 * To control which {@link prefuse.visual.VisualItem} instances are drawn, the
 * Display also maintains an optional {@link prefuse.data.expression.Predicate}
 * for filtering items. The drawing order of items is controlled by an
 * {@link prefuse.visual.sort.ItemSorter} instance, which calculates a score for
 * each item. Items with higher scores are drawn later, and hence on top of
 * lower scoring items.
 * </p>
 *
 * <p>
 * The {@link prefuse.controls.Control Control} interface provides the user
 * interface callbacks for supporting interaction. The {@link prefuse.controls}
 * package contains a number of pre-built <code>Control</code> implementations
 * for common interactions.
 * </p>
 *
 * <p>
 * The Display class also supports arbitrary graphics transforms through the
 * <code>java.awt.geom.AffineTransform</code> class. The
 * {@link #setTransform(java.awt.geom.AffineTransform) setTransform} method
 * allows arbitrary transforms to be applied, while the
 * {@link #pan(double,double) pan} and
 * {@link #zoom(java.awt.geom.Point2D,double) zoom} methods provide convenience
 * methods that appropriately update the current transform to achieve panning
 * and zooming of the presentation space.
 * </p>
 *
 * <p>
 * Additionally, each Display instance also supports use of a text editor to
 * facilitate direct editing of text. See the various
 * {@link #editText(prefuse.visual.VisualItem, String)} methods.
 * </p>
 *
 * @version 1.0
 * @author <a href="http://jheer.org">jeffrey heer</a>
 * @see Visualization
 * @see prefuse.controls.Control
 * @see prefuse.controls
 */
public class Display extends Canvas {

	private static final Logger s_logger = Logger.getLogger(Display.class.getName());

	// visual item source
	protected Visualization m_vis;
	protected AndPredicate m_predicate = new AndPredicate();

	// listeners
	protected CopyOnWriteArrayList m_controls = new CopyOnWriteArrayList();
	protected CopyOnWriteArrayList m_painters;
	protected CopyOnWriteArrayList m_bounders;

	// display
	protected Clip m_clip = new Clip();
	protected Clip m_screen = new Clip();
	protected Clip m_bounds = new Clip();
	protected Rectangle2D m_rclip = new Rectangle2D.Double();
	protected boolean m_damageRedraw = true;
	protected boolean m_highQuality = false;

	// optional background image
	protected BackgroundPainter m_bgpainter = null;

	// rendering queue
	protected RenderingQueue m_queue = new RenderingQueue();
	protected int m_visibleCount = 0;

	// transform variables
	protected AffineTransform m_transform = new AffineTransform();
	protected AffineTransform m_itransform = new AffineTransform();
	protected TransformActivity m_transact = new TransformActivity();
	protected Point2D m_tmpPoint = new Point2D.Double();

	// frame count and debugging output
	protected double frameRate;
	protected int nframes = 0;
	private final int sampleInterval = 10;
	private long mark = -1L;

	/* Custom tooltip, null to use regular tooltip mechanisms */
	protected Tooltip m_customToolTip = null;

	// text editing variables
	private TextField m_editor;
	private boolean m_editing;
	private VisualItem m_editItem;
	private String m_editAttribute;

	/**
	 * Creates a new Display instance. You will need to associate this Display
	 * with a {@link Visualization} for it to display anything.
	 */
	public Display() {
		this(null);
	}

	/**
	 * Creates a new Display associated with the given Visualization. By
	 * default, all {@link prefuse.visual.VisualItem} instances in the
	 * {@link Visualization} will be drawn by the Display.
	 *
	 * @param visualization
	 *            the {@link Visualization} backing this Display
	 */
	public Display(final Visualization visualization) {
		this(visualization, (Predicate) null);
	}

	/**
	 * Creates a new Display associated with the given Visualization that draws
	 * all VisualItems in the visualization that pass the given Predicate. The
	 * predicate string will be parsed by the
	 * {@link prefuse.data.expression.parser.ExpressionParser} to get a
	 * {@link prefuse.data.expression.Predicate} instance.
	 *
	 * @param visualization
	 *            the {@link Visualization} backing this Display
	 * @param predicate
	 *            a predicate expression in the prefuse expression language.
	 *            This expression will be parsed; if the parsing fails or does
	 *            not result in a Predicate instance, an exception will result.
	 */
	public Display(final Visualization visualization, final String predicate) {
		this(visualization, (Predicate) ExpressionParser.parse(predicate, true));
	}

	/**
	 * Creates a new Display associated with the given Visualization that draws
	 * all VisualItems in the visualization that pass the given Predicate.
	 *
	 * @param visualization
	 *            the {@link Visualization} backing this Display
	 * @param predicate
	 *            the filtering {@link prefuse.data.expression.Predicate}
	 */
	public Display(final Visualization visualization, final Predicate predicate) {

		// initialize text editor
		this.m_editing = false;
		this.m_editor = new TextField();
		this.m_editor.setBorder(null);
		this.m_editor.setVisible(false);
		// this.add(this.m_editor);

		// register input event capturer
		this.registerInputEventCapturer();

		this.registerDefaultCommands();

		// invalidate the display when the filter changes
		this.m_predicate.addExpressionListener(new UpdateListener() {
			@Override
			public void update(final Object src) {
				Display.this.damageReport();
			}
		});

		this.setVisualization(visualization);
		this.setPredicate(predicate);
		this.setSize(400, 400); // set a default size
	}

	/**
	 * We have to redirect all kind of event to this internal instance to manage
	 * state of VisualItem.
	 */
	private void registerInputEventCapturer() {
		final InputEventCapturer iec = new InputEventCapturer();
		// basic mouse events
		this.setOnMouseClicked((mouseEvent) -> {
			iec.mouseClicked(mouseEvent);
		});
		this.setOnMouseDragged((mouseEvent) -> {
			iec.mouseDragged(mouseEvent);
		});
		this.setOnMouseEntered((mouseEvent) -> {
			iec.mouseEntered(mouseEvent);
		});
		this.setOnMouseExited((mouseEvent) -> {
			iec.mouseExited(mouseEvent);
		});
		this.setOnMouseMoved((mouseEvent) -> {
			iec.mouseMoved(mouseEvent);
		});
		this.setOnMousePressed((mouseEvent) -> {
			iec.mousePressed(mouseEvent);
		});
		this.setOnMouseReleased((mouseEvent) -> {
			iec.mouseReleased(mouseEvent);
		});
		// scroll events
		this.setOnScroll((scrollEvent) -> {
			iec.mouseWheelMoved(scrollEvent);
		});
		// basic key events
		this.setOnKeyPressed((keyEvent) -> {
			iec.keyPressed(keyEvent);
		});
		this.setOnKeyReleased((keyEvent) -> {
			iec.keyReleased(keyEvent);
		});
		this.setOnKeyTyped((keyEvent) -> {
			iec.keyTyped(keyEvent);
		});
	}

	/**
	 * Resets the display by clearing the offscreen buffer and flushing the
	 * internal rendering queue. This method can help reclaim memory when a
	 * Display is not visible.
	 */
	public void reset() {
		this.m_queue.clean();
	}

	/**
	 * Registers default keystroke commands on the Display. The default commands
	 * are
	 * <ul>
	 * <li><b>ctrl D</b> - Toggle debug info display</li>
	 * <li><b>ctrl H</b> - Toggle high quality rendering</li>
	 * <li><b>ctrl E</b> - Export display view to an image file</li>
	 * </ul>
	 * Subclasses can override this method to prevent these commands from being
	 * set. Additional commands can be registered using the
	 * <code>registerKeyboardAction</code> method.
	 */
	protected void registerDefaultCommands() {
		// add debugging output control
		this.setOnKeyTyped(new EventHandler<KeyEvent>() {
			private PaintListener m_debug = null;

			@Override
			public void handle(final KeyEvent keyEvent) {
				if (keyEvent.isControlDown() && keyEvent.getCode().equals(KeyCode.D)) {
					if (this.m_debug == null) {
						this.m_debug = new DebugStatsPainter();
						Display.this.addPaintListener(this.m_debug);
					} else {
						Display.this.removePaintListener(this.m_debug);
						this.m_debug = null;
					}
				}
			}
		});

		// add quality toggle
		this.setOnKeyTyped((keyEvent) -> {
			if (keyEvent.isControlDown() && keyEvent.getCode().equals(KeyCode.D)) {
				Display.this.setHighQuality(!Display.this.isHighQuality());
			}
		});

		// add image output control, if this is not an applet
		try {
			this.setOnKeyTyped(new ExportDisplayAction(this));
		} catch (final SecurityException se) {
		}
	}

	/**
	 * Set the size of the Display.
	 *
	 * @param width
	 *            the width of the Display in pixels
	 * @param height
	 *            the height of the Display in pixels
	 * @see java.awt.Component#setSize(int, int)
	 */
	public void setSize(final int width, final int height) {
		this.m_offscreen = null;
		setPrefSize(width, height);
		super.setSize(width, height);
	}

	/**
	 * Set the size of the Display.
	 *
	 * @param d
	 *            the dimensions of the Display in pixels
	 * @see java.awt.Component#setSize(java.awt.Dimension)
	 */
	public void setSize(final Dimension d) {
		this.m_offscreen = null;
		setPreferredSize(d);
		super.setSize(d);
	}

	/**
	 * Invalidates this component. Overridden to ensure that an internal damage
	 * report is generated.
	 *
	 * @see java.awt.Component#invalidate()
	 */
	public void invalidate() {
		this.damageReport();
		super.invalidate();
	}

	/**
	 * @see java.awt.Component#setBounds(int, int, int, int)
	 */
	public void setBounds(final int x, final int y, final int w, final int h) {
		this.m_offscreen = null;
		super.setBounds(x, y, w, h);
	}

	/**
	 * Sets the font used by this Display. This determines the font used by this
	 * Display's text editor and in any debugging text.
	 *
	 * @param f
	 *            the Font to use
	 */
	public void setFont(final Font f) {
		super.setFont(f);
		this.m_editor.setFont(f);
	}

	/**
	 * Returns the running average frame rate for this Display.
	 *
	 * @return the frame rate
	 */
	public double getFrameRate() {
		return this.frameRate;
	}

	/**
	 * Determines if the Display uses a higher quality rendering, using
	 * anti-aliasing. This causes drawing to be much slower, however, and so is
	 * disabled by default.
	 *
	 * @param on
	 *            true to enable anti-aliased rendering, false to disable it
	 */
	public void setHighQuality(final boolean on) {
		if (this.m_highQuality != on) {
			this.damageReport();
		}
		this.m_highQuality = on;
	}

	/**
	 * Indicates if the Display is using high quality (return value true) or
	 * regular quality (return value false) rendering.
	 *
	 * @return true if high quality rendering is enabled, false otherwise
	 */
	public boolean isHighQuality() {
		return this.m_highQuality;
	}

	/**
	 * Returns the Visualization backing this Display.
	 *
	 * @return this Display's {@link Visualization}
	 */
	public Visualization getVisualization() {
		return this.m_vis;
	}

	/**
	 * Set the Visualiztion associated with this Display. This Display will
	 * render the items contained in the provided visualization. If this Display
	 * is already associated with a different Visualization, the Display
	 * unregisters itself with the previous one.
	 *
	 * @param vis
	 *            the backing {@link Visualization} to use.
	 */
	public void setVisualization(final Visualization vis) {
		// TODO: synchronization?
		if (this.m_vis == vis) {
			// nothing need be done
			return;
		} else if (this.m_vis != null) {
			// remove this display from it's previous registry
			this.m_vis.removeDisplay(this);
		}
		this.m_vis = vis;
		if (this.m_vis != null) {
			this.m_vis.addDisplay(this);
		}
	}

	/**
	 * Returns the filtering Predicate used to control what items are drawn by
	 * this display.
	 *
	 * @return the filtering {@link prefuse.data.expression.Predicate}
	 */
	public Predicate getPredicate() {
		if (this.m_predicate.size() == 1) {
			return BooleanLiteral.TRUE;
		} else {
			return this.m_predicate.get(0);
		}
	}

	/**
	 * Sets the filtering Predicate used to control what items are drawn by this
	 * Display.
	 *
	 * @param expr
	 *            the filtering predicate to use. The predicate string will be
	 *            parsed by the
	 *            {@link prefuse.data.expression.parser.ExpressionParser}. If
	 *            the parse fails or does not result in a
	 *            {@link prefuse.data.expression.Predicate} instance, an
	 *            exception will be thrown.
	 */
	public void setPredicate(final String expr) {
		final Predicate p = (Predicate) ExpressionParser.parse(expr, true);
		this.setPredicate(p);
	}

	/**
	 * Sets the filtering Predicate used to control what items are drawn by this
	 * Display.
	 *
	 * @param p
	 *            the filtering {@link prefuse.data.expression.Predicate} to use
	 */
	public synchronized void setPredicate(final Predicate p) {
		if (p == null) {
			this.m_predicate.set(VisiblePredicate.TRUE);
		} else {
			this.m_predicate.set(new Predicate[] { p, VisiblePredicate.TRUE });
		}
	}

	/**
	 * Returns the number of visible items processed by this Display. This
	 * includes items not currently visible on screen due to the current panning
	 * or zooming state.
	 *
	 * @return the count of visible items
	 */
	public int getVisibleItemCount() {
		return this.m_visibleCount;
	}

	/**
	 * Get the ItemSorter that determines the rendering order of the
	 * VisualItems. Items are drawn in ascending order of the scores provided by
	 * the ItemSorter.
	 *
	 * @return this Display's {@link prefuse.visual.sort.ItemSorter}
	 */
	public ItemSorter getItemSorter() {
		return this.m_queue.sort;
	}

	/**
	 * Set the ItemSorter that determines the rendering order of the
	 * VisualItems. Items are drawn in ascending order of the scores provided by
	 * the ItemSorter.
	 *
	 * @return the {@link prefuse.visual.sort.ItemSorter} to use
	 */
	public synchronized void setItemSorter(final ItemSorter cmp) {
		this.damageReport();
		this.m_queue.sort = cmp;
	}

	/**
	 * Set a background image for this display.
	 *
	 * @param image
	 *            the background Image. If a null value is provided, than no
	 *            background image will be shown.
	 * @param fixed
	 *            true if the background image should stay in a fixed position,
	 *            invariant to panning, zooming, or rotation; false if the image
	 *            should be subject to view transforms
	 * @param tileImage
	 *            true to tile the image across the visible background, false to
	 *            only include the image once
	 */
	public synchronized void setBackgroundImage(final Image image, final boolean fixed, final boolean tileImage) {
		BackgroundPainter bg = null;
		if (image != null) {
			bg = new BackgroundPainter(image, fixed, tileImage);
		}
		this.setBackgroundPainter(bg);
	}

	/**
	 * Set a background image for this display.
	 *
	 * @param location
	 *            a location String of where to retrieve the image file from.
	 *            Uses {@link prefuse.util.io.IOLib#urlFromString(String)} to
	 *            resolve the String. If a null value is provided, than no
	 *            background image will be shown.
	 * @param fixed
	 *            true if the background image should stay in a fixed position,
	 *            invariant to panning, zooming, or rotation; false if the image
	 *            should be subject to view transforms
	 * @param tileImage
	 *            true to tile the image across the visible background, false to
	 *            only include the image once
	 */
	public synchronized void setBackgroundImage(final String location, final boolean fixed, final boolean tileImage) {
		BackgroundPainter bg = null;
		if (location != null) {
			bg = new BackgroundPainter(location, fixed, tileImage);
		}
		this.setBackgroundPainter(bg);
	}

	private void setBackgroundPainter(final BackgroundPainter bg) {
		if (this.m_bgpainter != null) {
			this.removePaintListener(this.m_bgpainter);
		}
		this.m_bgpainter = bg;
		if (bg != null) {
			this.addPaintListener(bg);
		}
	}

	// ------------------------------------------------------------------------
	// ToolTips

	/**
	 * Returns the tooltip instance to use for this Display. By default, uses
	 * the normal Swing tooltips, returning the result of this same method
	 * invoked on the JComponent super-class. If a custom tooltip has been set,
	 * that is returned instead.
	 *
	 * @see #setCustomToolTip(JToolTip)
	 * @see javax.swing.JComponent#createToolTip()
	 */
	public Tooltip createToolTip() {
		if (this.m_customToolTip == null) {
			return super.createToolTip();
		} else {
			return this.m_customToolTip;
		}
	}

	/**
	 * Set a custom tooltip to use for this Display. To trigger tooltip display,
	 * you must still use the <code>setToolTipText</code> method as usual. The
	 * actual text will no longer have any effect, other than that a null text
	 * value will result in no tooltip display while a non-null text value will
	 * result in a tooltip being shown. Clients are responsible for setting the
	 * tool tip text to enable/disable tooltips as well as updating the content
	 * of their own custom tooltip instance.
	 *
	 * @param tooltip
	 *            the tooltip component to use
	 * @see prefuse.util.ui.JCustomTooltip
	 */
	public void setCustomToolTip(final Tooltip tooltip) {
		this.m_customToolTip = tooltip;
	}

	/**
	 * Get the custom tooltip used by this Display. Returns null if normal
	 * tooltips are being used.
	 *
	 * @return the custom tooltip used by this Display, or null if none
	 */
	public Tooltip getCustomToolTip() {
		return this.m_customToolTip;
	}

	// ------------------------------------------------------------------------
	// Clip / Bounds Management

	/**
	 * Indicates if damage/redraw rendering is enabled. If enabled, the display
	 * will only redraw within the bounding box of all areas that have changed
	 * since the last rendering operation. For small changes, such as a single
	 * item being dragged, this can result in a significant performance
	 * increase. By default, the damage/redraw optimization is enabled. It can
	 * be disabled, however, if rendering artifacts are appearing in your
	 * visualization. Be careful though, as this may not be the best solution.
	 * Rendering artifacts may result because the item bounds returned by
	 * {@link prefuse.visual.VisualItem#getBounds()} are not accurate and the
	 * item's {@link prefuse.render.Renderer} is drawing outside of the reported
	 * bounds. In this case, there is usually a bug in the Renderer. One
	 * reported problem arises from Java itself, however, which inaccurately
	 * redraws images outside of their reported bounds. If you have a
	 * visulization with a number of images and are seeing rendering artifacts,
	 * try disabling damage/redraw.
	 *
	 * @return true if damage/redraw optimizations are enabled, false otherwise
	 *         (in which case the entire Display is redrawn upon a repaint)
	 */
	public synchronized boolean isDamageRedraw() {
		return this.m_damageRedraw;
	}

	/**
	 * Sets if damage/redraw rendering is enabled. If enabled, the display will
	 * only redraw within the bounding box of all areas that have changed since
	 * the last rendering operation. For small changes, such as a single item
	 * being dragged, this can result in a significant performance increase. By
	 * default, the damage/redraw optimization is enabled. It can be disabled,
	 * however, if rendering artifacts are appearing in your visualization. Be
	 * careful though, as this may not be the best solution. Rendering artifacts
	 * may result because the item bounds returned by
	 * {@link prefuse.visual.VisualItem#getBounds()} are not accurate and the
	 * item's {@link prefuse.render.Renderer} is drawing outside of the reported
	 * bounds. In this case, there is usually a bug in the Renderer. One
	 * reported problem arises from Java itself, however, which inaccurately
	 * redraws images outside of their reported bounds. If you have a
	 * visulization with a number of images and are seeing rendering artifacts,
	 * try disabling damage/redraw.
	 *
	 * @param b
	 *            true to enable damage/redraw optimizations, false otherwise
	 *            (in which case the entire Display will be redrawn upon a
	 *            repaint)
	 */
	public synchronized void setDamageRedraw(final boolean b) {
		this.m_damageRedraw = b;
		this.m_clip.invalidate();
	}

	/**
	 * Reports damage to the Display within in the specified region.
	 *
	 * @param region
	 *            the damaged region, in absolute coordinates
	 */
	public synchronized void damageReport(final Rectangle2D region) {
		if (this.m_damageRedraw) {
			this.m_clip.union(region);
		}
	}

	/**
	 * Reports damage to the entire Display.
	 */
	public synchronized void damageReport() {
		this.m_clip.invalidate();
	}

	/**
	 * Clears any reports of damaged regions, causing the Display to believe
	 * that the display contents are up-to-date. If used incorrectly this can
	 * cause inaccurate rendering. <strong>Call this method only if you know
	 * what you are doing.</strong>
	 */
	public synchronized void clearDamage() {
		if (this.m_damageRedraw) {
			this.m_clip.reset();
		}
	}

	/**
	 * Returns the bounds, in absolute (item-space) coordinates, of the total
	 * bounds occupied by all currently visible VisualItems. This method
	 * allocates a new Rectangle2D instance for the result.
	 *
	 * @return the bounding box of all visibile VisualItems
	 * @see #getItemBounds(Rectangle2D)
	 */
	public synchronized Rectangle2D getItemBounds() {
		return this.getItemBounds(new Rectangle2D.Double());
	}

	/**
	 * Returns the bounds, in absolute (item-space) coordinates, of the total
	 * bounds occupied by all currently visible VisualItems.
	 *
	 * @param b
	 *            the Rectangle2D to use to store the return value
	 * @return the bounding box of all visibile VisualItems
	 */
	public synchronized Rectangle2D getItemBounds(final Rectangle2D b) {
		b.setFrameFromDiagonal(this.m_bounds.getMinX(), this.m_bounds.getMinY(), this.m_bounds.getMaxX(),
				this.m_bounds.getMaxY());
		return b;
	}

	// ------------------------------------------------------------------------
	// Rendering

	/**
	 * Returns the offscreen buffer used for double buffering.
	 *
	 * @return the offscreen buffer
	 */
	public BufferedImage getOffscreenBuffer() {
		return this.m_offscreen;
	}

	/**
	 * Creates a new buffered image to use as an offscreen buffer.
	 */
	protected BufferedImage getNewOffscreenBuffer(final int width, final int height) {
		BufferedImage img = null;
		if (!GraphicsEnvironment.isHeadless()) {
			try {
				img = (BufferedImage) createImage(width, height);
			} catch (final Exception e) {
				img = null;
			}
		}
		if (img == null) {
			return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		}
		return img;
	}

	/**
	 * Saves a copy of this display as an image to the specified output stream.
	 *
	 * @param output
	 *            the output stream to write to.
	 * @param format
	 *            the image format (e.g., "JPG", "PNG"). The number and kind of
	 *            available formats varies by platform. See
	 *            {@link javax.imageio.ImageIO} and related classes for more.
	 * @param scale
	 *            how much to scale the image by. For example, a value of 2.0
	 *            will result in an image with twice the pixel width and height
	 *            of this Display.
	 * @return true if image was successfully saved, false if an error occurred.
	 */
	public boolean saveImage(final OutputStream output, final String format, final double scale) {
		try {
			// get an image to draw into
			final Dimension d = new Dimension((int) (scale * this.getWidth()), (int) (scale * this.getHeight()));
			final BufferedImage img = this.getNewOffscreenBuffer(d.width, d.height);
			final Graphics2D g = (Graphics2D) img.getGraphics();

			// set up the display, render, then revert to normal settings
			final Point2D p = new Point2D.Double(0, 0);
			this.zoom(p, scale); // also takes care of damage report
			final boolean q = this.isHighQuality();
			this.setHighQuality(true);
			this.paintDisplay(g, d);
			this.setHighQuality(q);
			this.zoom(p, 1 / scale); // also takes care of damage report

			// save the image and return
			ImageIO.write(img, format, output);
			return true;
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * @see java.awt.Component#update(java.awt.Graphics)
	 */
	public void update(final Graphics g) {
		paint(g);
	}

	/**
	 * Paints the offscreen buffer to the provided graphics context.
	 *
	 * @param g
	 *            the Graphics context to paint to
	 */
	protected void paintBufferToScreen(final Graphics g) {
		synchronized (this) {
			g.drawImage(this.m_offscreen, 0, 0, null);
		}
	}

	/**
	 * Immediately repaints the contents of the offscreen buffer to the screen.
	 * This bypasses the usual rendering loop.
	 */
	public void repaintImmediate() {
		final Graphics g = this.getGraphics();
		if ((g != null) && (this.m_offscreen != null)) {
			this.paintBufferToScreen(g);
		}
	}

	/**
	 * Sets the transform of the provided Graphics context to be the transform
	 * of this Display and sets the desired rendering hints.
	 *
	 * @param g
	 *            the Graphics context to prepare.
	 */
	protected void prepareGraphics(final Graphics2D g) {
		if (this.m_transform != null) {
			g.transform(this.m_transform);
		}
		this.setRenderingHints(g);
	}

	/**
	 * Sets the rendering hints that should be used while drawing the
	 * visualization to the screen. Subclasses can override this method to set
	 * hints as desired. Such subclasses should consider honoring the high
	 * quality flag in one form or another.
	 *
	 * @param g
	 *            the Graphics context on which to set the rendering hints
	 */
	protected void setRenderingHints(final Graphics2D g) {
		if (this.m_highQuality) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		} else {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		}
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
	}

	/**
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	public void paintComponent(final Graphics g) {
		if (this.m_offscreen == null) {
			this.m_offscreen = this.getNewOffscreenBuffer(this.getWidth(), this.getHeight());
			this.damageReport();
		}
		final Graphics2D g2D = (Graphics2D) g;
		final Graphics2D buf_g2D = (Graphics2D) this.m_offscreen.getGraphics();

		// Why not fire a pre-paint event here?
		// Pre-paint events are fired by the clearRegion method

		// paint the visualization
		this.paintDisplay(buf_g2D, getSize());
		this.paintBufferToScreen(g2D);

		// fire post-paint events to any painters
		this.firePostPaint(g2D);

		buf_g2D.dispose();

		// compute frame rate
		this.nframes++;
		if (this.mark < 0) {
			this.mark = System.currentTimeMillis();
			this.nframes = 0;
		} else if (this.nframes == this.sampleInterval) {
			final long t = System.currentTimeMillis();
			this.frameRate = (1000.0 * this.nframes) / (t - this.mark);
			this.mark = t;
			this.nframes = 0;
		}
	}

	/**
	 * Renders the display within the given graphics context and size bounds.
	 *
	 * @param g2D
	 *            the <code>Graphics2D</code> context to use for rendering
	 * @param d
	 *            the rendering width and height of the Display
	 */
	public void paintDisplay(final Graphics2D g2D, final Dimension d) {
		// if double-locking *ALWAYS* lock on the visualization first
		synchronized (this.m_vis) {
			synchronized (this) {

				if (this.m_clip.isEmpty()) {
					return; // no damage, no render
				}

				// map the screen bounds to absolute coords
				this.m_screen.setClip(0, 0, d.width + 1, d.height + 1);
				this.m_screen.transform(this.m_itransform);

				// compute the approximate size of an "absolute pixel"
				// values too large are OK (though cause unnecessary rendering)
				// values too small will cause incorrect rendering
				final double pixel = 1.0 + (1.0 / this.getScale());

				if (this.m_damageRedraw) {
					if (this.m_clip.isInvalid()) {
						// if clip is invalid, we clip to the entire screen
						this.m_clip.setClip(this.m_screen);
					} else {
						// otherwise intersect damaged region with display
						// bounds
						this.m_clip.intersection(this.m_screen);
					}

					// expand the clip by the extra pixel margin
					this.m_clip.expand(pixel);

					// set the transform, rendering keys, etc
					this.prepareGraphics(g2D);

					// now set the actual rendering clip
					this.m_rclip.setFrameFromDiagonal(this.m_clip.getMinX(), this.m_clip.getMinY(),
							this.m_clip.getMaxX(), this.m_clip.getMaxY());
					g2D.setClip(this.m_rclip);

					// finally, we want to clear the region we'll redraw. we
					// clear
					// a slightly larger area than the clip. if we don't do
					// this,
					// we sometimes get rendering artifacts, possibly due to
					// scaling mismatches in the Java2D implementation
					this.m_rclip.setFrameFromDiagonal(this.m_clip.getMinX() - pixel, this.m_clip.getMinY() - pixel,
							this.m_clip.getMaxX() + pixel, this.m_clip.getMaxY() + pixel);

				} else {
					// set the background region to clear
					this.m_rclip.setFrame(this.m_screen.getMinX(), this.m_screen.getMinY(), this.m_screen.getWidth(),
							this.m_screen.getHeight());

					// set the item clip to the current screen
					this.m_clip.setClip(this.m_screen);

					// set the transform, rendering keys, etc
					this.prepareGraphics(g2D);
				}

				// now clear the region
				this.clearRegion(g2D, this.m_rclip);

				// -- render ----------------------------
				// the actual rendering loop

				// copy current item bounds into m_rclip, reset item bounds
				this.getItemBounds(this.m_rclip);
				this.m_bounds.reset();

				// fill the rendering and picking queues
				this.m_queue.clear(); // clear the queue
				final Iterator items = this.m_vis.items(this.m_predicate);
				for (this.m_visibleCount = 0; items.hasNext(); ++this.m_visibleCount) {
					final VisualItem item = (VisualItem) items.next();
					final Rectangle2D bounds = item.getBounds();
					this.m_bounds.union(bounds); // add to item bounds

					if (this.m_clip.intersects(bounds, pixel)) {
						this.m_queue.addToRenderQueue(item);
					}
					if (item.isInteractive()) {
						this.m_queue.addToPickingQueue(item);
					}
				}

				// sort the rendering queue
				this.m_queue.sortRenderQueue();

				// render each visual item
				for (int i = 0; i < this.m_queue.rsize; ++i) {
					this.m_queue.ritems[i].render(g2D);
				}

				// no more damage so reset the clip
				if (this.m_damageRedraw) {
					this.m_clip.reset();
				}

				// fire bounds change, if appropriate
				this.checkItemBoundsChanged(this.m_rclip);

			}
		} // end synchronized block
	}

	/**
	 * Immediately render the given VisualItem to the screen. This method
	 * bypasses the Display's offscreen buffer.
	 *
	 * @param item
	 *            the VisualItem to render immediately
	 */
	public void renderImmediate(final VisualItem item) {
		final Graphics2D g2D = (Graphics2D) this.getGraphics();
		this.prepareGraphics(g2D);
		item.render(g2D);
	}

	/**
	 * Paints the graph to the provided graphics context, for output to a
	 * printer. This method does not double buffer the painting, in order to
	 * provide the maximum print quality.
	 *
	 * <b>This method may not be working correctly, and will be repaired at a
	 * later date.</b>
	 *
	 * @param g
	 *            the printer graphics context.
	 */
	protected void printComponent(final Graphics g) {
		final boolean wasHighQuality = this.m_highQuality;
		try {
			// Set the quality to high for the duration of the printing.
			this.m_highQuality = true;
			// Paint directly to the print graphics context.
			this.paintDisplay((Graphics2D) g, getSize());
		} finally {
			// Reset the quality to the state it was in before printing.
			this.m_highQuality = wasHighQuality;
		}
	}

	/**
	 * Clears the specified region of the display in the display's offscreen
	 * buffer.
	 */
	protected void clearRegion(final Graphics2D g, final Rectangle2D r) {
		g.setColor(getBackground());
		g.fill(r);
		// fire pre-paint events to any painters
		this.firePrePaint(g);
	}

	// ------------------------------------------------------------------------
	// Transformations

	/**
	 * Set the 2D AffineTransform (e.g., scale, shear, pan, rotate) used by this
	 * display before rendering visual items. The provided transform must be
	 * invertible, otherwise an expection will be thrown. For simple panning and
	 * zooming transforms, you can instead use the provided pan() and zoom()
	 * methods.
	 */
	public synchronized void setTransform(final AffineTransform transform) throws NoninvertibleTransformException {
		this.damageReport();
		this.m_transform = transform;
		this.m_itransform = this.m_transform.createInverse();
	}

	/**
	 * Returns a reference to the AffineTransformation used by this Display.
	 * Changes made to this reference WILL corrupt the state of this display.
	 * Use setTransform() to safely update the transform state.
	 *
	 * @return the AffineTransform
	 */
	public AffineTransform getTransform() {
		return this.m_transform;
	}

	/**
	 * Returns a reference to the inverse of the AffineTransformation used by
	 * this display. Direct changes made to this reference WILL corrupt the
	 * state of this display.
	 *
	 * @return the inverse AffineTransform
	 */
	public AffineTransform getInverseTransform() {
		return this.m_itransform;
	}

	/**
	 * Gets the absolute co-ordinate corresponding to the given screen
	 * co-ordinate.
	 *
	 * @param screen
	 *            the screen co-ordinate to transform
	 * @param abs
	 *            a reference to put the result in. If this is the same object
	 *            as the screen co-ordinate, it will be overridden safely. If
	 *            this value is null, a new Point2D instance will be created and
	 *            returned.
	 * @return the point in absolute co-ordinates
	 */
	public Point2D getAbsoluteCoordinate(final Point2D screen, final Point2D abs) {
		return this.m_itransform.transform(screen, abs);
	}

	/**
	 * Returns the current scale (zoom) value.
	 *
	 * @return the current scale. This is the scaling factor along the
	 *         x-dimension, so be careful when using this value in rare
	 *         non-uniform scaling cases.
	 */
	public double getScale() {
		return this.m_transform.getScaleX();
	}

	/**
	 * Returns the x-coordinate of the top-left of the display, in absolute
	 * (item-space) co-ordinates.
	 *
	 * @return the x co-ord of the top-left corner, in absolute coordinates
	 */
	public double getDisplayX() {
		return -this.m_transform.getTranslateX();
	}

	/**
	 * Returns the y-coordinate of the top-left of the display, in absolute
	 * (item-space) co-ordinates.
	 *
	 * @return the y co-ord of the top-left corner, in absolute coordinates
	 */
	public double getDisplayY() {
		return -this.m_transform.getTranslateY();
	}

	/**
	 * Pans the view provided by this display in screen coordinates.
	 *
	 * @param dx
	 *            the amount to pan along the x-dimension, in pixel units
	 * @param dy
	 *            the amount to pan along the y-dimension, in pixel units
	 */
	public synchronized void pan(final double dx, final double dy) {
		this.m_tmpPoint.setLocation(dx, dy);
		this.m_itransform.transform(this.m_tmpPoint, this.m_tmpPoint);
		double panx = this.m_tmpPoint.getX();
		double pany = this.m_tmpPoint.getY();
		this.m_tmpPoint.setLocation(0, 0);
		this.m_itransform.transform(this.m_tmpPoint, this.m_tmpPoint);
		panx -= this.m_tmpPoint.getX();
		pany -= this.m_tmpPoint.getY();
		this.panAbs(panx, pany);

	}

	/**
	 * Pans the view provided by this display in absolute (i.e. item-space)
	 * coordinates.
	 *
	 * @param dx
	 *            the amount to pan along the x-dimension, in absolute co-ords
	 * @param dy
	 *            the amount to pan along the y-dimension, in absolute co-ords
	 */
	public synchronized void panAbs(final double dx, final double dy) {
		this.damageReport();
		this.m_transform.translate(dx, dy);
		try {
			this.m_itransform = this.m_transform.createInverse();
		} catch (final Exception e) {
			/* will never happen here */ }
	}

	/**
	 * Pans the display view to center on the provided point in screen (pixel)
	 * coordinates.
	 *
	 * @param p
	 *            the point to center on, in screen co-ords
	 */
	public synchronized void panTo(final Point2D p) {
		this.m_itransform.transform(p, this.m_tmpPoint);
		this.panToAbs(this.m_tmpPoint);
	}

	/**
	 * Pans the display view to center on the provided point in absolute (i.e.
	 * item-space) coordinates.
	 *
	 * @param p
	 *            the point to center on, in absolute co-ords
	 */
	public synchronized void panToAbs(final Point2D p) {
		final double sx = this.m_transform.getScaleX();
		final double sy = this.m_transform.getScaleY();
		double x = p.getX();
		x = (Double.isNaN(x) ? 0 : x);
		double y = p.getY();
		y = (Double.isNaN(y) ? 0 : y);
		x = (this.getWidth() / (2 * sx)) - x;
		y = (this.getHeight() / (2 * sy)) - y;

		final double dx = x - (this.m_transform.getTranslateX() / sx);
		final double dy = y - (this.m_transform.getTranslateY() / sy);

		this.damageReport();
		this.m_transform.translate(dx, dy);
		try {
			this.m_itransform = this.m_transform.createInverse();
		} catch (final Exception e) {
			/* will never happen here */ }
	}

	/**
	 * Zooms the view provided by this display by the given scale, anchoring the
	 * zoom at the specified point in screen coordinates.
	 *
	 * @param p
	 *            the anchor point for the zoom, in screen coordinates
	 * @param scale
	 *            the amount to zoom by
	 */
	public synchronized void zoom(final Point2D p, final double scale) {
		this.m_itransform.transform(p, this.m_tmpPoint);
		this.zoomAbs(this.m_tmpPoint, scale);
	}

	/**
	 * Zooms the view provided by this display by the given scale, anchoring the
	 * zoom at the specified point in absolute coordinates.
	 *
	 * @param p
	 *            the anchor point for the zoom, in absolute (i.e. item-space)
	 *            co-ordinates
	 * @param scale
	 *            the amount to zoom by
	 */
	public synchronized void zoomAbs(final Point2D p, final double scale) {
		;
		final double zx = p.getX(), zy = p.getY();
		this.damageReport();
		this.m_transform.translate(zx, zy);
		this.m_transform.scale(scale, scale);
		this.m_transform.translate(-zx, -zy);
		try {
			this.m_itransform = this.m_transform.createInverse();
		} catch (final Exception e) {
			/* will never happen here */ }
	}

	/**
	 * Rotates the view provided by this display by the given angle in radians,
	 * anchoring the rotation at the specified point in screen coordinates.
	 *
	 * @param p
	 *            the anchor point for the rotation, in screen coordinates
	 * @param theta
	 *            the angle to rotate by, in radians
	 */
	public synchronized void rotate(final Point2D p, final double theta) {
		this.m_itransform.transform(p, this.m_tmpPoint);
		this.rotateAbs(this.m_tmpPoint, theta);
	}

	/**
	 * Rotates the view provided by this display by the given angle in radians,
	 * anchoring the rotation at the specified point in absolute coordinates.
	 *
	 * @param p
	 *            the anchor point for the rotation, in absolute (i.e.
	 *            item-space) co-ordinates
	 * @param theta
	 *            the angle to rotation by, in radians
	 */
	public synchronized void rotateAbs(final Point2D p, final double theta) {
		final double zx = p.getX(), zy = p.getY();
		this.damageReport();
		this.m_transform.translate(zx, zy);
		this.m_transform.rotate(theta);
		this.m_transform.translate(-zx, -zy);
		try {
			this.m_itransform = this.m_transform.createInverse();
		} catch (final Exception e) {
			/* will never happen here */ }
	}

	/**
	 * Animate a pan along the specified distance in screen (pixel) co-ordinates
	 * using the provided duration.
	 *
	 * @param dx
	 *            the amount to pan along the x-dimension, in pixel units
	 * @param dy
	 *            the amount to pan along the y-dimension, in pixel units
	 * @param duration
	 *            the duration of the animation, in milliseconds
	 */
	public synchronized void animatePan(final double dx, final double dy, final long duration) {
		final double panx = dx / this.m_transform.getScaleX();
		final double pany = dy / this.m_transform.getScaleY();
		this.animatePanAbs(panx, pany, duration);
	}

	/**
	 * Animate a pan along the specified distance in absolute (item-space)
	 * co-ordinates using the provided duration.
	 *
	 * @param dx
	 *            the amount to pan along the x-dimension, in absolute co-ords
	 * @param dy
	 *            the amount to pan along the y-dimension, in absolute co-ords
	 * @param duration
	 *            the duration of the animation, in milliseconds
	 */
	public synchronized void animatePanAbs(final double dx, final double dy, final long duration) {
		this.m_transact.pan(dx, dy, duration);
	}

	/**
	 * Animate a pan to the specified location in screen (pixel) co-ordinates
	 * using the provided duration.
	 *
	 * @param p
	 *            the point to pan to in screen (pixel) units
	 * @param duration
	 *            the duration of the animation, in milliseconds
	 */
	public synchronized void animatePanTo(final Point2D p, final long duration) {
		final Point2D pp = new Point2D.Double();
		this.m_itransform.transform(p, pp);
		this.animatePanToAbs(pp, duration);
	}

	/**
	 * Animate a pan to the specified location in absolute (item-space)
	 * co-ordinates using the provided duration.
	 *
	 * @param p
	 *            the point to pan to in absolute (item-space) units
	 * @param duration
	 *            the duration of the animation, in milliseconds
	 */
	public synchronized void animatePanToAbs(final Point2D p, final long duration) {
		this.m_tmpPoint.setLocation(0, 0);
		this.m_itransform.transform(this.m_tmpPoint, this.m_tmpPoint);
		double x = p.getX();
		x = (Double.isNaN(x) ? 0 : x);
		double y = p.getY();
		y = (Double.isNaN(y) ? 0 : y);
		final double w = this.getWidth() / (2 * this.m_transform.getScaleX());
		final double h = this.getHeight() / (2 * this.m_transform.getScaleY());
		final double dx = (w - x) + this.m_tmpPoint.getX();
		final double dy = (h - y) + this.m_tmpPoint.getY();
		this.animatePanAbs(dx, dy, duration);
	}

	/**
	 * Animate a zoom centered on a given location in screen (pixel)
	 * co-ordinates by the given scale using the provided duration.
	 *
	 * @param p
	 *            the point to center on in screen (pixel) units
	 * @param scale
	 *            the scale factor to zoom by
	 * @param duration
	 *            the duration of the animation, in milliseconds
	 */
	public synchronized void animateZoom(final Point2D p, final double scale, final long duration) {
		final Point2D pp = new Point2D.Double();
		this.m_itransform.transform(p, pp);
		this.animateZoomAbs(pp, scale, duration);
	}

	/**
	 * Animate a zoom centered on a given location in absolute (item-space)
	 * co-ordinates by the given scale using the provided duration.
	 *
	 * @param p
	 *            the point to center on in absolute (item-space) units
	 * @param scale
	 *            the scale factor to zoom by
	 * @param duration
	 *            the duration of the animation, in milliseconds
	 */
	public synchronized void animateZoomAbs(final Point2D p, final double scale, final long duration) {
		this.m_transact.zoom(p, scale, duration);
	}

	/**
	 * Animate a pan to the specified location in screen (pixel) co-ordinates
	 * and zoom to the given scale using the provided duration.
	 *
	 * @param p
	 *            the point to center on in screen (pixel) units
	 * @param scale
	 *            the scale factor to zoom by
	 * @param duration
	 *            the duration of the animation, in milliseconds
	 */
	public synchronized void animatePanAndZoomTo(final Point2D p, final double scale, final long duration) {
		final Point2D pp = new Point2D.Double();
		this.m_itransform.transform(p, pp);
		this.animatePanAndZoomToAbs(pp, scale, duration);
	}

	/**
	 * Animate a pan to the specified location in absolute (item-space)
	 * co-ordinates and zoom to the given scale using the provided duration.
	 *
	 * @param p
	 *            the point to center on in absolute (item-space) units
	 * @param scale
	 *            the scale factor to zoom by
	 * @param duration
	 *            the duration of the animation, in milliseconds
	 */
	public synchronized void animatePanAndZoomToAbs(final Point2D p, final double scale, final long duration) {
		this.m_transact.panAndZoom(p, scale, duration);
	}

	/**
	 * Indicates if a view transformation is currently underway.
	 *
	 * @return true if a transform is in progress, false otherwise
	 */
	public boolean isTranformInProgress() {
		return this.m_transact.isRunning();
	}

	/**
	 * Activity for conducting animated view transformations.
	 */
	protected class TransformActivity extends Activity {
		// TODO: clean this up to be more general...
		// TODO: change mechanism so that multiple transform
		// activities can be running at once?

		private final double[] src, dst;
		private final AffineTransform m_at;

		public TransformActivity() {
			super(2000, 20, 0);
			this.src = new double[6];
			this.dst = new double[6];
			this.m_at = new AffineTransform();
			this.setPacingFunction(new SlowInSlowOutPacer());
		}

		private AffineTransform getTransform() {
			if (this.isScheduled()) {
				this.m_at.setTransform(this.dst[0], this.dst[1], this.dst[2], this.dst[3], this.dst[4], this.dst[5]);
			} else {
				this.m_at.setTransform(Display.this.m_transform);
			}
			return this.m_at;
		}

		public void panAndZoom(final Point2D p, final double scale, final long duration) {
			final AffineTransform at = this.getTransform();
			this.cancel();
			this.setDuration(duration);

			Display.this.m_tmpPoint.setLocation(0, 0);
			Display.this.m_itransform.transform(Display.this.m_tmpPoint, Display.this.m_tmpPoint);
			double x = p.getX();
			x = (Double.isNaN(x) ? 0 : x);
			double y = p.getY();
			y = (Double.isNaN(y) ? 0 : y);
			final double w = Display.this.getWidth() / (2 * Display.this.m_transform.getScaleX());
			final double h = Display.this.getHeight() / (2 * Display.this.m_transform.getScaleY());
			final double dx = (w - x) + Display.this.m_tmpPoint.getX();
			final double dy = (h - y) + Display.this.m_tmpPoint.getY();
			at.translate(dx, dy);

			at.translate(p.getX(), p.getY());
			at.scale(scale, scale);
			at.translate(-p.getX(), -p.getY());

			at.getMatrix(this.dst);
			Display.this.m_transform.getMatrix(this.src);
			this.run();
		}

		public void pan(final double dx, final double dy, final long duration) {
			final AffineTransform at = this.getTransform();
			this.cancel();
			this.setDuration(duration);
			at.translate(dx, dy);
			at.getMatrix(this.dst);
			Display.this.m_transform.getMatrix(this.src);
			this.run();
		}

		public void zoom(final Point2D p, final double scale, final long duration) {
			final AffineTransform at = this.getTransform();
			this.cancel();
			this.setDuration(duration);
			final double zx = p.getX(), zy = p.getY();
			at.translate(zx, zy);
			at.scale(scale, scale);
			at.translate(-zx, -zy);
			at.getMatrix(this.dst);
			Display.this.m_transform.getMatrix(this.src);
			this.run();
		}

		@Override
		protected void run(final long elapsedTime) {
			final double f = this.getPace(elapsedTime);
			Display.this.damageReport();
			Display.this.m_transform.setTransform(this.src[0] + (f * (this.dst[0] - this.src[0])),
					this.src[1] + (f * (this.dst[1] - this.src[1])), this.src[2] + (f * (this.dst[2] - this.src[2])),
					this.src[3] + (f * (this.dst[3] - this.src[3])), this.src[4] + (f * (this.dst[4] - this.src[4])),
					this.src[5] + (f * (this.dst[5] - this.src[5])));
			try {
				Display.this.m_itransform = Display.this.m_transform.createInverse();
			} catch (final Exception e) {
				/* won't happen */ }
			repaint();
		}
	} // end of inner class TransformActivity

	// ------------------------------------------------------------------------
	// Paint Listeners

	/**
	 * Add a PaintListener to this Display to receive notifications about paint
	 * events.
	 *
	 * @param pl
	 *            the {@link prefuse.util.display.PaintListener} to add
	 */
	public void addPaintListener(final PaintListener pl) {
		if (this.m_painters == null) {
			this.m_painters = new CopyOnWriteArrayList();
		}
		this.m_painters.add(pl);
	}

	/**
	 * Remove a PaintListener from this Display.
	 *
	 * @param pl
	 *            the {@link prefuse.util.display.PaintListener} to remove
	 */
	public void removePaintListener(final PaintListener pl) {
		this.m_painters.remove(pl);
	}

	/**
	 * Fires a pre-paint notification to PaintListeners.
	 *
	 * @param g
	 *            the current graphics context
	 */
	protected void firePrePaint(final Graphics2D g) {
		if ((this.m_painters != null) && (this.m_painters.size() > 0)) {
			final Object[] lstnrs = this.m_painters.getArray();
			for (int i = 0; i < lstnrs.length; ++i) {
				try {
					((PaintListener) lstnrs[i]).prePaint(this, g);
				} catch (final Exception e) {
					s_logger.warning("Exception thrown by PaintListener: " + e + "\n" + StringLib.getStackTrace(e));
				}
			}
		}
	}

	/**
	 * Fires a post-paint notification to PaintListeners.
	 *
	 * @param g
	 *            the current graphics context
	 */
	protected void firePostPaint(final Graphics2D g) {
		if ((this.m_painters != null) && (this.m_painters.size() > 0)) {
			final Object[] lstnrs = this.m_painters.getArray();
			for (int i = 0; i < lstnrs.length; ++i) {
				try {
					((PaintListener) lstnrs[i]).postPaint(this, g);
				} catch (final Exception e) {
					s_logger.warning("Exception thrown by PaintListener: " + e + "\n" + StringLib.getStackTrace(e));
				}
			}
		}
	}

	// ------------------------------------------------------------------------
	// Item Bounds Listeners

	/**
	 * Add an ItemBoundsListener to receive notifications when the bounds
	 * occupied by the VisualItems in this Display change.
	 *
	 * @param ibl
	 *            the {@link prefuse.util.display.ItemBoundsListener} to add
	 */
	public void addItemBoundsListener(final ItemBoundsListener ibl) {
		if (this.m_bounders == null) {
			this.m_bounders = new CopyOnWriteArrayList();
		}
		this.m_bounders.add(ibl);
	}

	/**
	 * Remove an ItemBoundsListener to receive notifications when the bounds
	 * occupied by the VisualItems in this Display change.
	 *
	 * @param ibl
	 *            the {@link prefuse.util.display.ItemBoundsListener} to remove
	 */
	public void removeItemBoundsListener(final ItemBoundsListener ibl) {
		this.m_bounders.remove(ibl);
	}

	/**
	 * Check if the item bounds has changed, and if so, fire a notification.
	 *
	 * @param prev
	 *            the previous item bounds of the Display
	 */
	protected void checkItemBoundsChanged(final Rectangle2D prev) {
		if (this.m_bounds.equals(prev)) {
			return; // nothing to do
		}

		if ((this.m_bounders != null) && (this.m_bounders.size() > 0)) {
			final Object[] lstnrs = this.m_bounders.getArray();
			for (int i = 0; i < lstnrs.length; ++i) {
				try {
					((ItemBoundsListener) lstnrs[i]).itemBoundsChanged(this);
				} catch (final Exception e) {
					s_logger.warning(
							"Exception thrown by ItemBoundsListener: " + e + "\n" + StringLib.getStackTrace(e));
				}
			}
		}
	}

	// ------------------------------------------------------------------------
	// Control Listeners

	/**
	 * Adds a ControlListener to receive all input events on VisualItems.
	 *
	 * @param cl
	 *            the listener to add.
	 */
	public void addControlListener(final Control cl) {
		this.m_controls.add(cl);
	}

	/**
	 * Removes a registered ControlListener.
	 *
	 * @param cl
	 *            the listener to remove.
	 */
	public void removeControlListener(final Control cl) {
		this.m_controls.remove(cl);
	}

	/**
	 * Returns the VisualItem located at the given point.
	 *
	 * @param p
	 *            the Point at which to look
	 * @return the VisualItem located at the given point, if any
	 */
	public synchronized VisualItem findItem(final Point p) {
		// transform mouse point from screen space to item space
		final Point2D p2 = (this.m_itransform == null ? p : this.m_itransform.transform(p, this.m_tmpPoint));
		// ensure that the picking queue has been z-sorted
		if (!this.m_queue.psorted) {
			this.m_queue.sortPickingQueue();
		}
		// walk queue from front to back looking for hits
		for (int i = this.m_queue.psize; --i >= 0;) {
			final VisualItem vi = this.m_queue.pitems[i];
			if (!vi.isValid()) {
				continue; // in case tuple went invalid
			}
			final Renderer r = vi.getRenderer();
			if ((r != null) && vi.isInteractive() && r.locatePoint(p2, vi)) {
				return vi;
			}
		}
		return null;
	}

	/**
	 * Captures all mouse and key events on the display, detects relevant
	 * VisualItems, and informs ControlListeners.
	 */
	public class InputEventCapturer {
		private VisualItem activeItem = null;
		private boolean mouseDown = false;

		private boolean validityCheck() {
			if (this.activeItem.isValid()) {
				return true;
			}
			this.activeItem = null;
			return false;
		}

		public void mouseDragged(final MouseEvent e) {
			synchronized (Display.this.m_vis) {
				if (this.activeItem != null) {
					if (this.validityCheck()) {
						this.fireItemDragged(this.activeItem, e);
					}
				} else {
					this.fireMouseDragged(e);
				}
			}
		}

		public void mouseMoved(final MouseEvent e) {
			synchronized (Display.this.m_vis) {
				boolean earlyReturn = false;
				// check if we've gone over any item
				final VisualItem vi = Display.this.findItem(e.getPoint());
				if ((this.activeItem != null) && (this.activeItem != vi)) {
					if (this.validityCheck()) {
						this.fireItemExited(this.activeItem, e);
					}
					earlyReturn = true;
				}
				if ((vi != null) && (vi != this.activeItem)) {
					this.fireItemEntered(vi, e);
					earlyReturn = true;
				}
				this.activeItem = vi;
				if (earlyReturn) {
					return;
				}

				if ((vi != null) && (vi == this.activeItem)) {
					this.fireItemMoved(vi, e);
				}
				if (vi == null) {
					this.fireMouseMoved(e);
				}
			}
		}

		public void mouseWheelMoved(final ScrollEvent e) {
			synchronized (Display.this.m_vis) {
				if (this.activeItem != null) {
					if (this.validityCheck()) {
						this.fireItemWheelMoved(this.activeItem, e);
					}
				} else {
					this.fireMouseWheelMoved(e);
				}
			}
		}

		public void mouseClicked(final MouseEvent e) {
			synchronized (Display.this.m_vis) {
				if (this.activeItem != null) {
					if (this.validityCheck()) {
						this.fireItemClicked(this.activeItem, e);
					}
				} else {
					this.fireMouseClicked(e);
				}
			}
		}

		public void mousePressed(final MouseEvent e) {
			synchronized (Display.this.m_vis) {
				this.mouseDown = true;
				if (this.activeItem != null) {
					if (this.validityCheck()) {
						this.fireItemPressed(this.activeItem, e);
					}
				} else {
					this.fireMousePressed(e);
				}
			}
		}

		public void mouseReleased(final MouseEvent e) {
			synchronized (Display.this.m_vis) {
				if (this.activeItem != null) {
					if (this.validityCheck()) {
						this.fireItemReleased(this.activeItem, e);
					}
				} else {
					this.fireMouseReleased(e);
				}
				if ((this.activeItem != null) && this.mouseDown && this.isOffComponent(e)) {
					// mouse was dragged off of the component,
					// then released, so register an exit
					this.fireItemExited(this.activeItem, e);
					this.activeItem = null;
				}
				this.mouseDown = false;
			}
		}

		public void mouseEntered(final MouseEvent e) {
			synchronized (Display.this.m_vis) {
				this.fireMouseEntered(e);
			}
		}

		public void mouseExited(final MouseEvent e) {
			synchronized (Display.this.m_vis) {
				if (!this.mouseDown && (this.activeItem != null)) {
					// we've left the component and an item
					// is active but not being dragged, deactivate it
					this.fireItemExited(this.activeItem, e);
					this.activeItem = null;
				}
				this.fireMouseExited(e);
			}
		}

		public void keyPressed(final KeyEvent e) {
			synchronized (Display.this.m_vis) {
				if (this.activeItem != null) {
					if (this.validityCheck()) {
						this.fireItemKeyPressed(this.activeItem, e);
					}
				} else {
					this.fireKeyPressed(e);
				}
			}
		}

		public void keyReleased(final KeyEvent e) {
			synchronized (Display.this.m_vis) {
				if (this.activeItem != null) {
					if (this.validityCheck()) {
						this.fireItemKeyReleased(this.activeItem, e);
					}
				} else {
					this.fireKeyReleased(e);
				}
			}
		}

		public void keyTyped(final KeyEvent e) {
			synchronized (Display.this.m_vis) {
				if (this.activeItem != null) {
					if (this.validityCheck()) {
						this.fireItemKeyTyped(this.activeItem, e);
					}
				} else {
					this.fireKeyTyped(e);
				}
			}
		}

		private boolean isOffComponent(final MouseEvent e) {
			final double x = e.getX(), y = e.getY();
			return ((x < 0) || (x > Display.this.getWidth()) || (y < 0) || (y > Display.this.getHeight()));
		}

		// --------------------------------------------------------------------
		// Fire Event Notifications

		private void fireItemDragged(final VisualItem item, final MouseEvent e) {
			final Object[] lstnrs = Display.this.m_controls.getArray();
			for (int i = 0; i < lstnrs.length; ++i) {
				final Control ctrl = (Control) lstnrs[i];
				if (ctrl.isEnabled()) {
					try {
						ctrl.itemDragged(item, e);
					} catch (final Exception ex) {
						s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
					}
				}
			}
		}

		private void fireItemMoved(final VisualItem item, final MouseEvent e) {
			final Object[] lstnrs = Display.this.m_controls.getArray();
			for (int i = 0; i < lstnrs.length; ++i) {
				final Control ctrl = (Control) lstnrs[i];
				if (ctrl.isEnabled()) {
					try {
						ctrl.itemMoved(item, e);
					} catch (final Exception ex) {
						s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
					}
				}
			}
		}

		private void fireItemWheelMoved(final VisualItem item, final ScrollEvent e) {
			final Object[] lstnrs = Display.this.m_controls.getArray();
			for (int i = 0; i < lstnrs.length; ++i) {
				final Control ctrl = (Control) lstnrs[i];
				if (ctrl.isEnabled()) {
					try {
						ctrl.itemWheelMoved(item, e);
					} catch (final Exception ex) {
						s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
					}
				}
			}
		}

		private void fireItemClicked(final VisualItem item, final MouseEvent e) {
			final Object[] lstnrs = Display.this.m_controls.getArray();
			for (int i = 0; i < lstnrs.length; ++i) {
				final Control ctrl = (Control) lstnrs[i];
				if (ctrl.isEnabled()) {
					try {
						ctrl.itemClicked(item, e);
					} catch (final Exception ex) {
						s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
					}
				}
			}
		}

		private void fireItemPressed(final VisualItem item, final MouseEvent e) {
			final Object[] lstnrs = Display.this.m_controls.getArray();
			for (int i = 0; i < lstnrs.length; ++i) {
				final Control ctrl = (Control) lstnrs[i];
				if (ctrl.isEnabled()) {
					try {
						ctrl.itemPressed(item, e);
					} catch (final Exception ex) {
						s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
					}
				}
			}
		}

		private void fireItemReleased(final VisualItem item, final MouseEvent e) {
			final Object[] lstnrs = Display.this.m_controls.getArray();
			for (int i = 0; i < lstnrs.length; ++i) {
				final Control ctrl = (Control) lstnrs[i];
				if (ctrl.isEnabled()) {
					try {
						ctrl.itemReleased(item, e);
					} catch (final Exception ex) {
						s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
					}
				}
			}
		}

		private void fireItemEntered(final VisualItem item, final MouseEvent e) {
			item.setHover(true);
			final Object[] lstnrs = Display.this.m_controls.getArray();
			for (int i = 0; i < lstnrs.length; ++i) {
				final Control ctrl = (Control) lstnrs[i];
				if (ctrl.isEnabled()) {
					try {
						ctrl.itemEntered(item, e);
					} catch (final Exception ex) {
						s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
					}
				}
			}
		}

		private void fireItemExited(final VisualItem item, final MouseEvent e) {
			if (item.isValid()) {
				item.setHover(false);
			}
			final Object[] lstnrs = Display.this.m_controls.getArray();
			for (int i = 0; i < lstnrs.length; ++i) {
				final Control ctrl = (Control) lstnrs[i];
				if (ctrl.isEnabled()) {
					try {
						ctrl.itemExited(item, e);
					} catch (final Exception ex) {
						s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
					}
				}
			}
		}

		private void fireItemKeyPressed(final VisualItem item, final KeyEvent e) {
			final Object[] lstnrs = Display.this.m_controls.getArray();
			if (lstnrs.length == 0) {
				return;
			}
			for (int i = 0; i < lstnrs.length; ++i) {
				final Control ctrl = (Control) lstnrs[i];
				if (ctrl.isEnabled()) {
					try {
						ctrl.itemKeyPressed(item, e);
					} catch (final Exception ex) {
						s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
					}
				}
			}
		}

		private void fireItemKeyReleased(final VisualItem item, final KeyEvent e) {
			final Object[] lstnrs = Display.this.m_controls.getArray();
			for (int i = 0; i < lstnrs.length; ++i) {
				final Control ctrl = (Control) lstnrs[i];
				if (ctrl.isEnabled()) {
					try {
						ctrl.itemKeyReleased(item, e);
					} catch (final Exception ex) {
						s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
					}
				}
			}
		}

		private void fireItemKeyTyped(final VisualItem item, final KeyEvent e) {
			final Object[] lstnrs = Display.this.m_controls.getArray();
			for (int i = 0; i < lstnrs.length; ++i) {
				final Control ctrl = (Control) lstnrs[i];
				if (ctrl.isEnabled()) {
					try {
						ctrl.itemKeyTyped(item, e);
					} catch (final Exception ex) {
						s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
					}
				}
			}
		}

		private void fireMouseEntered(final MouseEvent e) {
			final Object[] lstnrs = Display.this.m_controls.getArray();
			for (int i = 0; i < lstnrs.length; ++i) {
				final Control ctrl = (Control) lstnrs[i];
				if (ctrl.isEnabled()) {
					try {
						ctrl.mouseEntered(e);
					} catch (final Exception ex) {
						s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
					}
				}
			}
		}

		private void fireMouseExited(final MouseEvent e) {
			final Object[] lstnrs = Display.this.m_controls.getArray();
			for (int i = 0; i < lstnrs.length; ++i) {
				final Control ctrl = (Control) lstnrs[i];
				if (ctrl.isEnabled()) {
					try {
						ctrl.mouseExited(e);
					} catch (final Exception ex) {
						s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
					}
				}
			}
		}

		private void fireMousePressed(final MouseEvent e) {
			final Object[] lstnrs = Display.this.m_controls.getArray();
			for (int i = 0; i < lstnrs.length; ++i) {
				final Control ctrl = (Control) lstnrs[i];
				if (ctrl.isEnabled()) {
					try {
						ctrl.mousePressed(e);
					} catch (final Exception ex) {
						s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
					}
				}
			}
		}

		private void fireMouseReleased(final MouseEvent e) {
			final Object[] lstnrs = Display.this.m_controls.getArray();
			for (int i = 0; i < lstnrs.length; ++i) {
				final Control ctrl = (Control) lstnrs[i];
				if (ctrl.isEnabled()) {
					try {
						ctrl.mouseReleased(e);
					} catch (final Exception ex) {
						s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
					}
				}
			}
		}

		private void fireMouseClicked(final MouseEvent e) {
			final Object[] lstnrs = Display.this.m_controls.getArray();
			for (int i = 0; i < lstnrs.length; ++i) {
				final Control ctrl = (Control) lstnrs[i];
				if (ctrl.isEnabled()) {
					try {
						ctrl.mouseClicked(e);
					} catch (final Exception ex) {
						s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
					}
				}
			}
		}

		private void fireMouseDragged(final MouseEvent e) {
			final Object[] lstnrs = Display.this.m_controls.getArray();
			for (int i = 0; i < lstnrs.length; ++i) {
				final Control ctrl = (Control) lstnrs[i];
				if (ctrl.isEnabled()) {
					try {
						ctrl.mouseDragged(e);
					} catch (final Exception ex) {
						s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
					}
				}
			}
		}

		private void fireMouseMoved(final MouseEvent e) {
			final Object[] lstnrs = Display.this.m_controls.getArray();
			for (int i = 0; i < lstnrs.length; ++i) {
				final Control ctrl = (Control) lstnrs[i];
				if (ctrl.isEnabled()) {
					try {
						ctrl.mouseMoved(e);
					} catch (final Exception ex) {
						s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
					}
				}
			}
		}

		private void fireMouseWheelMoved(final ScrollEvent e) {
			final Object[] lstnrs = Display.this.m_controls.getArray();
			for (int i = 0; i < lstnrs.length; ++i) {
				final Control ctrl = (Control) lstnrs[i];
				if (ctrl.isEnabled()) {
					try {
						ctrl.mouseWheelMoved(e);
					} catch (final Exception ex) {
						s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
					}
				}
			}
		}

		private void fireKeyPressed(final KeyEvent e) {
			final Object[] lstnrs = Display.this.m_controls.getArray();
			for (int i = 0; i < lstnrs.length; ++i) {
				final Control ctrl = (Control) lstnrs[i];
				if (ctrl.isEnabled()) {
					try {
						ctrl.keyPressed(e);
					} catch (final Exception ex) {
						s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
					}
				}
			}
		}

		private void fireKeyReleased(final KeyEvent e) {
			final Object[] lstnrs = Display.this.m_controls.getArray();
			for (int i = 0; i < lstnrs.length; ++i) {
				final Control ctrl = (Control) lstnrs[i];
				if (ctrl.isEnabled()) {
					try {
						ctrl.keyReleased(e);
					} catch (final Exception ex) {
						s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
					}
				}
			}
		}

		private void fireKeyTyped(final KeyEvent e) {
			final Object[] lstnrs = Display.this.m_controls.getArray();
			for (int i = 0; i < lstnrs.length; ++i) {
				final Control ctrl = (Control) lstnrs[i];
				if (ctrl.isEnabled()) {
					try {
						ctrl.keyTyped(e);
					} catch (final Exception ex) {
						s_logger.warning("Exception thrown by Control: " + ex + "\n" + StringLib.getStackTrace(ex));
					}
				}
			}
		}

	} // end of inner class MouseEventCapturer

	// ------------------------------------------------------------------------
	// Text Editing

	/**
	 * Returns the TextComponent used for on-screen text editing.
	 *
	 * @return the TextComponent used for text editing
	 */
	public TextField getTextEditor() {
		return this.m_editor;
	}

	/**
	 * Sets the TextComponent used for on-screen text editing.
	 *
	 * @param tc
	 *            the TextComponent to use for text editing
	 */
	public void setTextEditor(final TextField tc) {
		this.remove(this.m_editor);
		this.m_editor = tc;
		this.add(this.m_editor, 1);
	}

	/**
	 * Edit text for the given VisualItem and attribute. Presents a text editing
	 * widget spaning the item's bounding box. Use stopEditing() to hide the
	 * text widget. When stopEditing() is called, the data field will
	 * automatically be updated with the VisualItem.
	 *
	 * @param item
	 *            the VisualItem to edit
	 * @param attribute
	 *            the attribute to edit
	 */
	public void editText(final VisualItem item, final String attribute) {
		if (this.m_editing) {
			this.stopEditing();
		}
		final Rectangle2D b = item.getBounds();
		final Rectangle r = this.m_transform.createTransformedShape(b).getBounds();

		// hacky placement code that attempts to keep text in same place
		// configured under Windows XP and Java 1.4.2b
		if (this.m_editor instanceof JTextArea) {
			r.y -= 2;
			r.width += 22;
			r.height += 2;
		} else {
			r.x += 3;
			r.y += 1;
			r.width -= 5;
			r.height -= 2;
		}

		final Font f = getFont();
		final int size = (int) Math.round(f.getSize() * this.m_transform.getScaleX());
		final Font nf = new Font(f.getFontName(), f.getStyle(), size);
		this.m_editor.setFont(nf);

		this.editText(item, attribute, r);
	}

	/**
	 * Edit text for the given VisualItem and field. Presents a text editing
	 * widget spaning the given bounding box. Use stopEditing() to hide the text
	 * widget. When stopEditing() is called, the field will automatically be
	 * updated with the VisualItem.
	 *
	 * @param item
	 *            the VisualItem to edit
	 * @param attribute
	 *            the attribute to edit
	 * @param r
	 *            Rectangle representing the desired bounding box of the text
	 *            editing widget
	 */
	public void editText(final VisualItem item, final String attribute, final Rectangle r) {
		if (this.m_editing) {
			this.stopEditing();
		}
		final String txt = item.getString(attribute);
		this.m_editItem = item;
		this.m_editAttribute = attribute;
		final Color tc = ColorLib.getColor(item.getTextColor());
		final Color fc = ColorLib.getColor(item.getFillColor());
		this.m_editor.setForeground(tc);
		this.m_editor.setBackground(fc);
		this.editText(txt, r);
	}

	/**
	 * Show a text editing widget containing the given text and spanning the
	 * specified bounding box. Use stopEditing() to hide the text widget. Use
	 * the method calls getTextEditor().getText() to get the resulting edited
	 * text.
	 *
	 * @param txt
	 *            the text string to display in the text widget
	 * @param r
	 *            Rectangle representing the desired bounding box of the text
	 *            editing widget
	 */
	public void editText(final String txt, final Rectangle r) {
		if (this.m_editing) {
			this.stopEditing();
		}
		this.m_editing = true;
		this.m_editor.setBounds(r.x, r.y, r.width, r.height);
		this.m_editor.setText(txt);
		this.m_editor.setVisible(true);
		this.m_editor.setCaretPosition(txt.length());
		this.m_editor.requestFocus();
	}

	/**
	 * Stops text editing on the display, hiding the text editing widget. If the
	 * text editor was associated with a specific VisualItem (ie one of the
	 * editText() methods which include a VisualItem as an argument was called),
	 * the item is updated with the edited text.
	 */
	public void stopEditing() {
		this.m_editor.setVisible(false);
		if (this.m_editItem != null) {
			final String txt = this.m_editor.getText();
			this.m_editItem.set(this.m_editAttribute, txt);
			this.m_editItem = null;
			this.m_editAttribute = null;
			this.m_editor.setBackground(null);
			this.m_editor.setForeground(null);
		}
		this.m_editing = false;
	}

} // end of class Display
