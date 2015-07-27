package prefuse.util.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

/**
 * A rounded button depicted as an "X" that allows users to cancel the current
 * query and clear the query field. It has a fixed size, and an action handler
 * should be provided to catch action events.
 *
 * Freely copied from https://gist.github.com/jewelsea/3383311
 */
public class JfxCancelButton extends StackPane {
	final double BUTTON_SIZE = 20;
	final double UPPER_CORNER = 6;
	final double LOWER_CORNER = 14;

	public JfxCancelButton(final EventHandler<ActionEvent> actionHandler) {
		super();

		this.getStylesheets().add(this.getClass().getResource("cancelbutton.css").toExternalForm());

		this.getStyleClass().add("cancelbutton");
		final Button button = new Button();
		// prevent the widget from getting the keyboard focus
		button.setFocusTraversable(false);
		// add callbacks
		button.setOnAction(actionHandler);

		this.getChildren().addAll(button, this.pathCancelIcon());
		button.setMinSize(this.BUTTON_SIZE, this.BUTTON_SIZE);
		this.setPrefSize(this.BUTTON_SIZE, this.BUTTON_SIZE);
		this.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

	}

	private Node pathCancelIcon() {
		final Path path = new Path();
		path.getElements().addAll(new MoveTo(this.UPPER_CORNER, this.UPPER_CORNER),
				new LineTo(this.LOWER_CORNER, this.LOWER_CORNER), new MoveTo(this.UPPER_CORNER, this.LOWER_CORNER),
				new LineTo(this.LOWER_CORNER, this.UPPER_CORNER));
		path.getStyleClass().add("cancelicon");
		path.setMouseTransparent(true);
		return path;
	}
}
