package de.debuglevel.liberatepdf2.restservice.restrictionsremover.openpdf

import com.lowagie.text.exceptions.BadPasswordException
import com.lowagie.text.pdf.PdfReader
import com.lowagie.text.pdf.PdfStamper
import de.debuglevel.liberatepdf2.restservice.Pdf
import de.debuglevel.liberatepdf2.restservice.restrictionsremover.RestrictionsRemoverService
import io.micronaut.context.annotation.Requires
import mu.KotlinLogging
import java.nio.file.Files
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Singleton

/**
 * Removes restrictions from a PDF file using the OpenPDF library.
 */
@Singleton
@Requires(property = "app.liberatepdf2.transformation.backend", value = "openpdf")
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
            logger.debug { "Reading PDF $pdf..." }
            val pdfReader = PdfReader(pdf.restrictedPath!!.toFile().inputStream(), password)
            logger.debug { "Read PDF $pdf" }

            val restrictedFilename = pdf.restrictedPath!!.fileName.toString()
            val unrestrictedFilename = "$restrictedFilename$SUFFIX_PDF_UNRESTRICTED"
            val unrestrictedPath = pdf.restrictedPath!!.resolveSibling(unrestrictedFilename)
            val unrestrictedOutputStream = unrestrictedPath.toFile().outputStream()

            logger.debug { "Writing PDF without encryption $pdf to $unrestrictedPath..." }
            val pdfStamper = PdfStamper(pdfReader, unrestrictedOutputStream)
            pdfStamper.close()
            logger.debug { "Wrote PDF without encryption $pdf to $unrestrictedPath" }

            pdfReader.close()

            // double check if PDF exists in case something odd happened
            when {
                !unrestrictedPath.toFile().isFile ->
                    throw MissingFileAfterTransformationException(unrestrictedPath.toFile().toString())
                Files.size(unrestrictedPath) == 0L ->
                    throw EmptyFileAfterTransformationException(unrestrictedPath.toFile().toString())
            }

            pdf.unrestrictedPath = unrestrictedPath
            pdf.done = true
            pdf.failed = false

            successfulItems.incrementAndGet()
        } catch (e: BadPasswordException) {
            logger.debug { "Removing restrictions from PDF $pdf failed due to a bad password." }
            pdf.done = true
            pdf.failed = true
            pdf.error = "Bad password"

            failedItems.incrementAndGet()
        } catch (e: MissingFileAfterTransformationException) {
            pdf.done = true
            pdf.failed = true
            pdf.error = "Output file was not found after transformation"

            failedItems.incrementAndGet()
        } catch (e: EmptyFileAfterTransformationException) {
            pdf.done = true
            pdf.failed = true
            pdf.error = "Output file was zero bytes after transformation"

            failedItems.incrementAndGet()
        } catch (e: Exception) {
            logger.warn(e) { "Removing restrictions from PDF $pdf failed due to an unhandled exception." }
            pdf.done = true
            pdf.failed = true
            pdf.error = "Unknown (${e.message})"

            failedItems.incrementAndGet()
        }

        logger.debug { "Removed restrictions from PDF $pdf" }
    }

    class MissingFileAfterTransformationException(filename: String) :
        Exception("File $filename not found after transformation")

    class EmptyFileAfterTransformationException(filename: String) :
        Exception("File $filename has zero bytes after transformation")
}