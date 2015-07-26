package prefuse.util.ui;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import prefuse.Visualization;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.search.PrefixSearchTupleSet;
import prefuse.data.search.SearchTupleSet;
import prefuse.data.tuple.TupleSet;

/**
 * JavaFX component that enables keyword search over prefuse data tuples.
 *
 * @see prefuse.data.query.SearchQueryBinding
 */
public class JfxSearchPanel extends HBox implements EventHandler<KeyEvent> {
	private Object m_lock;
	private SearchTupleSet m_searcher;

	private final TextField m_queryF = new TextField("               ");
	private final Label m_resultL = new Label("          ");
	private final Label m_searchL = new Label("search >> ");
	private final HBox m_sbox = new HBox();

	private final String[] m_fields;

	private boolean m_includeHitCount = false;
	private boolean m_monitorKeys = false;
	private boolean m_autoIndex = true;

	private boolean m_showBorder = true;
	private boolean m_showCancel = true;

	// ------------------------------------------------------------------------
	// Free form constructors

	/**
	 * Create a new JfxSearchPanel.
	 *
	 * @param search
	 *            the search tuple set conducting the searches
	 * @param field
	 *            the data field being searched
	 */
	public JfxSearchPanel(final SearchTupleSet search, final String field) {
		this(search, field, false);
	}

	/**
	 * Create a new JfxSearchPanel.
	 *
	 * @param search
	 *            the search tuple set conducting the searches
	 * @param field
	 *            the data field being searched
	 * @param monitorKeystrokes
	 *            indicates if each keystroke event should result in a new
	 *            search being issued (true) or if searches should only be
	 *            initiated by hitting the enter key (false)
	 */
	public JfxSearchPanel(final SearchTupleSet search, final String field, final boolean monitorKeystrokes) {
		this(null, search, new String[] { field }, false, monitorKeystrokes);
	}

	/**
	 * Create a new JfxSearchPanel.
	 *
	 * @param source
	 *            the source set of tuples that should be searched over
	 * @param search
	 *            the search tuple set conducting the searches
	 * @param fields
	 *            the data fields being searched
	 * @param monitorKeystrokes
	 *            indicates if each keystroke event should result in a new
	 *            search being issued (true) or if searches should only be
	 *            initiated by hitting the enter key (false)
	 */
	public JfxSearchPanel(final TupleSet source, final SearchTupleSet search, final String[] fields,
			final boolean autoIndex, final boolean monitorKeystrokes) {
		this.m_lock = new Object();
		this.m_fields = fields;
		this.m_autoIndex = autoIndex;
		this.m_monitorKeys = monitorKeystrokes;

		this.m_searcher = (search != null ? search : new PrefixSearchTupleSet());

		this.init(source);
	}

	// ------------------------------------------------------------------------
	// Visualization-based constructors

	/**
	 * Create a new JfxSearchPanel. The default search tuple set for the
	 * visualization will be used.
	 *
	 * @param vis
	 *            the Visualization to search over
	 * @param field
	 *            the data field being searched
	 */
	public JfxSearchPanel(final Visualization vis, final String field) {
		this(vis, Visualization.ALL_ITEMS, field, true);
	}

	/**
	 * Create a new JfxSearchPanel. The default search tuple set for the
	 * visualization will be used.
	 *
	 * @param vis
	 *            the Visualization to search over
	 * @param group
	 *            the particular data group to search over
	 * @param field
	 *            the data field being searched
	 */
	public JfxSearchPanel(final Visualization vis, final String group, final String field) {
		this(vis, group, field, true);
	}

	/**
	 * Create a new JfxSearchPanel. The default search tuple set for the
	 * visualization will be used.
	 *
	 * @param vis
	 *            the Visualization to search over
	 * @param group
	 *            the particular data group to search over
	 * @param field
	 *            the data field being searched
	 * @param autoIndex
	 *            indicates if items should be automatically indexed and
	 *            unindexed as their membership in the source group changes.
	 */
	public JfxSearchPanel(final Visualization vis, final String group, final String field, final boolean autoIndex) {
		this(vis, group, Visualization.SEARCH_ITEMS, new String[] { field }, autoIndex, false);
	}

