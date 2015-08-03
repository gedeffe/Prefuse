package prefuse.util.display;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import prefuse.Display;
import prefuse.util.io.IOLib;

/**
 * Replace this export dialog with a new one, yet without preview of image
 * content ...
 *
 * Swing ActionListener that reveals a dialog box that allows users to export
 * the current Display view to an image file.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class ExportDisplayAction implements EventHandler<KeyEvent> {

	private final Display display;
	private FileChooser chooser;

	/**
	 * Create a new ExportDisplayAction for the given Display.
	 *
	 * @param display
	 *            the Display to capture
	 */
	public ExportDisplayAction(final Display display) {
		this.display = display;
	}

	private void init() {
		this.chooser = new FileChooser();
		this.chooser.setTitle("Export Prefuse Display...");

		Set<String> seen = new HashSet<String>();
		final String[] fmts = ImageIO.getWriterFormatNames();
		for (int i = 0; i < fmts.length; i++) {
			final String s = fmts[i].toLowerCase();
			if ((s.length() == 3) && !seen.contains(s)) {
				seen.add(s);
				this.chooser.getExtensionFilters()
						.add(new ExtensionFilter(s.toUpperCase() + " Image (*." + s + ")", s));
			}
		}
		seen.clear();
		seen = null;
	}

	/**
	 * Shows the image export dialog and processes the results.
	 */
	@Override
	public void handle(final KeyEvent keyEvent) {
		if (keyEvent.isControlDown() && keyEvent.getCode().equals(KeyCode.E)) {
			// lazy initialization
			if (this.chooser == null) {
				this.init();
			}

			// open image save dialog
			File selectedFile = this.chooser.showSaveDialog(this.display.getScene().getWindow());
			if (selectedFile == null) {
				return;
			}
			final String format = this.chooser.getSelectedExtensionFilter().getExtensions().get(0);
			final String ext = IOLib.getExtension(selectedFile);
			if (!format.equals(ext)) {
				selectedFile = new File(selectedFile.toString() + "." + format);
			}

			final double scale = 1;

			// save image
			boolean success = false;
			try {
				final OutputStream out = new BufferedOutputStream(new FileOutputStream(selectedFile));
				System.out.print("Saving image " + selectedFile.getName() + ", " + format + " format...");
				success = this.display.saveImage(out, format, scale);
				out.flush();
				out.close();
				System.out.println("\tDONE");
			} catch (final Exception e) {
				success = false;
			}
			// show result dialog on failure
			if (!success) {
				final Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("Image Save Error");
				alert.setContentText("Error Saving Image!");
				alert.showAndWait();
			}
		}
	}

} // end of class SaveImageAction
