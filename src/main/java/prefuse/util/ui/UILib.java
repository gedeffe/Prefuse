package prefuse.util.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.UIManager;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Labeled;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Library routines for user interface tasks.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class UILib {

    // private static Image s_appIcon;

    /**
     * Not instantiable.
     */
    private UILib() {
        // prevent instantiation
    }

    // public static synchronized Image getApplicationIcon() {
    // if ( s_appIcon == null ) {
    // try {
    // s_appIcon = new ImageIcon(
    // UILib.class.getResource("icon.gif")).getImage();
    // } catch ( Exception e ) {
    // e.printStackTrace();
    // }
    // }
    // return s_appIcon;
    // }

    /**
     * Indicates if a given mouse button is being pressed.
     *
     * @param e
     *            the InputEvent to check
     * @param button
     *            the mouse button to look for
     * @return true if the button is being pressed, false otherwise
     * @see prefuse.controls.Control
     */
    public static boolean isButtonPressed(final MouseEvent e, final MouseButton button) {
        return e.getButton() == button;
    }

    /**
     * Set the look and feel of Java Swing user interface components to match
     * that of the platform (Windows, Mac, Linux, etc) on which it is currently
     * running.
     */
    public static final void setPlatformLookAndFeel() {
        try {
            final String laf = UIManager.getSystemLookAndFeelClassName();
            UIManager.setLookAndFeel(laf);
        } catch (final Exception e) {
        }
    }

    /**
     * Convenience method for creating a Box user interface widget container.
     *
     * @param c
     *            an array of components to include in the box
     * @param horiz
     *            indicated is the box should be horizontal (true) or vertical
     *            (false)
     * @param margin
     *            the margins, in pixels, to use on the sides of the box
     * @param spacing
     *            the minimum spacing, in pixels, to use between components
     * @return a new Box instance with the given properties.
     * @see javax.swing.Box
     */
    public static Box getBox(final Component[] c, final boolean horiz, final int margin, final int spacing) {
        return UILib.getBox(c, horiz, margin, margin, spacing);
    }

    /**
     * Convenience method for creating a Box user interface widget container.
     *
     * @param c
     *            an array of components to include in the box
     * @param horiz
     *            indicated is the box should be horizontal (true) or vertical
     *            (false)
     * @param margin1
     *            the margin, in pixels, for the left or top side
     * @param margin2
     *            the margin, in pixels, for the right or bottom side
     * @param spacing
     *            the minimum spacing, in pixels, to use between components
     * @return a new Box instance with the given properties.
     * @see javax.swing.Box
     */
    public static Box getBox(final Component[] c, final boolean horiz, final int margin1, final int margin2,
            final int spacing) {
        final Box b = new Box(horiz ? BoxLayout.X_AXIS : BoxLayout.Y_AXIS);
        UILib.addStrut(b, horiz, margin1);
        for (int i = 0; i < c.length; ++i) {
            if (i > 0) {
                UILib.addStrut(b, horiz, spacing);
                UILib.addGlue(b, horiz);
            }
            b.add(c[i]);
        }
        UILib.addStrut(b, horiz, margin2);
        return b;
    }

    /**
     * Add a strut, or rigid spacing, to a UI component
     *
     * @param b
     *            the component to add the strut to, should be either a Box or a
     *            Container using a BoxLayout.
     * @param horiz
     *            indicates if the strust should horizontal (true) or vertical
     *            (false)
     * @param size
     *            the length, in pixels, of the strut
     */
    public static void addStrut(final JComponent b, final boolean horiz, final int size) {
        if (size < 1) {
            return;
        }
        b.add(horiz ? Box.createHorizontalStrut(size) : Box.createVerticalStrut(size));
    }

    /**
     * Add a glue, or variable spacing, to a UI component
     *
     * @param b
     *            the component to add the glue to, should be either a Box or a
     *            Container using a BoxLayout.
     * @param horiz
     *            indicates if the glue should horizontal (true) or vertical
     *            (false)
     */
    public static void addGlue(final JComponent b, final boolean horiz) {
        b.add(horiz ? Box.createHorizontalGlue() : Box.createVerticalGlue());
    }

    /**
     * Add a strut, or rigid spacing, to a UI component
     *
     * @param b
     *            the component to add the strut to, should be either a Box or a
     *            Container using a BoxLayout.
     * @param layout
     *            the desired layout orientation of the strut. One of
     *            {@link javax.swing.BoxLayout#X_AXIS},
     *            {@link javax.swing.BoxLayout#Y_AXIS},
     *            {@link javax.swing.BoxLayout#LINE_AXIS}, or
     *            {@link javax.swing.BoxLayout#PAGE_AXIS}.
     * @param size
     *            the length, in pixels, of the strut
     */
    public static void addStrut(final JComponent b, final int layout, final int size) {
        if (size < 1) {
            return;
        }
        b.add(UILib.getAxis(b, layout) == BoxLayout.X_AXIS ? Box.createHorizontalStrut(size) : Box.createVerticalStrut(size));
    }

    /**
     * Add a glue, or variable spacing, to a UI component
     *
     * @param b
     *            the component to add the glue to, should be either a Box or a
     *            Container using a BoxLayout.
     * @param layout
     *            the desired layout orientation of the glue. One of
     *            {@link javax.swing.BoxLayout#X_AXIS},
     *            {@link javax.swing.BoxLayout#Y_AXIS},
     *            {@link javax.swing.BoxLayout#LINE_AXIS}, or
     *            {@link javax.swing.BoxLayout#PAGE_AXIS}.
     */
    public static void addGlue(final JComponent b, final int layout) {
        b.add(UILib.getAxis(b, layout) == BoxLayout.X_AXIS ? Box.createHorizontalGlue() : Box.createVerticalGlue());
    }

    /**
     * Resolve the axis type of a component, given a layout orientation
     *
     * @param c
     *            a Swing Component, should be either a Box or a Container using
     *            a BoxLayout.
     * @param layout
     *            the layout orientation of the component. One of
     *            {@link javax.swing.BoxLayout#X_AXIS},
     *            {@link javax.swing.BoxLayout#Y_AXIS},
     *            {@link javax.swing.BoxLayout#LINE_AXIS}, or
     *            {@link javax.swing.BoxLayout#PAGE_AXIS}.
     * @return one of {@link javax.swing.BoxLayout#X_AXIS}, or
     *         {@link javax.swing.BoxLayout#Y_AXIS},
     */
    public static int getAxis(final JComponent c, final int layout) {
        final ComponentOrientation o = c.getComponentOrientation();
        switch (layout) {
        case BoxLayout.LINE_AXIS:
            return o.isHorizontal() ? BoxLayout.X_AXIS : BoxLayout.Y_AXIS;
        case BoxLayout.PAGE_AXIS:
            return o.isHorizontal() ? BoxLayout.Y_AXIS : BoxLayout.X_AXIS;
        default:
            return layout;
        }
    }

    /**
     * Sets the foreground and background color for a component and all
     * components contained within it.
     *
     * @param c
     *            the parent component of the component subtree to set
     * @param back
     *            the background color to set
     * @param fore
     *            the foreground color to set
     */
    public static void setColor(final Component c, final Color back, final Color fore) {
        c.setBackground(back);
        c.setForeground(fore);
        if (c instanceof Container) {
            final Container con = (Container) c;
            for (int i = 0; i < con.getComponentCount(); ++i) {
                UILib.setColor(con.getComponent(i), back, fore);
            }
        }
    }

    /**
     * Sets the font for a component and all components contained within it.
     * <br>
     * We are able to set font for {@link Text}, {@link Labeled}
     *
     * @param c
     *            the parent component of the component subtree to set
     * @param font
     *            the font to set
     */
    public static void setFont(final Node c, final Font font) {
        if (c instanceof Text) {
            ((Text) c).setFont(font);
        } else if (c instanceof Labeled) {
            ((Labeled) c).setFont(font);
        } else if (c instanceof Parent) {
            final Parent con = (Parent) c;
            for (final Node node : con.getChildrenUnmodifiable()) {
                UILib.setFont(node, font);
            }
        }
    }

} // end of class UILib
