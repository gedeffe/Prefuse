package prefuse.demos;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Demonstration of a node-link tree viewer inside javaFX. We will iterate to
 * remove gradually Swing components.
 *
 */
public class JavaFxTreeView extends Application {

	public static void main(final String[] args) {
		launch(args);
	}

	@Override
	public void start(final Stage primaryStage) throws IOException {
		primaryStage.setTitle(this.getTitle());

		final BorderPane root = new BorderPane();

		final Parent parent = FXMLLoader.load(this.getClass().getResource("TreeView.fxml"));
		root.setCenter(parent);

		primaryStage.setScene(new Scene(root, 1024, 768));
		primaryStage.show();
	}

	private String getTitle() {
		return "p r e f u s e  |  t r e e v i e w";
	}

}
