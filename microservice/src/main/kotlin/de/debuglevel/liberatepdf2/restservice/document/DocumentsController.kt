package de.debuglevel.liberatepdf2.restservice.document

import de.debuglevel.liberatepdf2.restservice.restrictionsremover.RestrictionsRemoverService
import de.debuglevel.liberatepdf2.restservice.storage.StorageService
import de.debuglevel.liberatepdf2.restservice.storage.ZipService
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.multipart.CompletedFileUpload
import io.micronaut.http.server.types.files.SystemFile
import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging
import java.net.URI
import java.nio.file.Files
import java.util.*
import java.util.concurrent.Executors

@Controller("/api/v1/documents")
@Tag(name = "documents")
class DocumentsController(
    private val storageService: StorageService,
    private val restrictionsRemoverService: RestrictionsRemoverService,
    private val zipService: ZipService,
    @Property(name = "app.liberatepdf2.transformation.worker-threads") workerThreadsCount: Int,
) {
    private val logger = KotlinLogging.logger {}

    private val executor = Executors.newFixedThreadPool(workerThreadsCount)

    @Get("/zip{?id}")
    fun downloadZip(
        id: Array<UUID>?,
    ): HttpResponse<*> {
        return if (!id.isNullOrEmpty()) {
            logger.debug { "GET /zip for ${id.size} documents ${id.joinToString()}" }
            val zip = zipService.createZip(id)
            val filesystemResource = zip.toFile()

            HttpResponse.ok(SystemFile(filesystemResource).attach("unrestricted PDFs.zip"))
        } else {
            logger.warn { "GET /zip with missing id parameter" }
            HttpResponse.badRequest("id parameter must be supplied")
        }
    }

    @Get("/{documentId}")
    fun getOne(
        documentId: UUID,
    ): HttpResponse<*> {
        logger.debug { "GET / or HEAD / for document id=$documentId" }

        val pdf = storageService.getItem(documentId)
        return if (pdf == null) {
            // no item found with this ID (because no request was assigned this ID by now)
            logger.debug { "No document with id=$documentId found" }

            HttpResponse.notFound("No document found for id=$documentId")
        } else if (!pdf.done) {
            // the request exists, but was not transformed by now
            logger.debug { "Document with id=${documentId} found, but pdf.isDone=false (not processed by now)" }
            HttpResponse.status<Any>(HttpStatus.PROCESSING) // .status(HTTP_STATUS_IN_PROGRESS) // CAVEAT/TODO
                .body("The document was not processed by now. Please try again later.")
        } else if (pdf.failed == true) {
            // the request exists, but transformation failed
            logger.debug { "Document with id=${documentId} found, but transformation failed" }
            HttpResponse.status<Any>(HttpStatus.INTERNAL_SERVER_ERROR) // .status(HTTP_STATUS_FAILED) // CAVEAT/TODO
                .body("The document transformation failed: ${pdf.error}")
        } else if (!Files.exists(pdf.unrestrictedPath)) {
            // the request was transformed, but the file does not exist (somehow failed?)
            logger.debug { "Document with id=${documentId} found, but no file exists" }
            HttpResponse.status<String>(HttpStatus.INTERNAL_SERVER_ERROR) // .status(HTTP_STATUS_FAILED) // CAVEAT/TODO
                .body("The document was processed, but produced no result. Maybe the password was wrong or another error occurred.")
        } else {
            // request should be okay
            val filesystemResource = pdf.unrestrictedPath!!.toFile()
            logger.debug { "Document with id=${documentId} found and set for delivery" }

            // TODO: rename "foo.pdf" to "foo.unrestricted.pdf" or the like
            val filename = storageService.getItem(documentId)!!.originalFilename
            HttpResponse.ok(SystemFile(filesystemResource).attach(filename))

//            HttpResponse.ok().contentType(MediaType.APPLICATION_PDF)
//                .header("Content-Disposition", "attachment; filename=\"$filename\"")
//                .body(filesystemResource)
        }
    }

    @Post("/")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    fun postOne(
        file: CompletedFileUpload,
        password: String,
    ): HttpResponse<String> {
        logger.debug { "POST / with file=$file, password=$password" }

        val pdf = storageService.store(file.filename, file.inputStream, password)
        pdf.password = password

        executor.submit { restrictionsRemoverService.removeRestrictions(pdf) }

        val uri = URI("/documents/${pdf.id}")
        logger.debug { "Returning PDF id=$pdf.id" }
        return HttpResponse.accepted<String>(uri).body(pdf.id.toString())
    }

    companion object {
        private const val HTTP_STATUS_FAILED = 560
        private const val HTTP_STATUS_IN_PROGRESS = 260
    }
}