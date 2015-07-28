package prefuse.util.ui;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
public class SearchPanelControl extends HBox {
	@FXML
	private TextField queryTextField;
	@FXML
	private Label resultLabel;

	private final Object m_lock;
	private final SearchTupleSet m_searcher;

	private final String[] m_fields;

	private boolean m_monitorKeys = false;
	private boolean m_autoIndex = true;

	// ------------------------------------------------------------------------
	// Free form constructors

	/**
	 * Create a new SearchPanelControl.
	 *
	 * @param search
	 *            the search tuple set conducting the searches
	 * @param field
	 *            the data field being searched
	 */
	public SearchPanelControl(final SearchTupleSet search, final String field) {
		this(search, field, false);
	}

	/**
	 * Create a new SearchPanelControl.
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
	public SearchPanelControl(final SearchTupleSet search, final String field, final boolean monitorKeystrokes) {
		this(null, search, new String[] { field }, false, monitorKeystrokes);
	}

	/**
	 * Create a new SearchPanelControl.
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
	public SearchPanelControl(final TupleSet source, final SearchTupleSet search, final String[] fields,
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
	 * Create a new SearchPanelControl. The default search tuple set for the
	 * visualization will be used.
	 *
	 * @param vis
	 *            the Visualization to search over
	 * @param field
	 *            the data field being searched
	 */
	public SearchPanelControl(final Visualization vis, final String field) {
		this(vis, Visualization.ALL_ITEMS, field, true);
	}

	/**
	 * Create a new SearchPanelControl. The default search tuple set for the
	 * visualization will be used.
	 *
	 * @param vis
	 *            the Visualization to search over
	 * @param group
	 *            the particular data group to search over
	 * @param field
	 *            the data field being searched
	 */
	public SearchPanelControl(final Visualization vis, final String group, final String field) {
		this(vis, group, field, true);
	}

	/**
	 * Create a new SearchPanelControl. The default search tuple set for the
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
	public SearchPanelControl(final Visualization vis, final String group, final String field,
			final boolean autoIndex) {
		this(vis, group, Visualization.SEARCH_ITEMS, new String[] { field }, autoIndex, false);
	}

	/**
	 * Create a new SearchPanelControl. The default search tuple set for the
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
	public SearchPanelControl(final Visualization vis, final String group, final String field, final boolean autoIndex,
			final boolean monitorKeystrokes) {
		this(vis, group, Visualization.SEARCH_ITEMS, new String[] { field }, autoIndex, true);
	}

	/**
	 * Create a new SearchPanelControl.
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
	public SearchPanelControl(final Visualization vis, final String group, final String searchGroup, final String field,
			final boolean autoIndex, final boolean monitorKeystrokes) {
		this(vis, group, searchGroup, new String[] { field }, autoIndex, monitorKeystrokes);
	}

	/**
	 * Create a new SearchPanelControl.
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
	public SearchPanelControl(final Visualization vis, final String group, final String searchGroup,
			final String[] fields, final boolean autoIndex, final boolean monitorKeystrokes) {
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
							for (int j = 0; j < SearchPanelControl.this.m_fields.length; j++) {
								SearchPanelControl.this.m_searcher.index(add[i], SearchPanelControl.this.m_fields[j]);
							}
						}
					}
					if ((rem != null) && SearchPanelControl.this.m_searcher.isUnindexSupported()) {
						for (int i = 0; i < rem.length; ++i) {
							for (int j = 0; j < SearchPanelControl.this.m_fields.length; j++) {
								SearchPanelControl.this.m_searcher.unindex(rem[i], SearchPanelControl.this.m_fields[j]);
							}
						}
					}
				}
			});
		}
		this.initUI();
	}

	private void initUI() {
		// load display part.
		final FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("SearchPanel.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (final IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	@FXML
	protected void performCancel() {
		this.setQuery(null);
	}

	@FXML
	protected void updateSearchEngine(final KeyEvent event) {
		/*
		 * Update the search engine with new text, always on action key (enter)
		 * or on any key if we have activated the option.
		 */
		if (this.m_monitorKeys || event.getCode().equals(KeyCode.ENTER)) {
			this.searchUpdate();
		}
	}

	/**
	 * Set the query string in the text field.
	 *
	 * @param query
	 *            the query string to use
	 */
	public void setQuery(final String query) {
		this.queryTextField.setText(query);
		this.searchUpdate();
	}

	/**
	 * Get the query string in the text field.
	 *
	 * @return the current query string
	 */
	public String getQuery() {
		return this.queryTextField.getText();
	}

	/**
	 * Update the search results based on the current query.
	 */
	protected void searchUpdate() {
		final String query = this.queryTextField.getText();
		synchronized (this.m_lock) {
			this.m_searcher.search(query);
			if (this.m_searcher.getQuery().length() == 0) {
				this.resultLabel.setText(null);
			} else {
				final int r = this.m_searcher.getTupleCount();
				this.resultLabel.setText(r + " match" + (r == 1 ? "" : "es"));
			}
		}
	}

	/**
	 * Request the keyboard focus for this component.
	 */
	@Override
	public void requestFocus() {
		this.queryTextField.requestFocus();
	}
}
