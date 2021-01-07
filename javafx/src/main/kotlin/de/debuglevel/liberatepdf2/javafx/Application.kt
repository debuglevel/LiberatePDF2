package de.debuglevel.liberatepdf2.javafx

import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import org.slf4j.LoggerFactory

class Application : javafx.application.Application() {
    private val logger = LoggerFactory.getLogger(Application::class.java)

    override fun start(stage: Stage) {
        logger.info("Starting LiberatePDF2 JavaFX client...")
        val fxmlFile = "/fxml/DropWindow.fxml"

        logger.debug("Loading FXML for main view from: {}...", fxmlFile)
        val loader = FXMLLoader()
        val rootNode = loader.load<Parent>(this.javaClass.getResourceAsStream(fxmlFile))

        logger.debug("Showing JFX scene...")
        val scene = Scene(rootNode, 500.0, 500.0)
        scene.stylesheets.add("/styles/styles.css")
        stage.title = "LiberatePDF2 JavaFX client"
        stage.scene = scene
        stage.show()
    }
}
