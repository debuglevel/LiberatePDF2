package de.debuglevel.liberatepdf2.javafx

import de.debuglevel.liberatepdf2.restclient.apis.ConfigurationApi
import de.debuglevel.liberatepdf2.restclient.apis.DocumentsApi
import de.debuglevel.liberatepdf2.restclient.apis.StatusApi
import de.debuglevel.liberatepdf2.restclient.apis.TransformationsApi
import javafx.collections.ObservableList
import javafx.concurrent.Task
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import org.apache.commons.io.FileUtils
import org.apache.http.conn.HttpHostConnectException
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.SocketException
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class FileUploader {
    private val logger = LoggerFactory.getLogger(DropWindowController::class.java)

    private val uploadTaskExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private var maximumUploadSize: Long? = null

    private val HOST = "http://localhost:8081" // TODO: should be configurable

    private val statusApi = StatusApi(HOST)
    private val configurationApi = ConfigurationApi(HOST)
    private val transformationsApi = TransformationsApi(HOST)
    private val documentsApi = DocumentsApi(HOST)

    private fun uploadFile(
        path: Path,
        transferFiles: ObservableList<TransferFile>,
        passwordTextField: TextField?,
        filesListView: ListView<TransferFile>?
    ) {
        logger.info("Processing file $path")
        val transferFile = TransferFile(path, "uploading", passwordTextField!!.text)
        transferFiles.add(transferFile)

        if (isFileSizeAccepted(path)) {
            val uploadTask = object : Task<String>() {
                override fun call(): String {
                    val postTransformationResponse = transformationsApi.postOneTransformation(
                        path.toFile(),
                        transferFile.password
                    )
                    return postTransformationResponse.id.toString()
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

    fun uploadFiles(
        paths: List<Path>,
        transferFiles: ObservableList<TransferFile>,
        passwordTextField: TextField?,
        filesListView: ListView<TransferFile>?
    ) {
        logger.debug("Processing ${paths.size} files...")

        for (path in paths) {
            if (Files.isDirectory(path)) {
                logger.debug("$path is a directory")

                val extensions =
                    arrayOf("pdf", "PDF") // TODO: don't know if this is needed or filter is case insensitive
                val files = FileUtils.listFiles(path.toFile(), extensions, true)
                logger.debug("Found ${files.size} files in directory $path")

                files.forEach { uploadFile(it.toPath(), transferFiles, passwordTextField, filesListView) }
            } else if (Files.isRegularFile(path) && Files.isReadable(path)) {
                logger.debug("$path is a regular and readable file")
                uploadFile(path, transferFiles, passwordTextField, filesListView)
            } else {
                // do nothing because strange
            }
        }
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

    fun fetchMaximumUploadSize() {
        try {
            logger.debug("Querying service for maximum upload size...")
            maximumUploadSize = configurationApi.getConfiguration().maximumMultipartUploadSize
            logger.debug("Maximum upload size is '$maximumUploadSize'")
        } catch (e: HttpHostConnectException) {
            logger.error("Connection to host failed: " + e.message)
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }
}