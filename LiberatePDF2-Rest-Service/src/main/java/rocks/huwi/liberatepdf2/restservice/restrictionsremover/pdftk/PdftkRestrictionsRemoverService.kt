package rocks.huwi.liberatepdf2.restservice.restrictionsremover.pdftk

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import rocks.huwi.liberatepdf2.restservice.Pdf
import rocks.huwi.liberatepdf2.restservice.restrictionsremover.RestrictionsRemoverService
import rocks.huwi.liberatepdf2.restservice.restrictionsremover.pdftk.legacy.LegacyPdftkRestrictionsRemover
import java.util.concurrent.atomic.AtomicLong

/**
 * Removes restrictions from a PDF file using PDFtk
 */
@Service
class PdftkRestrictionsRemoverService : RestrictionsRemoverService {
    private val log = LoggerFactory.getLogger(PdftkRestrictionsRemoverService::class.java)

    private val failedItems = AtomicLong()
    private val processedItems = AtomicLong()
    override val failedItemsCount
        get() = failedItems.get()
    override val itemsCount
        get() = processedItems.get()

    override fun removeRestrictions(pdf: Pdf) {
        log.debug("Removing restrictions")
        val restrictionsRemover = LegacyPdftkRestrictionsRemover()
        val unrestrictedPdfPath = restrictionsRemover.removeRestrictions(
            pdf.restrictedPath,
            pdf.password
        )
        if (unrestrictedPdfPath == null) {
            log.debug("Setting PDF to failed, as unrestricted PDF path is null")
            pdf.failed = true
            failedItems.incrementAndGet()
        }
        pdf.unrestrictedPath = unrestrictedPdfPath
        pdf.isDone = true
        processedItems.incrementAndGet()
    }

    @Async
    override fun removeRestrictionsAsync(pdf: Pdf) {
        log.debug("Removing restrictions asynchronously")
        removeRestrictions(pdf)
    }
}