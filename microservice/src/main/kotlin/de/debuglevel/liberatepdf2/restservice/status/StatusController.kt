package de.debuglevel.liberatepdf2.restservice.status

import de.debuglevel.liberatepdf2.restservice.configuration.ConfigurationService
import de.debuglevel.liberatepdf2.restservice.restrictionsremover.RestrictionsRemoverService
import de.debuglevel.liberatepdf2.restservice.storage.StorageService
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging

@Controller("/v1/status")
@Tag(name = "status")
class StatusController(
    private val storageService: StorageService,
    private val restrictionsRemoverService: RestrictionsRemoverService,
    private val configurationService: ConfigurationService,
) {
    private val logger = KotlinLogging.logger {}

    @Deprecated("superseded by /v1/configuration/")
    @Get("/maximum-upload-size")
    fun maximumUploadSize(): Long {
        logger.debug { "Received GET request for maximum-upload-size" }
        return configurationService.maximumMultipartUploadSize
    }

    @Get("/ping")
    fun ping(): String {
        logger.debug { "Received GET request for ping" }
        return "ok"
    }

    @Get("/statistics")
    fun statistics(): GetStatisticResponse {
        return GetStatisticResponse(
            storageService.storedItemsCount,
            restrictionsRemoverService.itemsCount,
            restrictionsRemoverService.failedItemsCount,
            restrictionsRemoverService.successfulItemsCount,
        )
    }
}