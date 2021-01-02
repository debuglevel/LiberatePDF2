package rocks.huwi.liberatepdf2.restservice.restrictionsremover.pdftk

import mu.KotlinLogging
import rocks.huwi.liberatepdf2.restservice.Pdf
import rocks.huwi.liberatepdf2.restservice.restrictionsremover.RestrictionsRemoverService
import rocks.huwi.liberatepdf2.restservice.restrictionsremover.pdftk.legacy.LegacyPdftkRestrictionsRemover
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Singleton

/**
 * Removes restrictions from a PDF file using PDFtk
 */
@Singleton
class PdftkRestrictionsRemoverService : RestrictionsRemoverService {
    private val logger = KotlinLogging.logger {}

    private val failedItems = AtomicLong()
    private val processedItems = AtomicLong()
    override val failedItemsCount = failedItems.get()
    override val itemsCount = processedItems.get()

    override fun removeRestrictions(pdf: Pdf) {
        logger.debug { ("Removing restrictions") }
        val restrictionsRemover = LegacyPdftkRestrictionsRemover()
        val unrestrictedPdfPath = restrictionsRemover.removeRestrictions(
            pdf.restrictedPath,
            pdf.password
        )
        if (unrestrictedPdfPath == null) {
            logger.debug { ("Setting PDF to failed, as unrestricted PDF path is null") }
            pdf.failed = true
            failedItems.incrementAndGet()
        }
        pdf.unrestrictedPath = unrestrictedPdfPath
        pdf.isDone = true
        processedItems.incrementAndGet()
    }

    //@Async
    override fun removeRestrictionsAsync(pdf: Pdf) {
        logger.debug { ("Removing restrictions asynchronously") }
        removeRestrictions(pdf)
    }
}