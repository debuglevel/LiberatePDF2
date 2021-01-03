package de.debuglevel.liberatepdf2.restservice.status

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging
import de.debuglevel.liberatepdf2.restservice.restrictionsremover.RestrictionsRemoverService
import de.debuglevel.liberatepdf2.restservice.storage.StorageService

@Controller("/api/v1/status")
@Tag(name = "status")
class StatusController(
    private val storageService: StorageService,
    private val restrictionsRemoverService: RestrictionsRemoverService,
) {
    private val logger = KotlinLogging.logger {}

    @Get("/maximum-upload-size")
    fun maximumUploadSize(): Long {
        logger.debug { "Received GET request for maximum-upload-size" }

        //TODO("no yet implemented, if there is a limit in micronaut at all")
        return 100000 // TODO: fake.
    }

    @Get("/ping")
    fun ping(): String {
        logger.debug { "Received GET request for ping" }
        return "ok"
    }

    @Get("/statistics")
    fun statistics(): GetStatisticResponse {
        return GetStatisticResponse(
            storageService.itemsCount,
            restrictionsRemoverService.itemsCount,
            restrictionsRemoverService.failedItemsCount,
            restrictionsRemoverService.successfulItemsCount,
        )
    }
}