package de.debuglevel.liberatepdf2.restservice.restrictionsremover.openpdf

import com.lowagie.text.exceptions.BadPasswordException
import com.lowagie.text.pdf.PdfReader
import com.lowagie.text.pdf.PdfStamper
import de.debuglevel.liberatepdf2.restservice.restrictionsremover.RestrictionsRemoverService
import de.debuglevel.liberatepdf2.restservice.transformation.Transformation
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

    override fun removeRestrictions(transformation: Transformation) {
        logger.debug { "Removing restrictions from PDF $transformation..." }

        try {
            val passwordBytes = transformation.password?.encodeToByteArray()
            logger.debug { "Reading PDF $transformation..." }
            val pdfReader = PdfReader(transformation.restrictedPath.toFile().inputStream(), passwordBytes)
            logger.debug { "Read PDF $transformation" }

            val restrictedFilename = transformation.restrictedPath.fileName.toString()
            val unrestrictedFilename = "$restrictedFilename$SUFFIX_PDF_UNRESTRICTED"
            val unrestrictedPath = transformation.restrictedPath.resolveSibling(unrestrictedFilename)
            val unrestrictedOutputStream = unrestrictedPath.toFile().outputStream()

            logger.debug { "Writing PDF without encryption $transformation to $unrestrictedPath..." }
            val pdfStamper = PdfStamper(pdfReader, unrestrictedOutputStream)
            pdfStamper.close()
            logger.debug { "Wrote PDF without encryption $transformation to $unrestrictedPath" }

            pdfReader.close()

            // double check if PDF exists in case something odd happened
            when {
                !unrestrictedPath.toFile().isFile ->
                    throw MissingFileAfterTransformationException(unrestrictedPath.toFile().toString())
                Files.size(unrestrictedPath) == 0L ->
                    throw EmptyFileAfterTransformationException(unrestrictedPath.toFile().toString())
            }

            transformation.unrestrictedPath = unrestrictedPath
            transformation.failed = false

            successfulItems.incrementAndGet()
        } catch (e: BadPasswordException) {
            logger.debug { "Removing restrictions from PDF $transformation failed due to a bad password." }
            transformation.failed = true
            transformation.errorMessage = "Bad password"

            failedItems.incrementAndGet()
        } catch (e: MissingFileAfterTransformationException) {
            logger.error(e) { "Output file was not found after transformation" }
            transformation.failed = true
            transformation.errorMessage = "Output file was not found after transformation"

            failedItems.incrementAndGet()
        } catch (e: EmptyFileAfterTransformationException) {
            logger.error(e) { "Output file was zero bytes after transformation" }
            transformation.failed = true
            transformation.errorMessage = "Output file was zero bytes after transformation"

            failedItems.incrementAndGet()
        } catch (e: Exception) {
            logger.error(e) { "Removing restrictions from PDF $transformation failed due to an unhandled exception." }
            transformation.failed = true
            transformation.errorMessage = "Unknown (${e.message})"

            failedItems.incrementAndGet()
        }

        transformation.finished = true

        logger.debug { "Removed restrictions from PDF $transformation" }
    }

    class MissingFileAfterTransformationException(filename: String) :
        Exception("File $filename not found after transformation")

    class EmptyFileAfterTransformationException(filename: String) :
        Exception("File $filename has zero bytes after transformation")
}