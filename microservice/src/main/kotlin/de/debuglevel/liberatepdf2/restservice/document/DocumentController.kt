package de.debuglevel.liberatepdf2.restservice.document

import de.debuglevel.liberatepdf2.restservice.storage.ZipService
import de.debuglevel.liberatepdf2.restservice.transformation.TransformationService
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.multipart.CompletedFileUpload
import io.micronaut.http.server.types.files.StreamedFile
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging
import java.io.ByteArrayOutputStream
import java.net.URI
import java.util.*

@Controller("/v1/documents")
@Tag(name = "documents")
class DocumentController(
    private val zipService: ZipService,
    private val transformationService: TransformationService,
) {
    private val logger = KotlinLogging.logger {}

    @Get("/zip{?ids}")
    @Produces("application/zip")
    @ApiResponse(
        responseCode = "200",
        description = "Download of the ZIP with the given IDs",
        content = [Content(
            mediaType = MediaType.APPLICATION_OCTET_STREAM,
            schema = Schema(type = "string", format = "binary")
        )]
    )
    fun downloadZip(
        ids: Array<UUID>?,
    ): HttpResponse<StreamedFile> {
        return if (!ids.isNullOrEmpty()) {
            logger.debug { "GET /zip for ${ids.size} documents: ${ids.joinToString()}" }

            val zipItems = ids.map {
                val transformation = transformationService.get(it)
                ZipService.ZipItem(
                    // Include ID to get unique filenames
                    "${transformation.originalFilename}.unrestricted.${transformation.id}.pdf",
                    transformation.unrestrictedFile!!.inputStream()
                )
            }

            val byteArrayOutputStream = ByteArrayOutputStream()
            zipService.writeZip(zipItems.toSet(), byteArrayOutputStream)
            val byteArrayInputStream = byteArrayOutputStream.toByteArray().inputStream()

            val streamedFile = StreamedFile(byteArrayInputStream, MediaType.of("application/zip"))

            HttpResponse.ok(streamedFile.attach("unrestricted PDFs.zip"))
        } else {
            logger.warn { "GET /zip with missing ids parameter" }
            HttpResponse.badRequest()
        }
    }

    @Produces("application/pdf")
    @Get("/{documentId}")
    @ApiResponse(responseCode = "200", description = "Download of the document", content = [Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = Schema(type = "string", format = "binary"))])
    fun getOne(
        documentId: UUID,
    ): HttpResponse<StreamedFile> {
        logger.debug { "GET / or HEAD / for document id=$documentId" }

        // TODO: API v2 should just return a 404 if unrestricted file is not ready, and download it if ready.
        //  Status check then has to be done via /transformation/ endpoint.

        return try {
            val transformation = transformationService.get(documentId)

            if (!transformation.finished) {
                // the request exists, but was not transformed by now
                logger.debug { "Transformation with id=${documentId} found, but transformation.finished=false (not processed by now)" }
                HttpResponse.status(HttpStatus.PROCESSING)
            } else if (transformation.failed!!) {
                // the request exists, but transformation failed
                logger.debug { "Transformation with id=${documentId} found, but transformation failed." }
                HttpResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
            } else {
                val streamedFile =
                    StreamedFile(transformation.unrestrictedFile!!.inputStream(), MediaType.of("application/pdf"))
                val originalFilename = "${transformation.originalFilename}.unrestricted.pdf"
                HttpResponse.ok(streamedFile.attach(originalFilename))
            }
        } catch (e: TransformationService.ItemNotFoundException) {
            logger.debug { "Transformation with id=${documentId} not found." }
            HttpResponse.notFound()
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