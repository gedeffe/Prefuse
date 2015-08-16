package prefuse.util.display;

import javafx.scene.canvas.GraphicsContext;

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
    public void prePaint(Display d, GraphicsContext g) {
        
    }
    
    /**
     * Prints a debugging statistics string in the Display.
     * @see prefuse.util.display.PaintListener#postPaint(prefuse.Display, javafx.scene.canvas.GraphicsContext)
     */
    public void postPaint(Display d, GraphicsContext g) {
        g.setFont(d.getFont());
        g.setColor(d.getForeground());
        g.drawString(PrefuseLib.getDisplayStats(d), 5, 15);
    }

} // end of class DebugStatsPainter
