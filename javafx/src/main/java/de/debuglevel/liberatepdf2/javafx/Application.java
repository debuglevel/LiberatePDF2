package de.debuglevel.liberatepdf2.javafx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application extends javafx.application.Application {
	private static final Logger logger = LoggerFactory.getLogger(Application.class);

	public static void main(final String[] args) throws Exception {
		launch(args);
	}

	@Override
	public void start(final Stage stage) throws Exception {
		logger.info("Starting LiberatePDF2 JavaFX client...");

		final String fxmlFile = "/fxml/DropWindow.fxml";
		logger.debug("Loading FXML for main view from: {}...", fxmlFile);
		final FXMLLoader loader = new FXMLLoader();
		final Parent rootNode = loader.load(this.getClass().getResourceAsStream(fxmlFile));

		logger.debug("Showing JFX scene...");
		final Scene scene = new Scene(rootNode, 500, 500);
		scene.getStylesheets().add("/styles/styles.css");

		stage.setTitle("LiberatePDF2 JavaFX client");
		stage.setScene(scene);
		stage.show();
	}
}
