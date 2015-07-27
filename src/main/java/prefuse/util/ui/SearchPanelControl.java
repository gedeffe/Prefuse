package prefuse.util.ui;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import prefuse.data.search.PrefixSearchTupleSet;
import prefuse.data.search.SearchTupleSet;

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

	public SearchPanelControl() {
		// temporary initialize internal elements with dummy values
		this.m_lock = new Object();
		this.m_searcher = new PrefixSearchTupleSet();
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
	protected void updateSearchEngine() {
		// a new key has been typed into query text field.
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

}
