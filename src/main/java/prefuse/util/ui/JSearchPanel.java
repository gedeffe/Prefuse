package prefuse.util.ui;

import java.awt.Color;
import java.awt.Dimension;
import javafx.scene.text.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import prefuse.Visualization;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.search.PrefixSearchTupleSet;
import prefuse.data.search.SearchTupleSet;
import prefuse.data.tuple.TupleSet;
import prefuse.util.ColorLib;

/**
 * Swing component that enables keyword search over prefuse data tuples.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 * @see prefuse.data.query.SearchQueryBinding
 * @deprecated replaced by prefuse.util.ui.JfxSearchPanel.
 */
@Deprecated
public class JSearchPanel extends JPanel implements DocumentListener, ActionListener {
	private Object m_lock;
	private SearchTupleSet m_searcher;

	private final JTextField m_queryF = new JTextField(15);
	private final JLabel m_resultL = new JLabel("          ");
	private final JLabel m_searchL = new JLabel("search >> ");
	private final Box m_sbox = new Box(BoxLayout.X_AXIS);

	private final String[] m_fields;

	private Color m_cancelColor = ColorLib.getColor(255, 75, 75);

	private boolean m_includeHitCount = false;
	private boolean m_monitorKeys = false;
	private boolean m_autoIndex = true;

	private boolean m_showBorder = true;
	private boolean m_showCancel = true;

	// ------------------------------------------------------------------------
	// Free form constructors

	/**
	 * Create a new JSearchPanel.
	 * 
	 * @param search
	 *            the search tuple set conducting the searches
	 * @param field
	 *            the data field being searched
	 */
	public JSearchPanel(final SearchTupleSet search, final String field) {
		this(search, field, false);
	}

	/**
	 * Create a new JSearchPanel.
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
	public JSearchPanel(final SearchTupleSet search, final String field, final boolean monitorKeystrokes) {
		this(null, search, new String[] { field }, false, monitorKeystrokes);
	}

	/**
	 * Create a new JSearchPanel.
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
	public JSearchPanel(final TupleSet source, final SearchTupleSet search, final String[] fields,
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
	 * Create a new JSearchPanel. The default search tuple set for the
	 * visualization will be used.
	 * 
	 * @param vis
	 *            the Visualization to search over
	 * @param field
	 *            the data field being searched
	 */
	public JSearchPanel(final Visualization vis, final String field) {
		this(vis, Visualization.ALL_ITEMS, field, true);
	}

	/**
	 * Create a new JSearchPanel. The default search tuple set for the
	 * visualization will be used.
	 * 
	 * @param vis
	 *            the Visualization to search over
	 * @param group
	 *            the particular data group to search over
	 * @param field
	 *            the data field being searched
	 */
	public JSearchPanel(final Visualization vis, final String group, final String field) {
		this(vis, group, field, true);
	}

	/**
	 * Create a new JSearchPanel. The default search tuple set for the
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
	public JSearchPanel(final Visualization vis, final String group, final String field, final boolean autoIndex) {
		this(vis, group, Visualization.SEARCH_ITEMS, new String[] { field }, autoIndex, false);
	}

	/**
	 * Create a new JSearchPanel. The default search tuple set for the
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
	public JSearchPanel(final Visualization vis, final String group, final String field, final boolean autoIndex,
			final boolean monitorKeystrokes) {
		this(vis, group, Visualization.SEARCH_ITEMS, new String[] { field }, autoIndex, true);
	}

	/**
	 * Create a new JSearchPanel.
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
	public JSearchPanel(final Visualization vis, final String group, final String searchGroup, final String field,
			final boolean autoIndex, final boolean monitorKeystrokes) {
		this(vis, group, searchGroup, new String[] { field }, autoIndex, monitorKeystrokes);
	}

	/**
	 * Create a new JSearchPanel.
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
	public JSearchPanel(final Visualization vis, final String group, final String searchGroup, final String[] fields,
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
							for (int j = 0; j < JSearchPanel.this.m_fields.length; j++) {
								JSearchPanel.this.m_searcher.index(add[i], JSearchPanel.this.m_fields[j]);
							}
						}
					}
					if ((rem != null) && JSearchPanel.this.m_searcher.isUnindexSupported()) {
						for (int i = 0; i < rem.length; ++i) {
							for (int j = 0; j < JSearchPanel.this.m_fields.length; j++) {
								JSearchPanel.this.m_searcher.unindex(rem[i], JSearchPanel.this.m_fields[j]);
							}
						}
					}
				}
			});
		}

		this.m_queryF.addActionListener(this);
		if (this.m_monitorKeys) {
			this.m_queryF.getDocument().addDocumentListener(this);
		}
		this.m_queryF.setMaximumSize(new Dimension(400, 100));
		this.m_queryF.setPreferredSize(new Dimension(200, 20));
		this.m_queryF.setBorder(null);
		this.setBackground(Color.WHITE);
		this.initUI();
	}

	private void initUI() {
		this.removeAll();
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		this.m_sbox.removeAll();
		this.m_sbox.add(Box.createHorizontalStrut(3));
		this.m_sbox.add(this.m_queryF);
		this.m_sbox.add(Box.createHorizontalStrut(3));
		if (this.m_showCancel) {
			this.m_sbox.add(new CancelButton());
			this.m_sbox.add(Box.createHorizontalStrut(3));
		}
		if (this.m_showBorder) {
			this.m_sbox.setBorder(BorderFactory.createLineBorder(this.getForeground()));
		} else {
			this.m_sbox.setBorder(null);
		}
		this.m_sbox.setMaximumSize(new Dimension(400, 100));
		this.m_sbox.setPreferredSize(new Dimension(171, 20));

		final Box b = new Box(BoxLayout.X_AXIS);
		if (this.m_includeHitCount) {
			b.add(this.m_resultL);
			b.add(Box.createHorizontalStrut(10));
			// b.add(Box.createHorizontalGlue());
		}
		b.add(this.m_searchL);
		b.add(Box.createHorizontalStrut(3));
		b.add(this.m_sbox);

		this.add(b);
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
		this.validate();
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
		this.validate();
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
		this.validate();
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
		final Document d = this.m_queryF.getDocument();
		d.removeDocumentListener(this);
		this.m_queryF.setText(query);
		if (this.m_monitorKeys) {
			d.addDocumentListener(this);
		}
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
	 * Set the fill color of the cancel 'x' button that appears when the button
	 * has the mouse pointer over it.
	 * 
	 * @param c
	 *            the cancel color
	 */
	public void setCancelColor(final Color c) {
		this.m_cancelColor = c;
	}