	/**
	 * Create a new JfxSearchPanel. The default search tuple set for the
	 * visualization will be used.
	 *
	 * @param vis
	 *            the Visualization to search over
	 * @param group
	 *            the particular data group to search over
	 * @param field
	 *            the data field being searched
	 * @param autoIndex
	 *            indicates if items should be automatically indexed and
	 *            unindexed as their membership in the source group changes.
	 * @param monitorKeystrokes
	 *            indicates if each keystroke event should result in a new
	 *            search being issued (true) or if searches should only be
	 *            initiated by hitting the enter key (false)
	 */
	public JfxSearchPanel(final Visualization vis, final String group, final String field, final boolean autoIndex,
			final boolean monitorKeystrokes) {
		this(vis, group, Visualization.SEARCH_ITEMS, new String[] { field }, autoIndex, true);
	}

	/**
	 * Create a new JfxSearchPanel.
	 *
	 * @param vis
	 *            the Visualization to search over
	 * @param group
	 *            the particular data group to search over
	 * @param searchGroup
	 *            the group name that resolves to the SearchTupleSet to use
	 * @param field
	 *            the data field being searched
	 * @param autoIndex
	 *            indicates if items should be automatically indexed and
	 *            unindexed as their membership in the source group changes.
	 * @param monitorKeystrokes
	 *            indicates if each keystroke event should result in a new
	 *            search being issued (true) or if searches should only be
	 *            initiated by hitting the enter key (false)
	 */
	public JfxSearchPanel(final Visualization vis, final String group, final String searchGroup, final String field,
			final boolean autoIndex, final boolean monitorKeystrokes) {
		this(vis, group, searchGroup, new String[] { field }, autoIndex, monitorKeystrokes);
	}

	/**
	 * Create a new JfxSearchPanel.
	 *
	 * @param vis
	 *            the Visualization to search over
	 * @param group
	 *            the particular data group to search over
	 * @param searchGroup
	 *            the group name that resolves to the SearchTupleSet to use
	 * @param fields
	 *            the data fields being searched
	 * @param autoIndex
	 *            indicates if items should be automatically indexed and
	 *            unindexed as their membership in the source group changes.
	 * @param monitorKeystrokes
	 *            indicates if each keystroke event should result in a new
	 *            search being issued (true) or if searches should only be
	 *            initiated by hitting the enter key (false)
	 */
	public JfxSearchPanel(final Visualization vis, final String group, final String searchGroup, final String[] fields,
			final boolean autoIndex, final boolean monitorKeystrokes) {
		this.m_lock = vis;
		this.m_fields = fields;
		this.m_autoIndex = autoIndex;
		this.m_monitorKeys = monitorKeystrokes;

		final TupleSet search = vis.getGroup(searchGroup);

		if (search != null) {
			if (search instanceof SearchTupleSet) {
				this.m_searcher = (SearchTupleSet) search;
			} else {
				throw new IllegalStateException("Search focus set not instance of SearchTupleSet!");
			}
		} else {
			this.m_searcher = new PrefixSearchTupleSet();
			vis.addFocusGroup(searchGroup, this.m_searcher);
		}

		this.init(vis.getGroup(group));
	}

	// ------------------------------------------------------------------------
	// Initialization

	private void init(final TupleSet source) {
		if (this.m_autoIndex && (source != null)) {
			// index everything already there
			for (int i = 0; i < this.m_fields.length; i++) {
				this.m_searcher.index(source.tuples(), this.m_fields[i]);
			}

			// add a listener to dynamically build search index
			source.addTupleSetListener(new TupleSetListener() {
				@Override
				public void tupleSetChanged(final TupleSet tset, final Tuple[] add, final Tuple[] rem) {
					if (add != null) {
						for (int i = 0; i < add.length; ++i) {
							for (int j = 0; j < JfxSearchPanel.this.m_fields.length; j++) {
								JfxSearchPanel.this.m_searcher.index(add[i], JfxSearchPanel.this.m_fields[j]);
							}
						}
					}
					if ((rem != null) && JfxSearchPanel.this.m_searcher.isUnindexSupported()) {
						for (int i = 0; i < rem.length; ++i) {
							for (int j = 0; j < JfxSearchPanel.this.m_fields.length; j++) {
								JfxSearchPanel.this.m_searcher.unindex(rem[i], JfxSearchPanel.this.m_fields[j]);
							}
						}
					}
				}
			});
		}

		// use css to default styling
		this.getStylesheets().add(this.getClass().getResource("searchpanel.css").toExternalForm());
		this.getStyleClass().add("searchpanel");

		this.m_queryF.setOnKeyTyped(this);
		this.m_queryF.setMaxSize(400, 100);
		this.m_queryF.setPrefSize(200, 20);
		this.m_queryF.setBorder(null);
		this.initUI();
	}

