package prefuse.controls;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import prefuse.Display;
import prefuse.activity.Activity;
import prefuse.activity.SlowInSlowOutPacer;

/**
 * <p>
 * Allows users to pan over a display such that the display zooms in and out
 * proportionally to how fast the pan is performed.
 * </p>
 *
 * <p>
 * The algorithm used is that of Takeo Igarishi and Ken Hinckley in their
 * research paper
 * <a href="http://citeseer.ist.psu.edu/igarashi00speeddependent.html"> Speed-
 * dependent Automatic Zooming for Browsing Large Documents</a>, UIST 2000.
 * </p>
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class ZoomingPanControl extends ControlAdapter {

	private boolean repaint = true, started = false;

	private Point2D mouseDown, mouseCur, mouseUp;
	double dx;

	private double dy;
	private double d = 0;

	private final double v0 = 75.0, d0 = 50, d1 = 400, s0 = .1;

	private final UpdateActivity update = new UpdateActivity();
	private final FinishActivity finish = new FinishActivity();

	/**
	 * Create a new ZoomingPanControl.
	 */
	public ZoomingPanControl() {
		this(true);
	}

	/**
	 * Create a new ZoomingPanControl.
	 *
	 * @param repaint
	 *            true if repaint requests should be issued while panning and
	 *            zooming. false if repaint requests will come from elsewhere
	 *            (e.g., a continuously running action).
	 */
	public ZoomingPanControl(final boolean repaint) {
		this.repaint = repaint;
	}

	/**
	 * @see java.awt.event.MouseListener#mousePressed(javafx.scene.input.MouseEvent)
	 */
	@Override
	public void mousePressed(final MouseEvent e) {
		if (e.getButton() == LEFT_MOUSE_BUTTON) {
			final Display display = (Display) e.getSource();
			display.setCursor(Cursor.MOVE);
			this.mouseDown = new Point2D(e.getX(), e.getY());
		}
	}

	/**
	 * @see java.awt.event.MouseMotionListener#mouseDragged(javafx.scene.input.MouseEvent)
	 */
	@Override
	public void mouseDragged(final MouseEvent e) {
		if (e.getButton() == LEFT_MOUSE_BUTTON) {
			this.mouseCur = new Point2D(e.getX(), e.getY());
			this.dx = this.mouseCur.getX() - this.mouseDown.getX();
			this.dy = this.mouseCur.getY() - this.mouseDown.getY();
			this.d = Math.sqrt((this.dx * this.dx) + (this.dy * this.dy));

			if (!this.started) {
				final Display display = (Display) e.getSource();
				this.update.setDisplay(display);
				this.update.run();
			}
		}
	}

	/**
	 * @see java.awt.event.MouseListener#mouseReleased(javafx.scene.input.MouseEvent)
	 */
	@Override
	public void mouseReleased(final MouseEvent e) {
		if (e.getButton() == LEFT_MOUSE_BUTTON) {
			this.update.cancel();
			this.started = false;

			final Display display = (Display) e.getSource();
			this.mouseUp = new Point2D(e.getX(), e.getY());

			this.finish.setDisplay(display);
			this.finish.run();

			display.setCursor(Cursor.DEFAULT);
		}
	}

	private class UpdateActivity extends Activity {
		private Display display;
		private long lastTime = 0;

		public UpdateActivity() {
			super(-1, 15, 0);
		}

		public void setDisplay(final Display display) {
			this.display = display;
		}

		@Override
		protected void run(final long elapsedTime) {
			final double sx = this.display.getTransform().getMxx();
			double s, v;

			if (ZoomingPanControl.this.d <= ZoomingPanControl.this.d0) {
				s = 1.0;
				v = ZoomingPanControl.this.v0 * (ZoomingPanControl.this.d / ZoomingPanControl.this.d0);
			} else {
				s = (ZoomingPanControl.this.d >= ZoomingPanControl.this.d1 ? ZoomingPanControl.this.s0
						: Math.pow(ZoomingPanControl.this.s0, (ZoomingPanControl.this.d - ZoomingPanControl.this.d0)
								/ (ZoomingPanControl.this.d1 - ZoomingPanControl.this.d0)));
				v = ZoomingPanControl.this.v0;
			}

			s = s / sx;

			final double dd = (v * (elapsedTime - this.lastTime)) / 1000;
			this.lastTime = elapsedTime;
			final double deltaX = (-dd * ZoomingPanControl.this.dx) / ZoomingPanControl.this.d;
			final double deltaY = (-dd * ZoomingPanControl.this.dy) / ZoomingPanControl.this.d;

			this.display.pan(deltaX, deltaY);
			if (s != 1.0) {
				this.display.zoom(ZoomingPanControl.this.mouseCur, s);
			}

			if (ZoomingPanControl.this.repaint) {
				this.display.repaint();
			}
		}
	} // end of class UpdateActivity

	private class FinishActivity extends Activity {
		private Display display;
		private double scale;

		public FinishActivity() {
			super(1500, 15, 0);
			this.setPacingFunction(new SlowInSlowOutPacer());
		}

		public void setDisplay(final Display display) {
			this.display = display;
			this.scale = display.getTransform().getMxx();
			final double z = (this.scale < 1.0 ? 1 / this.scale : this.scale);
			this.setDuration((long) (500 + (500 * Math.log(1 + z))));
		}

		@Override
		protected void run(final long elapsedTime) {
			final double f = this.getPace(elapsedTime);
			final double s = this.display.getTransform().getMxx();
			final double z = (f + ((1 - f) * this.scale)) / s;
			this.display.zoom(ZoomingPanControl.this.mouseUp, z);
			if (ZoomingPanControl.this.repaint) {
				this.display.repaint();
			}
		}
	} // end of class FinishActivity

} // end of class ZoomingPanControl
