package prefuse.data.query;

import javafx.scene.Node;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.expression.AbstractPredicate;
import prefuse.data.search.PrefixSearchTupleSet;
import prefuse.data.search.SearchTupleSet;
import prefuse.data.tuple.TupleSet;
import prefuse.util.ui.JSearchPanel;
import prefuse.visual.VisualTupleSet;

/**
 * DynamicQueryBinding supporting text search over data values. Implementations
 * of the {@link prefuse.data.search.SearchTupleSet} class from the
 * {@link prefuse.data.search} package can be used to control the type of
 * search index used.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 * @see prefuse.data.search.SearchTupleSet
 */
public class SearchQueryBinding extends DynamicQueryBinding {

    private final SearchTupleSet m_set;
    private final Listener m_lstnr;
    private Object m_lock;

    /**
     * Create a new SearchQueryBinding over the given set and data field.
     * @param ts the TupleSet to query
     * @param field the data field (Table column) to query
     */
    public SearchQueryBinding(final TupleSet ts, final String field) {
        this(ts, field, new PrefixSearchTupleSet());
    }

    /**
     * Create a new SearchQueryBinding over the given set and data field,
     * using the specified SearchTupleSet instance. Use this constructor to
     * choose the type of search engine used, and to potentially reuse the
     * same search set over multiple dynamic query bindings.
     * @param ts the TupleSet to query
     * @param field the data field (Table column) to query
     * @param set the {@link prefuse.data.search.SearchTupleSet} to use.
     */
    public SearchQueryBinding(final TupleSet ts, final String field, final SearchTupleSet set) {
        super(ts, field);
        this.m_lstnr = new Listener();
        this.setPredicate(new SearchBindingPredicate());

        this.m_set = set;
        this.m_set.index(ts.tuples(), field);
        this.m_set.addTupleSetListener(this.m_lstnr);

        if ( ts instanceof VisualTupleSet ) {
            this.m_lock = ((VisualTupleSet)ts).getVisualization();
        }
    }

    /**
     * Return the SearchTupleSet used for conducting searches.
     * @return the {@link prefuse.data.search.SearchTupleSet} used by this
     * dynamic query binding.
     */
    public SearchTupleSet getSearchSet() {
        return this.m_set;
    }

    // ------------------------------------------------------------------------

    /**
     * Create a new search text panel for searching over the data.
     * @return a {@link prefuse.util.ui.JSearchPanel} bound to this
     * dynamic query.
     * @see prefuse.data.query.DynamicQueryBinding#createComponent()
     */
    @Override
    public Node createComponent() {
        return this.createSearchPanel();
    }

    /**
     * Create a new search text panel for searching over the data.
     * @return a {@link prefuse.util.ui.JSearchPanel} bound to this
     * dynamic query.
     */
    public JSearchPanel createSearchPanel() {
        return this.createSearchPanel(this.m_set instanceof PrefixSearchTupleSet);
    }

    /**
     * Create a new search text panel for searching over the data.
     * @param monitorKeystrokes if true, each keystroke will cause the
     * search to be re-run (this is the default for prefix searches);
     * if false, searches will only re-run when the enter key is typed
     * (this is the default for the other search engine types).
     * @return a {@link prefuse.util.ui.JSearchPanel} bound to this
     * dynamic query.
     */
    public JSearchPanel createSearchPanel(final boolean monitorKeystrokes) {
        final JSearchPanel jsp = new JSearchPanel(this.m_set, this.m_field, monitorKeystrokes);
        if ( this.m_lock != null ) { jsp.setLock(this.m_lock); }
        return jsp;
    }

    // ------------------------------------------------------------------------

    private class SearchBindingPredicate extends AbstractPredicate {
        @Override
        public boolean getBoolean(final Tuple t) {
            final String q = SearchQueryBinding.this.m_set.getQuery();
            return ((q==null) || (q.length()==0) || SearchQueryBinding.this.m_set.containsTuple(t));
        }
        public void touch() {
            this.fireExpressionChange();
        }
    }

    private class Listener implements TupleSetListener {
        @Override
        public void tupleSetChanged(final TupleSet tset, final Tuple[] added, final Tuple[] removed) {
            ((SearchBindingPredicate)SearchQueryBinding.this.getPredicate()).touch();
        }
    }

} // end of class SearchQueryBinding
