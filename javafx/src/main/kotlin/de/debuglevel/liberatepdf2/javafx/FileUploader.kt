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
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
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
        logger.info("Uploading file $path...")
        val transferFile = TransferFile(path, "uploading", passwordTextField!!.text)
        transferFiles.add(transferFile)

        if (!isFileSizeAccepted(path)) {
            transferFile.status = "file too big; did not upload"
            return
        }

        val uploadTask = object : Task<UUID>() {
            override fun call(): UUID {
                logger.debug("Uploading file $path...")
                val postTransformationResponse = transformationsApi.postOneTransformation(
                    path.toFile(),
                    transferFile.password
                )
                logger.debug("Uploaded file $path: ${postTransformationResponse.id!!}")
                return postTransformationResponse.id
            }
        }

        uploadTask.setOnSucceeded {
            logger.debug("Uploading task for $path succeeded; got id=${uploadTask.get()}")
            transferFile.id = uploadTask.get()
            transferFile.status = "transferred"

            filesListView!!.refresh() // should be better be done via an property. but works good enough.
        }

        uploadTask.setOnFailed {
            logger.warn("Uploading task for $path failed")
            transferFile.status = "upload failed"

            filesListView!!.refresh() // should be better be done via an property. but works good enough.
        }

        logger.debug("Queueing uploading task for $path...")
        uploadTaskExecutor.submit(uploadTask)
        logger.debug("Queued uploading task for $path")
    }

    fun uploadFiles(
        paths: List<Path>,
        transferFiles: ObservableList<TransferFile>,
        passwordTextField: TextField?,
        filesListView: ListView<TransferFile>?
    ) {
        logger.debug("Uploading ${paths.size} files...")

        for (path in paths) {
            if (Files.isDirectory(path)) {
                logger.debug("$path is a directory")

                val extensions = arrayOf("pdf", "PDF") // CAVEAT: filter is NOT case insensitive
                val files = FileUtils.listFiles(path.toFile(), extensions, true)
                logger.debug("Found ${files.size} files in directory $path")

                files.forEach { uploadFile(it.toPath(), transferFiles, passwordTextField, filesListView) }
            } else if (Files.isRegularFile(path) && Files.isReadable(path)) {
                logger.debug("$path is a regular and readable file")
                uploadFile(path, transferFiles, passwordTextField, filesListView)
            } else {
                logger.warn("strange case")
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
        logger.debug("Checking if file size is accepted by service...")
        return when (maximumUploadSize) {
            null -> true
            else -> Files.size(path) < maximumUploadSize!!
        }
    }

    fun initializeMaximumUploadSize() {
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