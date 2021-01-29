package de.debuglevel.liberatepdf2.restservice.configuration

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging

@Controller("/v1/configuration")
@Tag(name = "configuration")
class ConfigurationController(
    private val configurationService: ConfigurationService,
) {
    private val logger = KotlinLogging.logger {}

    @Get("/")
    fun getConfiguration(): GetConfigurationResponse {
        logger.debug { "Received GET request for /" }

        return GetConfigurationResponse(
            maximumMultipartFileSize = configurationService.maximumMultipartFileSize,
            maximumRequestSize = configurationService.maximumRequestSize,
            maximumMultipartUploadSize = configurationService.maximumMultipartUploadSize,
            multipartEnabled = configurationService.multipartEnabled,
        )
    }
}