package prefuse.action.assignment;

import java.util.logging.Logger;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import prefuse.action.EncoderAction;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.visual.VisualItem;

/**
 * In JavaFX, there is no more a Stroke class. It has been replaced by several
 * methods defined in Shape class. It corresponds to css attributes to define a
 * complete "stroke". It means color, dashing pattern, direction, width, ...
 *
 * So we might consider that this class should be completely adapted to this
 * behavior. And it will impact also VisualItem too (for instance, it might
 * extends directly Shape or just redefines corresponding methods).
 *
 * <p>
 * Assignment Action that assigns <code>Stroke</code> values to VisualItems. The
 * Stroke instance determines how lines and shape outlines are drawn, including
 * the base size of the line, the line endings and line join types, and whether
 * the line is solid or dashed. By default, a StrokeAction simply sets each
 * VisualItem to use a default 1-pixel wide solid line. Clients can change this
 * default value to achieve uniform Stroke assignment, or can add any number of
 * additional rules for Stroke assignment. Rules are specified by a Predicate
 * instance which, if returning true, will trigger that rule, causing either the
 * provided Stroke value or the result of a delegate StrokeAction to be applied.
 * Rules are evaluated in the order in which they are added to the StrokeAction,
 * so earlier rules will have precedence over rules added later.
 * </p>
 *
 * <p>
 * In addition, subclasses can simply override {@link #getStroke(VisualItem)} to
 * achieve custom Stroke assignment. In some cases, this may be the simplest or
 * most flexible approach.
 * </p>
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class StrokeAction extends EncoderAction {

	protected Paint defaultStroke = Color.BLACK;

	/**
	 * Create a new StrokeAction that processes all data groups.
	 */
	public StrokeAction() {
		super();
	}

	/**
	 * Create a new StrokeAction that processes the specified group.
	 *
	 * @param group
	 *            the data group to process
	 */
	public StrokeAction(final String group) {
		super(group);
	}

	/**
	 * Create a new StrokeAction that processes the specified group.
	 *
	 * @param group
	 *            the data group to process
	 * @param defaultStroke
	 *            the default Stroke to assign
	 */
	public StrokeAction(final String group, final Paint defaultStroke) {
		super(group);
		this.defaultStroke = defaultStroke;
	}

	// ------------------------------------------------------------------------

	/**
	 * Set the default BasicStroke to be assigned to items. Items will be
	 * assigned the default Stroke if they do not match any registered rules.
	 *
	 * @param f
	 *            the default BasicStroke to use
	 */
	public void setDefaultStroke(final Paint f) {
		this.defaultStroke = f;
	}

	/**
	 * Get the default BasicStroke assigned to items.
	 *
	 * @return the default BasicStroke
	 */
	public Paint getDefaultStroke() {
		return this.defaultStroke;
	}

	/**
	 * Add a mapping rule to this StrokeAction. VisualItems that match the
	 * provided predicate will be assigned the given BasicStroke value (assuming
	 * they do not match an earlier rule).
	 *
	 * @param p
	 *            the rule Predicate
	 * @param stroke
	 *            the BasicStroke
	 */
	public void add(final Predicate p, final Paint stroke) {
		super.add(p, stroke);
	}

	/**
	 * Add a mapping rule to this StrokeAction. VisualItems that match the
	 * provided expression will be assigned the given BasicStroke value
	 * (assuming they do not match an earlier rule). The provided expression
	 * String will be parsed to generate the needed rule Predicate.
	 *
	 * @param expr
	 *            the expression String, should parse to a Predicate.
	 * @param stroke
	 *            the BasicStroke
	 * @throws RuntimeException
	 *             if the expression does not parse correctly or does not result
	 *             in a Predicate instance.
	 */
	public void add(final String expr, final Paint stroke) {
		final Predicate p = (Predicate) ExpressionParser.parse(expr);
		this.add(p, stroke);
	}

	/**
	 * Add a mapping rule to this StrokeAction. VisualItems that match the
	 * provided predicate will be assigned the BasicStroke value returned by the
	 * given StrokeAction's getStroke() method.
	 *
	 * @param p
	 *            the rule Predicate
	 * @param f
	 *            the delegate StrokeAction to use
	 */
	public void add(final Predicate p, final StrokeAction f) {
		super.add(p, f);
	}

	/**
	 * Add a mapping rule to this StrokeAction. VisualItems that match the
	 * provided expression will be assigned the given BasicStroke value
	 * (assuming they do not match an earlier rule). The provided expression
	 * String will be parsed to generate the needed rule Predicate.
	 *
	 * @param expr
	 *            the expression String, should parse to a Predicate.
	 * @param f
	 *            the delegate StrokeAction to use
	 * @throws RuntimeException
	 *             if the expression does not parse correctly or does not result
	 *             in a Predicate instance.
	 */
	public void add(final String expr, final StrokeAction f) {
		final Predicate p = (Predicate) ExpressionParser.parse(expr);
		super.add(p, f);
	}

	// ------------------------------------------------------------------------

	/**
	 * @see prefuse.action.ItemAction#process(prefuse.visual.VisualItem, double)
	 */
	@Override
	public void process(final VisualItem item, final double frac) {
		item.setStroke(this.getStroke(item));
	}

	/**
	 * Returns the stroke to use for a given VisualItem. Subclasses should
	 * override this method to perform customized Stroke assignment.
	 *
	 * @param item
	 *            the VisualItem for which to get the Stroke
	 * @return the BasicStroke for the given item
	 */
	public Paint getStroke(final VisualItem item) {
		final Object o = this.lookup(item);
		if (o != null) {
			if (o instanceof StrokeAction) {
				return ((StrokeAction) o).getStroke(item);
			} else if (o instanceof Paint) {
				return (Paint) o;
			} else {
				Logger.getLogger(this.getClass().getName()).warning("Unrecognized Object from predicate chain.");
			}
		}
		return this.defaultStroke;
	}

} // end of class StrokeAction
