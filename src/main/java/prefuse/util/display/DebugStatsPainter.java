package prefuse.util.display;


import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import prefuse.Display;
import prefuse.util.PrefuseLib;

/**
 * PinatListener that paints useful debugging statistics over a prefuse
 * display. This includes the current frame rate, the number of visible
 * items, memory usage, and display navigation information.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class DebugStatsPainter implements PaintListener {

    /**
     * Does nothing.
     * @see prefuse.util.display.PaintListener#prePaint(prefuse.Display, javafx.scene.canvas.GraphicsContext)
     */
    @Override
    public void prePaint(final Display d, final GraphicsContext g) {

    }

    /**
     * Prints a debugging statistics string in the Display.
     * @see prefuse.util.display.PaintListener#postPaint(prefuse.Display, javafx.scene.canvas.GraphicsContext)
     */
    @Override
    public void postPaint(final Display d, final GraphicsContext g) {
        // TODO use default font
        // g.setFont(d.getFont());
        // TODO use default color
        g.setFill(Color.BLACK);
        g.fillText(PrefuseLib.getDisplayStats(d), 5, 15);
    }

} // end of class DebugStatsPainter
