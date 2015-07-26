package prefuse.demos;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import prefuse.Visualization;
import prefuse.controls.ControlAdapter;
import prefuse.data.Tree;
import prefuse.data.io.TreeMLReader;
import prefuse.util.ui.JfxSearchPanel;
import prefuse.visual.VisualItem;

/**
 * Demonstration of a node-link tree viewer inside javaFX. We will iterate to
 * remove gradually Swing components.
 *
 */
public class JavaFxTreeView extends Application {

	private static final Color BACKGROUND = Color.WHITE;
	private static final Color FOREGROUND = Color.BLACK;

	public static void main(final String[] args) {
		launch(args);
	}

	@Override
	public void start(final Stage primaryStage) {
		primaryStage.setTitle(this.getTitle());

		final String label = "name";

		final TreeView treeView = this.getTreeView(label);

		final SwingNode swingNode = new SwingNode();
		SwingUtilities.invokeLater(() -> {
			swingNode.setContent(this.getContent(label, treeView));
		});
		final BorderPane root = new BorderPane();
		// use css to default styling
		root.getStylesheets().add(this.getClass().getResource("treeview.css").toExternalForm());

		root.setCenter(swingNode);
		final Node box = this.getStatusBar(label, treeView);
		root.setBottom(box);
		primaryStage.setScene(new Scene(root));
		primaryStage.sizeToScene();
		primaryStage.show();
	}

	private TreeView getTreeView(final String label) {
		Tree t = null;
		try {
			t = (Tree) new TreeMLReader().readGraph(TreeView.TREE_CHI);
		} catch (final Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		final TreeView treeView = new TreeView(t, label);
		treeView.setBackground(BACKGROUND);
		treeView.setForeground(FOREGROUND);
		return treeView;
	}

	private Node getStatusBar(final String label, final TreeView treeView) {
		// create a search panel for the tree map
		final JfxSearchPanel search = new JfxSearchPanel(treeView.getVisualization(), TreeView.treeNodes,
				Visualization.SEARCH_ITEMS, label, true, true);
		search.setShowResultCount(true);

		final Label title = new Label("                 ");
		title.setPrefSize(350, 20);

		treeView.addControlListener(new ControlAdapter() {
			@Override
			public void itemEntered(final VisualItem item, final MouseEvent e) {
				if (item.canGetString(label)) {
					Platform.runLater(() -> {
						title.setText(item.getString(label));
					});
				}
			}

			@Override
			public void itemExited(final VisualItem item, final MouseEvent e) {
				Platform.runLater(() -> {
					title.setText(null);
				});
			}
		});

		final HBox box = new HBox();
		box.setPadding(new Insets(10));
		HBox.setHgrow(title, Priority.ALWAYS);
		box.getChildren().add(title);
		box.getChildren().add(search);
		return box;
	}

	private String getTitle() {
		return "p r e f u s e  |  t r e e v i e w";
	}

	protected JComponent getContent(final String label, final TreeView treeView) {
		final JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(BACKGROUND);
		panel.setForeground(FOREGROUND);
		panel.add(treeView, BorderLayout.CENTER);
		return panel;
	}

}
