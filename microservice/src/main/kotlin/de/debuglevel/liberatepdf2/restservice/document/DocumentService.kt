package de.debuglevel.liberatepdf2.restservice.document

import de.debuglevel.liberatepdf2.restservice.Pdf
import de.debuglevel.liberatepdf2.restservice.restrictionsremover.RestrictionsRemoverService
import de.debuglevel.liberatepdf2.restservice.storage.StorageService
import de.debuglevel.liberatepdf2.restservice.storage.ZipService
import mu.KotlinLogging
import java.util.*
import javax.inject.Singleton

@Singleton
class DocumentService(
    private val storageService: StorageService,
    private val restrictionsRemoverService: RestrictionsRemoverService,
    private val zipService: ZipService,
) {
    private val logger = KotlinLogging.logger {}

    fun get(documentId: UUID): Pdf {
        logger.debug { "Getting document with id=$documentId..." }

        val pdf = storageService.get(documentId) ?: throw NotFoundException(documentId)

        logger.debug { "Got document with id=$documentId: $pdf" }
        return pdf
    }

    data class NotFoundException(val documentId: UUID) :
        Exception("No document found with id=$documentId")
}