package de.debuglevel.liberatepdf2.restservice.transformation

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.multipart.CompletedFileUpload
import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging
import java.net.URI
import java.util.*

@Controller("/v1/transformations")
@Tag(name = "transformations")
class TransformationController(
    private val transformationService: TransformationService,
) {
    private val logger = KotlinLogging.logger {}

    @Get("/{transformationId}")
    fun getOne(
        transformationId: UUID,
    ): HttpResponse<*> {
        logger.debug { "GET / or HEAD / for transformation id=$transformationId" }

        return try {
            val transformation = transformationService.get(transformationId)
            val getTransformationResponse = GetTransformationResponse(transformation)
            logger.debug { "Returning PostTransformationResponse $getTransformationResponse..." }
            HttpResponse.ok(getTransformationResponse)
        } catch (e: TransformationService.NotFoundException) {
            logger.debug { "No transformation with id=$transformationId found" }
            HttpResponse.notFound("No transformation found for id=$transformationId")
        }
    }

    @Post("/")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    fun postOne(
        file: CompletedFileUpload,
        password: String,
    ): HttpResponse<*> {
        logger.debug { "POST / with file=$file, password=$password" }

        val transformation = transformationService.add(
            file.filename,
            file.inputStream,
            password
        )

        val uri = URI("/transformations/${transformation.id}")
        val postTransformationResponse = PostTransformationResponse(transformation)
        logger.debug { "Returning PostTransformationResponse $postTransformationResponse..." }
        return HttpResponse.accepted<PostTransformationResponse>(uri).body(postTransformationResponse)
    }
}