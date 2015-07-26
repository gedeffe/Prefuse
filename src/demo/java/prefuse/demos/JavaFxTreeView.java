package prefuse.demos;

import javax.swing.JComponent;

/**
 * Demonstration of a node-link tree viewer inside javaFX. We will iterate to
 * remove gradually Swing components.
 *
 */
public class JavaFxTreeView extends AbstractJavaFxDemosContainer {

	public static void main(final String[] args) {
		launch(args);
	}

	@Override
	protected String getTitle() {
		return "p r e f u s e  |  t r e e v i e w";
	}

	@Override
	protected JComponent getContent() {
		final String infile = TreeView.TREE_CHI;
		final String label = "name";
		final JComponent treeview = TreeView.demo(infile, label);
		return treeview;
	}

}
