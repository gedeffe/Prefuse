package prefuse.demos;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import javafx.application.Application;
import javafx.embed.swing.SwingNode;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public abstract class AbstractJavaFxDemosContainer extends Application {

	@Override
	public void start(final Stage primaryStage) {
		primaryStage.setTitle(this.getTitle());
		final SwingNode swingNode = new SwingNode();
		SwingUtilities.invokeLater(() -> {
			swingNode.setContent(AbstractJavaFxDemosContainer.this.getContent());
		});
		final StackPane root = new StackPane();
		root.getChildren().add(swingNode);
		primaryStage.setScene(new Scene(root));
		primaryStage.sizeToScene();
		primaryStage.show();
	}

	/**
	 * Provides a title to main frame. Should be not null !
	 *
	 * @return title to display.
	 */
	protected abstract String getTitle();

	/**
	 * Provides a Prefuse component to include into this JavaFX frame. Should be
	 * not null !
	 *
	 * @return instance of Swing component to display.
	 */
	protected abstract JComponent getContent();
}
