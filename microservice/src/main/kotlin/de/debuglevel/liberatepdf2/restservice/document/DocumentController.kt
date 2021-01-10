package de.debuglevel.liberatepdf2.restservice.document

import de.debuglevel.liberatepdf2.restservice.storage.StorageService
import de.debuglevel.liberatepdf2.restservice.storage.ZipService
import de.debuglevel.liberatepdf2.restservice.transformation.TransformationService
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.multipart.CompletedFileUpload
import io.micronaut.http.server.types.files.StreamedFile
import io.micronaut.http.server.types.files.SystemFile
import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging
import java.net.URI
import java.util.*

@Controller("/api/v1/documents")
@Tag(name = "documents")
class DocumentController(
    private val zipService: ZipService,
    private val transformationService: TransformationService,
    private val storageService: StorageService,
) {
    private val logger = KotlinLogging.logger {}

    @Get("/zip{?ids}")
    @Produces("application/zip")
    fun downloadZip(
        ids: Array<UUID>?,
    ): HttpResponse<*> {
        return if (!ids.isNullOrEmpty()) {
            logger.debug { "GET /zip for ${ids.size} documents: ${ids.joinToString()}" }
            val storedFiles = ids.map { storageService.get(it) }
            val zipPath = zipService.createZip(storedFiles)
            val systemFile = SystemFile(zipPath.toFile(), MediaType.of("application/zip"))

            HttpResponse.ok(systemFile.attach("unrestricted PDFs.zip"))
        } else {
            logger.warn { "GET /zip with missing ids parameter" }
            HttpResponse.badRequest("ids parameter must be supplied")
        }
    }

    @Get("/{documentId}")
    fun getOne(
        documentId: UUID,
    ): HttpResponse<*> {
        logger.debug { "GET / or HEAD / for document id=$documentId" }

        // TODO: API v2 should just return a 404 if unrestricted file is not ready, and download it if ready.
        //  Status check then has to be done via /transformation/ endpoint.

        return try {
            val transformation = transformationService.get(documentId)

            if (!transformation.finished) {
                // the request exists, but was not transformed by now
                logger.debug { "Transformation with id=${documentId} found, but transformation.finished=false (not processed by now)" }
                HttpResponse.status<String>(HttpStatus.PROCESSING)
                    .body("The transformation was not processed by now. Please try again later.")
            } else if (transformation.failed!!) {
                // the request exists, but transformation failed
                logger.debug { "Transformation with id=${documentId} found, but transformation failed." }
                HttpResponse.status<String>(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("The transformation failed: ${transformation.errorMessage}")
            } else {
                val storedFile = storageService.get(transformation.unrestrictedStoredFileId!!)
                val streamedFile = StreamedFile(storedFile.inputStream, MediaType.of("application/pdf"))
                HttpResponse.ok(streamedFile.attach(storedFile.filename))
            }
        } catch (e: TransformationService.NotFoundException) {
            logger.debug { "Transformation with id=${documentId} not found." }
            HttpResponse.notFound("Transformation with id=${documentId} not found")
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
}