	private void initUI() {
		this.m_sbox.getChildren().clear();
		this.m_sbox.setPadding(new Insets(3));
		this.m_sbox.getChildren().add(this.m_queryF);
		if (this.m_showCancel) {
			final JfxCancelButton cancelButton = new JfxCancelButton((event) -> {
				JfxSearchPanel.this.setQuery(null);
			});

			this.m_sbox.getChildren().add(cancelButton);

		}
		if (this.m_showBorder) {
			// might replaced by a Border with BorderStrokes or we could use CSS
			// ...
			// this.m_sbox.setBorder(BorderFactory.createLineBorder(this.getForeground()));
		} else {
			this.m_sbox.setBorder(null);
		}
		this.m_sbox.setMaxSize(400, 100);
		this.m_sbox.setPrefSize(171, 20);

		final HBox b = new HBox();
		if (this.m_includeHitCount) {
			b.getChildren().add(this.m_resultL);
		}
		b.getChildren().add(this.m_searchL);
		b.getChildren().add(this.m_sbox);

		this.getChildren().add(b);
	}

	// ------------------------------------------------------------------------

	/**
	 * Request the keyboard focus for this component.
	 */
	@Override
	public void requestFocus() {
		this.m_queryF.requestFocus();
	}

	/**
	 * Set the lock, an object to synchronize on while issuing queries.
	 *
	 * @param lock
	 *            the synchronization lock
	 */
	public void setLock(final Object lock) {
		this.m_lock = lock;
	}

	/**
	 * Indicates if the component should show the number of search results.
	 *
	 * @param b
	 *            true to show the result count, false to hide it
	 */
	public void setShowResultCount(final boolean b) {
		this.m_includeHitCount = b;
		this.initUI();
	}

	/**
	 * Indicates if the component should show a border around the text field.
	 *
	 * @param b
	 *            true to show the text field border, false to hide it
	 */
	public void setShowBorder(final boolean b) {
		this.m_showBorder = b;
		this.initUI();
	}

	/**
	 * Indicates if the component should show the cancel query button.
	 *
	 * @param b
	 *            true to show the cancel query button, false to hide it
	 */
	public void setShowCancel(final boolean b) {
		this.m_showCancel = b;
		this.initUI();
	}

	/**
	 * Update the search results based on the current query.
	 */
	protected void searchUpdate() {
		final String query = this.m_queryF.getText();
		synchronized (this.m_lock) {
			this.m_searcher.search(query);
			if (this.m_searcher.getQuery().length() == 0) {
				this.m_resultL.setText(null);
			} else {
				final int r = this.m_searcher.getTupleCount();
				this.m_resultL.setText(r + " match" + (r == 1 ? "" : "es"));
			}
		}
	}

	/**
	 * Set the query string in the text field.
	 *
	 * @param query
	 *            the query string to use
	 */
	public void setQuery(final String query) {
		this.m_queryF.setText(query);
		this.searchUpdate();
	}

	/**
	 * Get the query string in the text field.
	 *
	 * @return the current query string
	 */
	public String getQuery() {
		return this.m_queryF.getText();
	}

	/**
	 * Set the label text used on this component.
	 *
	 * @param text
	 *            the label text, use null to show no label
	 */
	public void setLabelText(final String text) {
		this.m_searchL.setText(text);
	}

	@Override
	public void handle(final KeyEvent event) {
		final Object src = event.getSource();
		if (src == this.m_queryF) {
			/*
			 * Update the search engine with new text, always on action key
			 * (enter) or on any key if we have activated the option.
			 */
			if (this.m_monitorKeys || event.getCode().equals(KeyCode.ENTER)) {
				this.searchUpdate();
			}
		}
	}

} // end of class JfxSearchPanel
