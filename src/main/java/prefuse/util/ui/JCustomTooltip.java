package prefuse.util.ui;

import java.awt.Point;
import java.awt.event.MouseAdapter;

import javax.swing.JToolTip;
import javax.swing.Popup;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;

/**
 * Tooltip component that allows arbitrary Swing components to be
 * used within tooltips. To use this class, provide the constructor
 * with both the source component (the component to provide the
 * tooltip for) and the tooltip component (a JComponent to use as the
 * displayed tooltip). This class can be used to provide
 * a custom tooltip for a prefuse {@link prefuse.Display} instance,
 * by registering it with the
 * {@link prefuse.Display#setCustomToolTip(JToolTip)} method.
 *
 * <p>In general, <code>JCustomTooltip</code> can be used with any Swing
 * widget. This is done by  overriding JComponent's <code>createToolTip</code>
 * method such that it returns the custom tooltip instance.</p>
 *
 * <p>Before using this class, you might first check if you can
 * achieve your desired custom tooltip by using HTML formatting.
 * As with JLabel instances, the standard Swing tooltip mechanism includes
 * support for HTML tooltip text, allowing multi-line tooltips using
 * coloring and various fonts to be created. See
 * See <a href="http://examples.oreilly.com/jswing2/code/ch04/HtmlLabel.java">
 * this example</a> for an instance of using HTML formatting in
 * a JLabel. The same HTML string could be used as the input to
 * JComponent's <code>setToolTipText</code> method.</p>
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class JCustomTooltip extends Tooltip {

    private boolean  m_persist = false;
    private final Listener m_lstnr = null;

    /**
     * Create a new JCustomTooltip
     * @param src the component for which this is a tooltip
     * @param content the component to use as the tooltip content
     */
    public JCustomTooltip(final Node src, final Node content) {
        this(src, content, false);
    }

    /**
     * Create a new JCustomTooltip
     * @param src the component for which this is a tooltip
     * @param content the component to use as the tooltip content
     * @param inter indicates if the tooltip should be interactive
     */
    public JCustomTooltip(final Node src, final Node content, final boolean inter)
    {
        this.setGraphic(content);

        this.setPersistent(inter);
        Tooltip.install(src, this);
    }

    /**
     * Indicates if the tooltip will stay persistent on the screen to
     * support interaction within the tooltip component.
     * @return true if persistent, false otherwise.
     */
    public boolean isPersistent() {
        return this.m_persist;
    }

    /**
     * Sets if the tooltip will stay persistent on the screen to
     * support interaction within the tooltip component.
     * @param inter true for persistence, false otherwise.
     */
    public void setPersistent(final boolean inter) {
        if ( inter == this.m_persist ) {
            return;
        }

        if ( inter ) {
            // TODO add a mechanism to transform the tooltip into a popup dialog
            // with current content.

            // this.m_lstnr = new Listener();
            // this.addAncestorListener(this.m_lstnr);
        } else {
            // this.removeAncestorListener(this.m_lstnr);
            // this.m_lstnr = null;
        }
        this.m_persist = inter;
    }

    /**
     * Set the content component of the tooltip
     * @param content the tooltip content
     */
    public void setContent(final Node content) {
        this.setGraphic(content);
    }



    /**
     * Listener class that registers the tooltip component and performs
     * persistence management.
     */
    private class Listener extends MouseAdapter implements AncestorListener {
        private final Point point = new Point();
        private final boolean showing = false;
        private Popup popup;

        @Override
        public void ancestorAdded(final AncestorEvent event) {
            // if ( this.showing ) { return; }
            //
            // final Window ttip =
            // SwingUtilities.getWindowAncestor(getParent());
            // if ( (ttip == null) || !ttip.isVisible() ) {
            // return;
            // }
            // //ttip.addMouseListener(this);
            // ttip.getLocation(this.point);
            // ttip.setVisible(false);
            // getParent().remove(JCustomTooltip.this);
            //
            // final JComponent c = getComponent();
            // c.setToolTipText(null);
            // c.removeMouseMotionListener(ToolTipManager.sharedInstance());
            //
            // this.popup = PopupFactory.getSharedInstance().getPopup(
            // c, JCustomTooltip.this, this.point.x, this.point.y);
            // final Window w =
            // SwingUtilities.getWindowAncestor(JCustomTooltip.this);
            // w.addMouseListener(this);
            // w.setFocusableWindowState(true);
            // this.popup.show();
            //
            // this.showing = true;
        }

        public void mouseEntered(final MouseEvent e) {
            //            Window ttip = SwingUtilities.getWindowAncestor(getParent());
            //            ttip.removeMouseListener(this);
            //            if ( ttip == null || !ttip.isVisible() ) {
            //                return;
            //            }
            //            ttip.getLocation(point);
            //            ttip.hide();
            //            getParent().remove(JCustomTooltip.this);
            //
            //            JComponent c = getComponent();
            //            c.setToolTipText(null);
            //            c.removeMouseMotionListener(ToolTipManager.sharedInstance());
            //
            //            popup = PopupFactory.getSharedInstance().getPopup(
            //                    c, JCustomTooltip.this, point.x, point.y);
            //            Window w = SwingUtilities.getWindowAncestor(JCustomTooltip.this);
            //            w.addMouseListener(this);
            //            w.setFocusableWindowState(true);
            //            popup.show();
            //
            //            showing = true;
        }

        public void mouseExited(final MouseEvent e) {
            // if ( !this.showing ) {
            // return;
            // }
            // final int x = e.getX(), y = e.getY();
            // final Component c = (Component)e.getSource();
            // if ( (x < 0) || (y < 0) || (x > c.getWidth()) || (y >
            // c.getHeight()) )
            // {
            // final Window w =
            // SwingUtilities.getWindowAncestor(JCustomTooltip.this);
            // w.removeMouseListener(this);
            // w.setFocusableWindowState(false);
            // this.popup.hide();
            // this.popup = null;
            // getComponent().setToolTipText("?");
            // this.showing = false;
            // }
        }

        @Override
        public void ancestorMoved(final AncestorEvent event) {
        }
        @Override
        public void ancestorRemoved(final AncestorEvent event) {
        }
    }

} // end of class JCustomTooltip
