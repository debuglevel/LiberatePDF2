package de.debuglevel.liberatepdf2.javafx

import de.debuglevel.liberatepdf2.client.apis.StatusApi
import javafx.animation.Animation
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.concurrent.Task
import javafx.fxml.FXML
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.input.DragEvent
import javafx.scene.input.TransferMode
import javafx.util.Duration
import org.apache.commons.io.FileUtils
import org.apache.http.HttpVersion
import org.apache.http.client.fluent.Request
import org.apache.http.conn.HttpHostConnectException
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.InputStream
import java.net.SocketException
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class DropWindowController {
    private val logger = LoggerFactory.getLogger(DropWindowController::class.java)

    @FXML
    private var filesListView: ListView<TransferFile>? = null

    @FXML
    private var passwordTextField: TextField? = null

    private val checkTaskExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private var maximumUploadSize: Long? = null
    private val transferFiles: ObservableList<TransferFile> = FXCollections.observableList(ArrayList())
    private val uploadTaskExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private val HOST = "http://localhost:8080" // TODO: should be configurable

    private val statusApi = StatusApi(HOST)

    private fun checkFilesStatus() {
        logger.debug("Checking status of files...")
        transferFiles.stream()
            .filter { !it.done && it.id != null }
            .forEach {
                checkTaskExecutor.submit(object : Task<Void>() {
                    @Throws(Exception::class)
                    override fun call(): Void? {
                        // check again if pre-conditions for checking status are still met.
                        // (although conditions were filtered, the queue MIGHT contain duplicates
                        // if checking the whole queue lasts longer than the checking interval?)
                        if (!it.done && it.id != null) {
                            checkFileStatus(it)
                        }

                        return null
                    }
                })
            }
    }

    private fun checkFileStatus(transferFile: TransferFile) {
        try {
            val url = "$HOST/v1/documents/${transferFile.id}"
            logger.debug("Checking status of file $transferFile via $url")
            val response = Request.Get(url).execute().returnResponse()
            val statusCode = response.statusLine.statusCode

            logger.debug("Got status code $statusCode for $transferFile")
            when (statusCode) {
                200 -> {
                    // file was successfully processed
                    transferFile.status = "done"
                    transferFile.done = true
                    saveFile(transferFile, response.entity.content)
                }
                102 -> {
                    // file is still in progress
                    transferFile.status = "in progress"
                }
                500 -> {
                    // processing failed
                    transferFile.status = "processing failed"
                    transferFile.done = true
                }
                else -> {
                    // unknown
                    transferFile.status = "unknown status code"
                }
            }
            filesListView!!.refresh() // should be better be done via an property. but works good enough.
        } catch (e: HttpHostConnectException) {
            logger.error("Connection to host failed: " + e.message)
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

    private fun fetchMaximumUploadSize() {
        try {
            logger.debug("Querying service for maximum upload size...")
            maximumUploadSize = statusApi.maximumUploadSize()
            logger.debug("Maximum upload size is '$maximumUploadSize'")
        } catch (e: HttpHostConnectException) {
            logger.error("Connection to host failed: " + e.message)
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

    @FXML
    fun initialize() {
        filesListView!!.items = transferFiles
        val fileCheckTimer = Timeline(
            KeyFrame(Duration.seconds(1.0), { checkFilesStatus() })
        )
        fileCheckTimer.cycleCount = Animation.INDEFINITE
        fileCheckTimer.play()
        fetchMaximumUploadSize()
    }

    /**
     * Checks if the size of the given file meets the restrictions of the server
     *
     * @return true if file size is okay (or unknown), false if file is too big
     */
    private fun isFileSizeAccepted(path: Path): Boolean {
        return when (maximumUploadSize) {
            null -> true
            else -> Files.size(path) < maximumUploadSize!!
        }
    }

    fun onDragDropped(event: DragEvent) {
        val dragboard = event.dragboard
        var success = false
        if (dragboard.hasFiles()) {
            val files = dragboard.files

            val thread = thread {
                processFiles(files.map { it.toPath() })
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

    private fun processFile(path: Path) {
        logger.info("Processing file $path")
        val transferFile = TransferFile(path, "uploading", passwordTextField!!.text)
        transferFiles.add(transferFile)

        if (isFileSizeAccepted(path)) {
            val uploadTask = object : Task<String>() {
                override fun call(): String {
                    logger.debug("Building POST request for $path...")
                    val entity = MultipartEntityBuilder.create()
                        .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                        .setCharset(Charset.defaultCharset())
                        .addBinaryBody("file", path.toFile())
                        .addTextBody("password", transferFile.password).build()

                    logger.debug("Sending POST request for $path...")
                    return Request.Post("$HOST/v1/documents/")
                        .useExpectContinue()
                        .version(HttpVersion.HTTP_1_1)
                        .body(entity)
                        .execute()
                        .returnContent()
                        .asString()
                }
            }

            uploadTask.setOnSucceeded {
                logger.debug("Upload Task for $path succeeded; got id=${uploadTask.get()}")
                transferFile.id = uploadTask.get()
                transferFile.status = "transferred"

                filesListView!!.refresh() // should be better be done via an property. but works good enough.
            }

            uploadTask.setOnFailed {
                logger.warn("Upload Task for $path failed")
                transferFile.status = "upload failed"
                if (uploadTask.exceptionProperty().get() is SocketException) {
                    logger.warn("Exception is SocketException; the file MIGHT be too big.")
                    uploadTask.exceptionProperty().get().printStackTrace()

                    transferFile.status = "upload failed (too big?)"
                }

                filesListView!!.refresh() // should be better be done via an property. but works good enough.
            }

            logger.debug("Queueing upload task for $path...")
            uploadTaskExecutor.submit(uploadTask)
        } else {
            transferFile.status = "file too big; did not upload"
        }
    }

    private fun processFiles(paths: List<Path>) {
        logger.debug("Processing ${paths.size} files...")

        for (path in paths) {
            if (Files.isDirectory(path)) {
                logger.debug("$path is a directory")

                val extensions =
                    arrayOf("pdf", "PDF") // TODO: don't know if this is needed or filter is case insensitive
                val files = FileUtils.listFiles(path.toFile(), extensions, true)
                logger.debug("Found ${files.size} files in directory $path")

                files.forEach { processFile(it.toPath()) }
            } else if (Files.isRegularFile(path) && Files.isReadable(path)) {
                logger.debug("$path is a regular and readable file")
                processFile(path)
            } else {
                // do nothing because strange
            }
        }
    }

    private fun saveFile(transferFile: TransferFile, inputStream: InputStream) {
        try {
            val originalPath = transferFile.path
            val destinationFile = originalPath.resolveSibling("${originalPath.fileName} (unrestricted).pdf")
                .toFile()

            logger.debug("Copying $transferFile to $destinationFile...")
            FileUtils.copyInputStreamToFile(inputStream, destinationFile)
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }
}