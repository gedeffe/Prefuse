package prefuse.data.query;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import javafx.scene.Node;
import prefuse.data.Table;
import prefuse.data.column.ColumnMetadata;
import prefuse.data.expression.BooleanLiteral;
import prefuse.data.expression.ColumnExpression;
import prefuse.data.expression.ComparisonPredicate;
import prefuse.data.expression.Expression;
import prefuse.data.expression.Literal;
import prefuse.data.expression.OrPredicate;
import prefuse.data.expression.Predicate;
import prefuse.data.tuple.TupleSet;
import prefuse.util.DataLib;
import prefuse.util.ui.JToggleGroup;

/**
 * DynamicQueryBinding supporting queries based on a list of included
 * data values.
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class ListQueryBinding extends DynamicQueryBinding {

    /** String used to indicate inclusion of all data values. */
    private static final String ALL = "All";

    private final Class m_type;
    private ListModel m_model;
    private final Listener m_lstnr;
    private final boolean m_includeAll;

    /**
     * Create a new ListQueryBinding over the given set and data field.
     * @param ts the TupleSet to query
     * @param field the data field (Table column) to query
     */
    public ListQueryBinding(final TupleSet ts, final String field) {
        this(ts, field, true);
    }

    /**
     * Create a new ListQueryBinding over the given set and data field.
     * @param ts the TupleSet to query
     * @param field the data field (Table column) to query
     * @param includeAllOption indicates if the dynamic queries should
     * include an "All" option for including all data values
     */
    public ListQueryBinding(final TupleSet ts, final String field, final boolean includeAllOption) {
        super(ts, field);
        this.m_type = DataLib.inferType(ts, field);
        this.m_lstnr = new Listener();
        this.m_includeAll = includeAllOption;
        this.initPredicate();
        this.initModel();
    }

    private void initPredicate() {
        // set up predicate
        final OrPredicate orP = new OrPredicate();
        orP.add(BooleanLiteral.TRUE);
        this.setPredicate(orP);
    }

    private void initModel() {
        if ( this.m_model != null ) {
            this.m_model.removeListSelectionListener(this.m_lstnr);
        }

        // set up data / selection model
        Object[] o = null;
        if ( this.m_tuples instanceof Table ) {
            final ColumnMetadata md = ((Table)this.m_tuples).getMetadata(this.m_field);
            o = md.getOrdinalArray();
        } else {
            o = DataLib.ordinalArray(this.m_tuples.tuples(), this.m_field);
        }
        this.m_model = new ListModel(o);
        this.m_model.addListSelectionListener(this.m_lstnr);
        if ( this.m_includeAll ) {
            this.m_model.insertElementAt(ListQueryBinding.ALL, 0);
            this.m_model.setSelectedItem(ListQueryBinding.ALL);
        }
    }

    // ------------------------------------------------------------------------

    /**
     * Returns a list model for creating custom dynamic query widgets.
     * This list model acts both as a data model and a selection model,
     * and so must be registered as both with any custom widgets.
     * @return the dynamic query list model
     */
    public ListModel getListModel() {
        return this.m_model;
    }

    /**
     * Creates a new group of check boxes for interacting with the query.
     * @return a {@link prefuse.util.ui.JToggleGroup} of check boxes bound to
     * this dynamic query.
     * @see prefuse.data.query.DynamicQueryBinding#createComponent()
     */
    @Override
    public Node createComponent() {
        return this.createCheckboxGroup();
    }

    /**
     * Create a new interactive list for interacting with the query.
     * @return a {@link javax.swing.JList} bound to this dynamic query.
     */
    public JList createList() {
        final JList list = new JList(this.m_model);
        list.setSelectionModel(this.m_model);
        return list;
    }

    /**
     * Create a new drop-down combo box for interacting with the query.
     * @return a {@link javax.swing.JComboBox} bound to this dynamic query.
     */
    public JComboBox createComboBox() {
        return new JComboBox(this.m_model);
    }

    /**
     * Creates a new group of check boxes for interacting with the query.
     * @return a {@link prefuse.util.ui.JToggleGroup} of check boxes bound to
     * this dynamic query.
     */
    public JToggleGroup createCheckboxGroup() {
        return this.createToggleGroup(JToggleGroup.CHECKBOX);
    }

    /**
     * Creates a new group of radio buttons for interacting with the query.
     * @return a {@link prefuse.util.ui.JToggleGroup} of radio buttons bound to
     * this dynamic query.
     */
    public JToggleGroup createRadioGroup() {
        return this.createToggleGroup(JToggleGroup.RADIO);
    }

    private JToggleGroup createToggleGroup(final int type) {
        return new JToggleGroup(type, this.m_model, this.m_model);
    }

    // ------------------------------------------------------------------------

    /**
     * Create a comparison predicate fof the given data value
     */
    private ComparisonPredicate getComparison(final Object o) {
        final Expression left = new ColumnExpression(this.m_field);
        final Expression right = Literal.getLiteral(o, this.m_type);
        return new ComparisonPredicate(ComparisonPredicate.EQ, left, right);
    }

    private class Listener implements ListSelectionListener {

        @Override
        public void valueChanged(final ListSelectionEvent e) {
            final ListModel model = (ListModel)e.getSource();
            final OrPredicate orP = (OrPredicate)ListQueryBinding.this.m_query;

            if ( model.isSelectionEmpty() )
            {
                orP.clear();
            }
            else if ( ListQueryBinding.this.m_includeAll && model.isSelectedIndex(0) )
            {
                orP.set(BooleanLiteral.TRUE);
            }
            else
            {
                final int min   = model.getMinSelectionIndex();
                final int max   = model.getMaxSelectionIndex();
                int count = 0;
                for ( int i=min; i<=max; ++i ) {
                    if ( model.isSelectedIndex(i) ) {
                        ++count;
                    }
                }

                if ( count == model.getSize() ) {
                    orP.set(BooleanLiteral.TRUE);
                } else if ( count == 1 ) {
                    orP.set(ListQueryBinding.this.getComparison(model.getElementAt(min)));
                } else {
                    final Predicate[] p = new Predicate[count];
                    for ( int i=min, j=0; i<=max; ++i ) {
                        if ( model.isSelectedIndex(i) ) {
                            p[j++] = ListQueryBinding.this.getComparison(model.getElementAt(i));
                        }
                    }
                    orP.set(p);
                }
            }
        }
    }

} // end of class ListQueryBinding
