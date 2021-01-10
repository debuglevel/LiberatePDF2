package de.debuglevel.liberatepdf2.restservice.document

import de.debuglevel.liberatepdf2.restservice.Pdf
import de.debuglevel.liberatepdf2.restservice.storage.StorageService
import mu.KotlinLogging
import java.util.*
import javax.inject.Singleton

@Singleton
class DocumentService(
    private val storageService: StorageService,
) {
    private val logger = KotlinLogging.logger {}

    fun get(documentId: UUID): Pdf {
        logger.debug { "Getting document with id=$documentId..." }

        val pdf = try {
            storageService.get(documentId)
        } catch (e: StorageService.NotFoundException) {
            throw NotFoundException(documentId, e)
        }

        logger.debug { "Got document with id=$documentId: $pdf" }
        return pdf
    }

    data class NotFoundException(val documentId: UUID, val inner: Exception) :
        Exception("No document found with id=$documentId (inner Exception: $inner)")
}