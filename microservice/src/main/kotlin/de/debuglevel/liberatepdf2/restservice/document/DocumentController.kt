package de.debuglevel.liberatepdf2.restservice.document

import de.debuglevel.liberatepdf2.restservice.storage.ZipService
import de.debuglevel.liberatepdf2.restservice.transformation.TransformationService
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.multipart.CompletedFileUpload
import io.micronaut.http.server.types.files.SystemFile
import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging
import java.net.URI
import java.util.*

@Controller("/api/v1/documents")
@Tag(name = "documents")
class DocumentController(
    private val zipService: ZipService,
    private val documentService: DocumentService,
    private val transformationService: TransformationService,
) {
    private val logger = KotlinLogging.logger {}

    @Get("/zip{?ids}")
    @Produces("application/zip")
    fun downloadZip(
        ids: Array<UUID>?,
    ): HttpResponse<*> {
        return if (!ids.isNullOrEmpty()) {
            logger.debug { "GET /zip for ${ids.size} documents ${ids.joinToString()}" }
            val zip = zipService.createZip(ids)
            val systemFile = SystemFile(zip.toFile(), MediaType.of("application/zip"))

            HttpResponse.ok(systemFile.attach("unrestricted PDFs.zip"))
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

        return try {
            val pdf = documentService.get(documentId)

            // if found but failed:
            if (!pdf.done) {
                // the request exists, but was not transformed by now
                logger.debug { "Transformation with id=${documentId} found, but pdf.done=false (not processed by now)" }
                HttpResponse.status<String>(HttpStatus.PROCESSING)
                    .body("The transformation was not processed by now. Please try again later.")
            } else if (pdf.failed!!) {
                // the request exists, but transformation failed
                logger.debug { "Transformation with id=${documentId} found, but transformation failed." }
                HttpResponse.status<String>(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("The transformation failed: ${pdf.error}")
            } else {
                val file = pdf.unrestrictedPath!!.toFile()
                val filename = pdf.originalFilename // TODO: rename "foo.pdf" to "foo.unrestricted.pdf" or the like
                HttpResponse.ok(SystemFile(file).attach(filename))
            }
        } catch (e: DocumentService.NotFoundException) {
            // XXX: detailed processing only for compatibility; should actually be checked via /transformations/
            // this only catches if thing was not found in storage; but found and failed should be handled above
            try {
                val transformation = transformationService.get(documentId)

                if (!transformation.finished) {
                    // the request exists, but was not transformed by now
                    logger.debug { "Transformation with id=${documentId} found, but transformation.finished=false (not processed by now)" }
                    HttpResponse.status<String>(HttpStatus.PROCESSING)
                        .body("The transformation was not processed by now. Please try again later.")
                } else if (transformation.failed == true) {
                    // the request exists, but transformation failed
                    logger.debug { "Transformation with id=${documentId} found, but transformation failed." }
                    HttpResponse.status<String>(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("The transformation failed: ${transformation.errorMessage}")
                } else {
                    logger.debug { "Transformation with id=${documentId} found, unknown error." }
                    HttpResponse.status<String>(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("The transformation failed: ${transformation.errorMessage}")
                }
            } catch (e: TransformationService.NotFoundException) {
                logger.debug { "Transformation with id=${documentId} not found." }
                HttpResponse.notFound("The transformation failed: ${e.message}")
            }
        }
    }

    @Post("/")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    @Deprecated("use /v1/transformations/ instead")
    fun postOne(
        file: CompletedFileUpload,
        password: String,
    ): HttpResponse<String> {
        logger.debug { "POST / with file=$file, password=$password" }

        val transformation = transformationService.add(
            file.filename,
            file.inputStream,
            password
        )

        val uri = URI("/documents/${transformation.id}")
        logger.debug { "Returning PDF id=${transformation.id}" }
        return HttpResponse.accepted<String>(uri).body(transformation.id.toString())
    }

    companion object {
        private const val HTTP_STATUS_FAILED = 560
        private const val HTTP_STATUS_IN_PROGRESS = 260
    }
}