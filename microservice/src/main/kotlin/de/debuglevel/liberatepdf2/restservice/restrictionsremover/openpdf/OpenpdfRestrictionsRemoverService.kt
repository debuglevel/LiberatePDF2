package de.debuglevel.liberatepdf2.restservice.restrictionsremover.openpdf

import com.lowagie.text.exceptions.BadPasswordException
import com.lowagie.text.pdf.PdfReader
import com.lowagie.text.pdf.PdfStamper
import de.debuglevel.liberatepdf2.restservice.Pdf
import de.debuglevel.liberatepdf2.restservice.restrictionsremover.RestrictionsRemoverService
import io.micronaut.context.annotation.Requires
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Singleton

/**
 * Removes restrictions from a PDF file using the OpenPDF library.
 */
@Singleton
@Requires(property = "app.liberatepdf2.backend", value = "openpdf")
class OpenpdfRestrictionsRemoverService : RestrictionsRemoverService {
    private val logger = KotlinLogging.logger {}

    private val failedItems = AtomicLong()
    override val failedItemsCount = failedItems.get()

    private val successfulItems = AtomicLong()
    override val successfulItemsCount = successfulItems.get()

    val SUFFIX_PDF_UNRESTRICTED = ".unrestricted.pdf"

    override fun removeRestrictions(pdf: Pdf) {
        logger.debug { "Removing restrictions from PDF $pdf..." }

        try {
            val password = pdf.password?.encodeToByteArray()
            val pdfReader = PdfReader(pdf.restrictedPath!!.toFile().inputStream(), password)

            val restrictedFilename = pdf.restrictedPath!!.fileName.toString()
            val unrestrictedFilename = "$restrictedFilename$SUFFIX_PDF_UNRESTRICTED"
            val unrestrictedPath = pdf.restrictedPath!!.resolveSibling(unrestrictedFilename)
            val unrestrictedOutputStream = unrestrictedPath.toFile().outputStream()

            val pdfStamper = PdfStamper(pdfReader, unrestrictedOutputStream)
            pdfStamper.close()
            pdfReader.close()

            pdf.unrestrictedPath = unrestrictedPath
            pdf.done = true

            successfulItems.incrementAndGet()
        } catch (e: BadPasswordException) {
            pdf.failed = true
            pdf.error = "Bad password"

            failedItems.incrementAndGet()
        } catch (e: Exception) {
            pdf.failed = true
            pdf.error = "Unknown (${e.message})"

            failedItems.incrementAndGet()
        }

        logger.debug { "Removed restrictions from PDF $pdf" }
    }

}