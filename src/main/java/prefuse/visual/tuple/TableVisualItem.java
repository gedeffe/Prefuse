package prefuse.visual.tuple;

import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import prefuse.Visualization;
import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.tuple.TableTuple;
import prefuse.data.tuple.TupleSet;
import prefuse.render.Renderer;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;

/**
 * VisualItem implementation that uses data values from a backing VisualTable.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class TableVisualItem extends TableTuple implements VisualItem {

	/**
	 * Initialize a new TableVisualItem for the given table and row. This method
	 * is used by the appropriate TupleManager instance, and should not be
	 * called directly by client code, unless by a client-supplied custom
	 * TupleManager.
	 *
	 * @param table
	 *            the data Table
	 * @param graph
	 *            ignored by this class
	 * @param row
	 *            the table row index
	 */
	@Override
	protected void init(final Table table, final Graph graph, final int row) {
		this.m_table = table;
		this.m_row = this.m_table.isValidRow(row) ? row : -1;
	}

	/**
	 * @see prefuse.visual.VisualItem#getVisualization()
	 */
	@Override
	public Visualization getVisualization() {
		return ((VisualTable) this.m_table).getVisualization();
	}

	/**
	 * @see prefuse.visual.VisualItem#getGroup()
	 */
	@Override
	public String getGroup() {
		return ((VisualTable) this.m_table).getGroup();
	}

	/**
	 * @see prefuse.visual.VisualItem#isInGroup(java.lang.String)
	 */
	@Override
	public boolean isInGroup(final String group) {
		return this.getVisualization().isInGroup(this, group);
	}

	/**
	 * @see prefuse.visual.VisualItem#getSourceData()
	 */
	@Override
	public TupleSet getSourceData() {
		final VisualTable vt = (VisualTable) this.m_table;
		return vt.getVisualization().getSourceData(vt.getGroup());
	}

	/**
	 * @see prefuse.visual.VisualItem#getSourceTuple()
	 */
	@Override
	public Tuple getSourceTuple() {
		final VisualTable vt = (VisualTable) this.m_table;
		return vt.getVisualization().getSourceTuple(this);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuffer sbuf = new StringBuffer();
		sbuf.append("VisualItem[").append(this.getGroup());
		sbuf.append(",").append(this.m_row).append(',');
		final VisualTable vt = (VisualTable) this.m_table;
		final int local = vt.getLocalColumnCount();
		final int inherited = vt.getColumnCount() - local;
		for (int i = 0; i < inherited; ++i) {
			if (i > 0) {
				sbuf.append(',');
			}
			final String name = vt.getColumnName(local + i);
			sbuf.append(name);
			sbuf.append('=');
			if (vt.canGetString(name)) {
				sbuf.append(vt.getString(this.m_row, name));
			} else {
				sbuf.append(vt.get(this.m_row, name).toString());
			}
		}
		sbuf.append(']');

		return sbuf.toString();
	}

	// ------------------------------------------------------------------------
	// VisualItem Methods

	/**
	 * @see prefuse.visual.VisualItem#render(javafx.scene.canvas.GraphicsContext)
	 */
	@Override
	public void render(final GraphicsContext g) {
		this.getRenderer().render(g, this);
	}

	/**
	 * @see prefuse.visual.VisualItem#getRenderer()
	 */
	@Override
	public Renderer getRenderer() {
		return this.getVisualization().getRenderer(this);
	}

	/**
	 * @see prefuse.visual.VisualItem#validateBounds()
	 */
	@Override
	public Bounds validateBounds() {
		if (this.isValidated()) {
			return this.getBounds();
		}

		final Visualization v = this.getVisualization();

		// set the new bounds from the renderer and validate
		this.getRenderer().setBounds(this);
		this.setValidated(true);

		// report damage from the new bounds and return
		final Bounds bounds = this.getBounds();
		v.damageReport(this, bounds);
		return bounds;
	}

	// -- Boolean Flags -------------------------------------------------------

	/**
	 * @see prefuse.visual.VisualItem#isValidated()
	 */
	@Override
	public boolean isValidated() {
		return ((VisualTable) this.m_table).isValidated(this.m_row);
	}

	/**
	 * @see prefuse.visual.VisualItem#setValidated(boolean)
	 */
	@Override
	public void setValidated(final boolean value) {
		((VisualTable) this.m_table).setValidated(this.m_row, value);
	}

	/**
	 * @see prefuse.visual.VisualItem#isVisible()
	 */
	@Override
	public boolean isVisible() {
		return ((VisualTable) this.m_table).isVisible(this.m_row);
	}

	/**
	 * @see prefuse.visual.VisualItem#setVisible(boolean)
	 */
	@Override
	public void setVisible(final boolean value) {
		((VisualTable) this.m_table).setVisible(this.m_row, value);
	}

	/**
	 * @see prefuse.visual.VisualItem#isStartVisible()
	 */
	@Override
	public boolean isStartVisible() {
		return ((VisualTable) this.m_table).isStartVisible(this.m_row);
	}

	/**
	 * @see prefuse.visual.VisualItem#setStartVisible(boolean)
	 */
	@Override
	public void setStartVisible(final boolean value) {
		((VisualTable) this.m_table).setStartVisible(this.m_row, value);
	}

	/**
	 * @see prefuse.visual.VisualItem#isEndVisible()
	 */
	@Override
	public boolean isEndVisible() {
		return ((VisualTable) this.m_table).isEndVisible(this.m_row);
	}

	/**
	 * @see prefuse.visual.VisualItem#setEndVisible(boolean)
	 */
	@Override
	public void setEndVisible(final boolean value) {
		((VisualTable) this.m_table).setEndVisible(this.m_row, value);
	}

	/**
	 * @see prefuse.visual.VisualItem#isInteractive()
	 */
	@Override
	public boolean isInteractive() {
		return ((VisualTable) this.m_table).isInteractive(this.m_row);
	}

	/**
	 * @see prefuse.visual.VisualItem#setInteractive(boolean)
	 */
	@Override
	public void setInteractive(final boolean value) {
		((VisualTable) this.m_table).setInteractive(this.m_row, value);
	}

	/**
	 * @see prefuse.visual.VisualItem#isExpanded()
	 */
	@Override
	public boolean isExpanded() {
		return ((VisualTable) this.m_table).isExpanded(this.m_row);
	}

	/**
	 * @see prefuse.visual.VisualItem#setExpanded(boolean)
	 */
	@Override
	public void setExpanded(final boolean value) {
		((VisualTable) this.m_table).setExpanded(this.m_row, value);
	}

	/**
	 * @see prefuse.visual.VisualItem#isFixed()
	 */
	@Override
	public boolean isFixed() {
		return ((VisualTable) this.m_table).isFixed(this.m_row);
	}

	/**
	 * @see prefuse.visual.VisualItem#setFixed(boolean)
	 */
	@Override
	public void setFixed(final boolean value) {
		((VisualTable) this.m_table).setFixed(this.m_row, value);
	}

	/**
	 * @see prefuse.visual.VisualItem#isHighlighted()
	 */
	@Override
	public boolean isHighlighted() {
		return ((VisualTable) this.m_table).isHighlighted(this.m_row);
	}

	/**
	 * @see prefuse.visual.VisualItem#setHighlighted(boolean)
	 */
	@Override
	public void setHighlighted(final boolean value) {
		((VisualTable) this.m_table).setHighlighted(this.m_row, value);
	}

	/**
	 * @see prefuse.visual.VisualItem#isHover()
	 */
	@Override
	public boolean isHover() {
		return ((VisualTable) this.m_table).isHover(this.m_row);
	}

	/**
	 * @see prefuse.visual.VisualItem#setHover(boolean)
	 */
	@Override
	public void setHover(final boolean value) {
		((VisualTable) this.m_table).setHover(this.m_row, value);
	}

	// ------------------------------------------------------------------------

	/**
	 * @see prefuse.visual.VisualItem#getX()
	 */
	@Override
	public double getX() {
		return ((VisualTable) this.m_table).getX(this.m_row);
	}

	/**
	 * @see prefuse.visual.VisualItem#setX(double)
	 */
	@Override
	public void setX(final double x) {
		((VisualTable) this.m_table).setX(this.m_row, x);
	}

	/**
	 * @see prefuse.visual.VisualItem#getY()
	 */
	@Override
	public double getY() {
		return ((VisualTable) this.m_table).getY(this.m_row);
	}

	/**
	 * @see prefuse.visual.VisualItem#setY(double)
	 */
	@Override
	public void setY(final double y) {
		((VisualTable) this.m_table).setY(this.m_row, y);
	}

	/**
	 * @see prefuse.visual.VisualItem#getStartX()
	 */
	@Override
	public double getStartX() {
		return ((VisualTable) this.m_table).getStartX(this.m_row);
	}

	/**
	 * @see prefuse.visual.VisualItem#setStartX(double)
	 */
	@Override
	public void setStartX(final double x) {
		((VisualTable) this.m_table).setStartX(this.m_row, x);
	}

	/**
	 * @see prefuse.visual.VisualItem#getStartY()
	 */
	@Override
	public double getStartY() {
		return ((VisualTable) this.m_table).getStartY(this.m_row);
	}

	/**
	 * @see prefuse.visual.VisualItem#setStartY(double)
	 */
	@Override
	public void setStartY(final double y) {
		((VisualTable) this.m_table).setStartY(this.m_row, y);
	}

	/**
	 * @see prefuse.visual.VisualItem#getEndX()
	 */
	@Override
	public double getEndX() {
		return ((VisualTable) this.m_table).getEndX(this.m_row);
	}

	/**
	 * @see prefuse.visual.VisualItem#setEndX(double)
	 */
	@Override
	public void setEndX(final double x) {
		((VisualTable) this.m_table).setEndX(this.m_row, x);
	}

	/**
	 * @see prefuse.visual.VisualItem#getEndY()
	 */
	@Override
	public double getEndY() {
		return ((VisualTable) this.m_table).getEndY(this.m_row);
	}

	/**
	 * @see prefuse.visual.VisualItem#setEndY(double)
	 */
	@Override
	public void setEndY(final double y) {
		((VisualTable) this.m_table).setEndY(this.m_row, y);
	}

	/**
	 * @see prefuse.visual.VisualItem#getBounds()
	 */
	@Override
	public Bounds getBounds() {
		if (!this.isValidated()) {
			return this.validateBounds();
		}
		return ((VisualTable) this.m_table).getBounds(this.m_row);
	}

	/**
	 * @see prefuse.visual.VisualItem#setBounds(double, double, double, double)
	 */
	@Override
	public void setBounds(final double x, final double y, final double w, final double h) {
		((VisualTable) this.m_table).setBounds(this.m_row, x, y, w, h);
	}

	// ------------------------------------------------------------------------

	/**
	 * @see prefuse.visual.VisualItem#getStrokeColor()
	 */
	@Override
	public int getStrokeColor() {
		return ((VisualTable) this.m_table).getStrokeColor(this.m_row);
	}

	/**
	 * @see prefuse.visual.VisualItem#setStrokeColor(int)
	 */
	@Override
	public void setStrokeColor(final int color) {
		((VisualTable) this.m_table).setStrokeColor(this.m_row, color);
	}

	/**
	 * @see prefuse.visual.VisualItem#getStartStrokeColor()
	 */
	@Override
	public int getStartStrokeColor() {
		return ((VisualTable) this.m_table).getStartStrokeColor(this.m_row);
	}

	/**
	 * @see prefuse.visual.VisualItem#setStartStrokeColor(int)
	 */
	@Override
	public void setStartStrokeColor(final int color) {
		((VisualTable) this.m_table).setStartStrokeColor(this.m_row, color);
	}

	/**
	 * @see prefuse.visual.VisualItem#getEndStrokeColor()
	 */
	@Override
	public int getEndStrokeColor() {
		return ((VisualTable) this.m_table).getEndStrokeColor(this.m_row);
	}

	/**
	 * @see prefuse.visual.VisualItem#setEndStrokeColor(int)
	 */
	@Override
	public void setEndStrokeColor(final int color) {
		((VisualTable) this.m_table).setEndStrokeColor(this.m_row, color);
	}

	/**
	 * @see prefuse.visual.VisualItem#getFillColor()
	 */
	@Override
	public int getFillColor() {
		return ((VisualTable) this.m_table).getFillColor(this.m_row);
	}

	/**
	 * @see prefuse.visual.VisualItem#setFillColor(int)
	 */
	@Override
	public void setFillColor(final int color) {
		((VisualTable) this.m_table).setFillColor(this.m_row, color);
	}

	/**
	 * @see prefuse.visual.VisualItem#getStartFillColor()
	 */
	@Override
	public int getStartFillColor() {
		return ((VisualTable) this.m_table).getStartFillColor(this.m_row);
	}

	/**
	 * @see prefuse.visual.VisualItem#setStartFillColor(int)
	 */
	@Override
	public void setStartFillColor(final int color) {
		((VisualTable) this.m_table).setStartFillColor(this.m_row, color);
	}

	/**
	 * @see prefuse.visual.VisualItem#getEndFillColor()
	 */
	@Override
	public int getEndFillColor() {
		return ((VisualTable) this.m_table).getEndFillColor(this.m_row);
	}

	/**
	 * @see prefuse.visual.VisualItem#setEndFillColor(int)
	 */
	@Override
	public void setEndFillColor(final int color) {
		((VisualTable) this.m_table).setEndFillColor(this.m_row, color);
	}

	/**
	 * @see prefuse.visual.VisualItem#getTextColor()
	 */
	@Override
	public int getTextColor() {
		return ((VisualTable) this.m_table).getTextColor(this.m_row);
	}

	/**
	 * @see prefuse.visual.VisualItem#setTextColor(int)
	 */
	@Override
	public void setTextColor(final int color) {
		((VisualTable) this.m_table).setTextColor(this.m_row, color);
	}

	/**
	 * @see prefuse.visual.VisualItem#getStartTextColor()
	 */
	@Override
	public int getStartTextColor() {
		return ((VisualTable) this.m_table).getStartTextColor(this.m_row);
	}

	/**
	 * @see prefuse.visual.VisualItem#setStartTextColor(int)
	 */
	@Override
	public void setStartTextColor(final int color) {
		((VisualTable) this.m_table).setStartTextColor(this.m_row, color);
	}

	/**
	 * @see prefuse.visual.VisualItem#getEndTextColor()
	 */
	@Override
	public int getEndTextColor() {
		return ((VisualTable) this.m_table).getEndTextColor(this.m_row);
	}

	/**
	 * @see prefuse.visual.VisualItem#setEndTextColor(int)
	 */
	@Override
	public void setEndTextColor(final int color) {
		((VisualTable) this.m_table).setEndTextColor(this.m_row, color);
	}

	// ------------------------------------------------------------------------

	/**
	 * @see prefuse.visual.VisualItem#getSize()
	 */
	@Override
	public double getSize() {
		return ((VisualTable) this.m_table).getSize(this.m_row);
	}

	/**
	 * @see prefuse.visual.VisualItem#setSize(double)
	 */
	@Override
	public void setSize(final double size) {
		((VisualTable) this.m_table).setSize(this.m_row, size);
	}

	/**
	 * @see prefuse.visual.VisualItem#getStartSize()
	 */
	@Override
	public double getStartSize() {
		return ((VisualTable) this.m_table).getStartSize(this.m_row);
	}

	/**
	 * @see prefuse.visual.VisualItem#setStartSize(double)
	 */
	@Override
	public void setStartSize(final double size) {
		((VisualTable) this.m_table).setStartSize(this.m_row, size);
	}

	/**
	 * @see prefuse.visual.VisualItem#getEndSize()
	 */
	@Override
	public double getEndSize() {
		return ((VisualTable) this.m_table).getEndSize(this.m_row);
	}

	/**
	 * @see prefuse.visual.VisualItem#setEndSize(double)
	 */
	@Override
	public void setEndSize(final double size) {
		((VisualTable) this.m_table).setEndSize(this.m_row, size);
	}

	// ------------------------------------------------------------------------

	/**
	 * @see prefuse.visual.VisualItem#getShape()
	 */
	@Override
	public int getShape() {
		return ((VisualTable) this.m_table).getShape(this.m_row);
	}

	/**
	 * @see prefuse.visual.VisualItem#setShape(int)
	 */
	@Override
	public void setShape(final int shape) {
		((VisualTable) this.m_table).setShape(this.m_row, shape);
	}

	// ------------------------------------------------------------------------

	/**
	 * @see prefuse.visual.VisualItem#getStroke()
	 */
	@Override
	public Paint getStroke() {
		return ((VisualTable) this.m_table).getStroke(this.m_row);
	}

	/**
	 * @see prefuse.visual.VisualItem#setStroke(java.awt.BasicStroke)
	 */
	@Override
	public void setStroke(final Paint stroke) {
		((VisualTable) this.m_table).setStroke(this.m_row, stroke);
	}

	// ------------------------------------------------------------------------

	/**
	 * @see prefuse.visual.VisualItem#getFont()
	 */
	@Override
	public Font getFont() {
		return ((VisualTable) this.m_table).getFont(this.m_row);
	}

	/**
	 * @see prefuse.visual.VisualItem#setFont(javafx.scene.text.Font)
	 */
	@Override
	public void setFont(final Font font) {
		((VisualTable) this.m_table).setFont(this.m_row, font);
	}

	/**
	 * @see prefuse.visual.VisualItem#getStartFont()
	 */
	@Override
	public Font getStartFont() {
		return ((VisualTable) this.m_table).getStartFont(this.m_row);
	}

	/**
	 * @see prefuse.visual.VisualItem#setStartFont(javafx.scene.text.Font)
	 */
	@Override
	public void setStartFont(final Font font) {
		((VisualTable) this.m_table).setStartFont(this.m_row, font);
	}

	/**
	 * @see prefuse.visual.VisualItem#getEndFont()
	 */
	@Override
	public Font getEndFont() {
		return ((VisualTable) this.m_table).getEndFont(this.m_row);
	}

	/**
	 * @see prefuse.visual.VisualItem#setEndFont(javafx.scene.text.Font)
	 */
	@Override
	public void setEndFont(final Font font) {
		((VisualTable) this.m_table).setEndFont(this.m_row, font);
	}

	// ------------------------------------------------------------------------

	/**
	 * @see prefuse.visual.VisualItem#getDOI()
	 */
	@Override
	public double getDOI() {
		return ((VisualTable) this.m_table).getDOI(this.m_row);
	}

	/**
	 * @see prefuse.visual.VisualItem#setDOI(double)
	 */
	@Override
	public void setDOI(final double doi) {
		((VisualTable) this.m_table).setDOI(this.m_row, doi);
	}

} // end of class TableVisualItem
