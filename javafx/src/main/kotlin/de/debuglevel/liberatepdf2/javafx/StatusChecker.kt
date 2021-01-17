package de.debuglevel.liberatepdf2.javafx

import de.debuglevel.liberatepdf2.restclient.apis.ConfigurationApi
import de.debuglevel.liberatepdf2.restclient.apis.DocumentsApi
import de.debuglevel.liberatepdf2.restclient.apis.StatusApi
import de.debuglevel.liberatepdf2.restclient.apis.TransformationsApi
import javafx.collections.ObservableList
import javafx.concurrent.Task
import javafx.scene.control.ListView
import org.apache.commons.io.FileUtils
import org.apache.http.client.fluent.Request
import org.apache.http.conn.HttpHostConnectException
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class StatusChecker {
    private val logger = LoggerFactory.getLogger(DropWindowController::class.java)

    private val checkTaskExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private val HOST = "http://localhost:8081" // TODO: should be configurable

    private val statusApi = StatusApi(HOST)
    private val configurationApi = ConfigurationApi(HOST)
    private val transformationsApi = TransformationsApi(HOST)
    private val documentsApi = DocumentsApi(HOST)

    fun checkFilesStatus(
        transferFiles: ObservableList<TransferFile>,
        filesListView: ListView<TransferFile>
    ) {
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
                            checkFileStatus(it, filesListView)
                        }

                        return null
                    }
                })
            }
    }

    private fun checkFileStatus(
        transferFile: TransferFile,
        filesListView: ListView<TransferFile>
    ) {
        try {
            logger.debug("Checking status of file $transferFile")

            val getTransformationResponse = transformationsApi.getOneTransformation(UUID.fromString(transferFile.id))
            val finished = getTransformationResponse.finished!!
            val failed = getTransformationResponse.failed!!
            val errorMessage = getTransformationResponse.errorMessage

            if (!finished) {
                // file is still in progress
                transferFile.status = "in progress"
            } else if (finished && !failed) {
                // file was successfully processed
                transferFile.status = "done"
                transferFile.done = true
                saveFile(transferFile)
            } else if (finished && failed) {
                // processing failed
                transferFile.status = "processing failed"
                transferFile.done = true
            } else {
                // unknown
                transferFile.status = "unknown status code"
            }

            filesListView.refresh() // should be better be done via an property. but works good enough.
        } catch (e: HttpHostConnectException) {
            logger.error("Connection to host failed: " + e.message)
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

    private fun saveFile(transferFile: TransferFile) {
        // TODO: throws exception
        //val x = documentsApi.getOne(UUID.fromString(transferFile.id))

        val url = "$HOST/v1/documents/${transferFile.id}"
        logger.debug("Getting document $transferFile via $url")
        val response = Request.Get(url).execute().returnResponse()
        val inputStream = response.entity.content

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