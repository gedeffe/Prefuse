package prefuse.demos;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import prefuse.Visualization;
import prefuse.controls.ControlAdapter;
import prefuse.data.Tree;
import prefuse.data.io.TreeMLReader;
import prefuse.util.ui.SearchPanelControl;
import prefuse.visual.VisualItem;

public class TreeViewController {

    @FXML
    Label titleLabel;
    @FXML
    BorderPane mainBorderPane;
    @FXML
    HBox bottomBox;

    public void initialize() {
        final String label = "name";

        final TreeView treeView = this.getTreeView(label);
        this.mainBorderPane.setCenter(treeView);

        treeView.addControlListener(new ControlAdapter() {
            @Override
            public void itemEntered(final VisualItem item, final MouseEvent e) {
                if (item.canGetString(label)) {
                    Platform.runLater(() -> {
                        TreeViewController.this.titleLabel.setText(item.getString(label));
                    });
                }
            }

            @Override
            public void itemExited(final VisualItem item, final MouseEvent e) {
                Platform.runLater(() -> {
                    TreeViewController.this.titleLabel.setText(null);
                });
            }
        });

        // create a search panel for the tree map
        final SearchPanelControl searchPanelControl = new SearchPanelControl(treeView.getVisualization(),
                TreeView.treeNodes, Visualization.SEARCH_ITEMS, label, true, true);
        this.bottomBox.getChildren().add(searchPanelControl);

        // force repaint
        treeView.repaint();
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
        return treeView;
    }
}
