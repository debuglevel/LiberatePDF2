package de.debuglevel.liberatepdf2.javafx

import de.debuglevel.liberatepdf2.restclient.apis.ConfigurationApi
import de.debuglevel.liberatepdf2.restclient.apis.DocumentsApi
import de.debuglevel.liberatepdf2.restclient.apis.StatusApi
import de.debuglevel.liberatepdf2.restclient.apis.TransformationsApi
import javafx.animation.Animation
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.input.DragEvent
import javafx.scene.input.TransferMode
import javafx.util.Duration
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.concurrent.thread

class DropWindowController {
    private val logger = LoggerFactory.getLogger(DropWindowController::class.java)

    @FXML
    private var filesListView: ListView<TransferFile>? = null

    @FXML
    private var passwordTextField: TextField? = null

    private val transferFiles: ObservableList<TransferFile> = FXCollections.observableList(ArrayList())

    private val HOST = "http://localhost:8081" // TODO: should be configurable

    private val statusApi = StatusApi(HOST)
    private val configurationApi = ConfigurationApi(HOST)
    private val transformationsApi = TransformationsApi(HOST)
    private val documentsApi = DocumentsApi(HOST)

    private val statusChecker: StatusChecker = StatusChecker()
    private val fileUploader: FileUploader = FileUploader()

    @FXML
    fun initialize() {
        filesListView!!.items = transferFiles
        val fileCheckTimer = Timeline(
            KeyFrame(Duration.seconds(1.0), { statusChecker.checkFilesStatus(transferFiles, filesListView!!) })
        )
        fileCheckTimer.cycleCount = Animation.INDEFINITE
        fileCheckTimer.play()
        fileUploader.initializeMaximumUploadSize()
    }

    fun onDragDropped(event: DragEvent) {
        val dragboard = event.dragboard
        var success = false
        if (dragboard.hasFiles()) {
            val files = dragboard.files

            val thread = thread {
                fileUploader.uploadFiles(files.map { it.toPath() }, transferFiles, passwordTextField, filesListView)
            }

            thread.isDaemon = true
            thread.start()
            success = true
        }
        event.isDropCompleted = success
        event.consume()
    }

    fun onDragOver(event: DragEvent) {
        if (event.gestureSource !== filesListView && event.dragboard.hasFiles()) {
            event.acceptTransferModes(*TransferMode.ANY)
        }
        event.consume()
    }
}