	/**
	 * @see java.awt.Component#setBackground(java.awt.Color)
	 */
	@Override
	public void setBackground(final Color bg) {
		super.setBackground(bg);
		if (this.m_queryF != null) {
			this.m_queryF.setBackground(bg);
		}
		if (this.m_resultL != null) {
			this.m_resultL.setBackground(bg);
		}
		if (this.m_searchL != null) {
			this.m_searchL.setBackground(bg);
		}
	}

	/**
	 * @see java.awt.Component#setForeground(java.awt.Color)
	 */
	@Override
	public void setForeground(final Color fg) {
		super.setForeground(fg);
		if (this.m_queryF != null) {
			this.m_queryF.setForeground(fg);
			this.m_queryF.setCaretColor(fg);
		}
		if (this.m_resultL != null) {
			this.m_resultL.setForeground(fg);
		}
		if (this.m_searchL != null) {
			this.m_searchL.setForeground(fg);
		}
		if ((this.m_sbox != null) && this.m_showBorder) {
			this.m_sbox.setBorder(BorderFactory.createLineBorder(fg));
		}
	}

	/**
	 * @see javax.swing.JComponent#setOpaque(boolean)
	 */
	@Override
	public void setOpaque(final boolean opaque) {
		super.setOpaque(opaque);
		if (this.m_queryF != null) {
			this.m_queryF.setOpaque(opaque);
		}
		if (this.m_resultL != null) {
			this.m_resultL.setOpaque(opaque);
		}
		if (this.m_searchL != null) {
			this.m_searchL.setOpaque(opaque);
		}
	}

	/**
	 * @see java.awt.Component#setFont(javafx.scene.text.Font)
	 */
	@Override
	public void setFont(final Font f) {
		super.setFont(f);
		;
		if (this.m_queryF != null) {
			this.m_queryF.setFont(f);
		}
		if (this.m_resultL != null) {
			this.m_resultL.setFont(f);
		}
		if (this.m_searchL != null) {
			this.m_searchL.setFont(f);
		}
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

	/**
	 * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
	 */
	@Override
	public void changedUpdate(final DocumentEvent e) {
		this.searchUpdate();
	}

	/**
	 * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
	 */
	@Override
	public void insertUpdate(final DocumentEvent e) {
		this.searchUpdate();
	}

	/**
	 * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
	 */
	@Override
	public void removeUpdate(final DocumentEvent e) {
		this.searchUpdate();
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(final ActionEvent e) {
		final Object src = e.getSource();
		if (src == this.m_queryF) {
			this.searchUpdate();
		}
	}

	/**
	 * A button depicted as an "X" that allows users to cancel the current query
	 * and clear the query field.
	 */
	public class CancelButton extends JComponent implements MouseListener {

		private boolean hover = false;
		private final int[] outline = new int[] { 0, 0, 2, 0, 4, 2, 5, 2, 7, 0, 9, 0, 9, 2, 7, 4, 7, 5, 9, 7, 9, 9, 7,
				9, 5, 7, 4, 7, 2, 9, 0, 9, 0, 7, 2, 5, 2, 4, 0, 2, 0, 0 };
		private final int[] fill = new int[] { 1, 1, 8, 8, 1, 2, 7, 8, 2, 1, 8, 7, 7, 1, 1, 7, 8, 2, 2, 8, 1, 8, 8, 1 };

		public CancelButton() {
			// set button size
			final Dimension d = new Dimension(10, 10);
			this.setPreferredSize(d);
			this.setMinimumSize(d);
			this.setMaximumSize(d);

			// prevent the widget from getting the keyboard focus
			this.setFocusable(false);

			// add callbacks
			this.addMouseListener(this);
		}

		@Override
		public void paintComponent(final Graphics g) {
			if (this.hover) { // draw fill
				g.setColor(JSearchPanel.this.m_cancelColor);
				for (int i = 0; (i + 3) < this.fill.length; i += 4) {
					g.drawLine(this.fill[i], this.fill[i + 1], this.fill[i + 2], this.fill[i + 3]);
				}
			}
			g.setColor(JSearchPanel.this.getForeground());
			for (int i = 0; (i + 3) < this.outline.length; i += 2) {
				g.drawLine(this.outline[i], this.outline[i + 1], this.outline[i + 2], this.outline[i + 3]);
			}
		}

		@Override
		public void mouseClicked(final MouseEvent arg0) {
			JSearchPanel.this.setQuery(null);
		}

		@Override
		public void mousePressed(final MouseEvent arg0) {
		}

		@Override
		public void mouseReleased(final MouseEvent arg0) {
		}

		@Override
		public void mouseEntered(final MouseEvent arg0) {
			this.hover = true;
			this.repaint();
		}

		@Override
		public void mouseExited(final MouseEvent arg0) {
			this.hover = false;
			this.repaint();
		}

	} // end of class CancelButton

} // end of class JSearchPanel
