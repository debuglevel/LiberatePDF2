package de.debuglevel.liberatepdf2.restservice.restrictionsremover.pdfbox

import de.debuglevel.liberatepdf2.restservice.restrictionsremover.RestrictionsRemoverService
import de.debuglevel.liberatepdf2.restservice.transformation.Transformation
import io.micronaut.context.annotation.Requires
import mu.KotlinLogging
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Singleton

/**
 * Removes restrictions from a PDF file using the PDFBox library.
 */
@Singleton
@Requires(property = "app.liberatepdf2.transformation.backend", value = "pdfbox")
class PdfboxRestrictionsRemover : RestrictionsRemoverService {
    private val logger = KotlinLogging.logger {}

    private val failedItems = AtomicLong()
    override val failedItemsCount: Long
        get() = failedItems.get()

    private val successfulItems = AtomicLong()
    override val successfulItemsCount: Long
        get() = successfulItems.get()

    override fun removeRestrictions(transformation: Transformation) {
        logger.debug { "Removing restrictions from PDF $transformation..." }

        try {
            val unrestrictedOutputStream = ByteArrayOutputStream()

            logger.debug { "Reading PDF $transformation..." }
            Loader.loadPDF(transformation.restrictedFile, transformation.password).use { document ->
                logger.debug { "Read PDF $transformation" }

                document.isAllSecurityToBeRemoved = true

                logger.debug { "Writing PDF without encryption $transformation..." }
                document.save(unrestrictedOutputStream)
                logger.debug { "Wrote PDF without encryption $transformation" }
            }

            // double check if PDF contains some bytes in case something odd happened
            if (unrestrictedOutputStream.size() == 0) {
                throw EmptyByteArrayAfterTransformationException()
            }

            transformation.unrestrictedFile = unrestrictedOutputStream.toByteArray()
            transformation.failed = false

            successfulItems.incrementAndGet()
        } catch (e: InvalidPasswordException) {
            logger.debug { "Removing restrictions from PDF $transformation failed due to a bad password." }
            transformation.failed = true
            transformation.errorMessage = "Bad password"

            failedItems.incrementAndGet()
        } catch (e: EmptyByteArrayAfterTransformationException) {
            logger.error(e) { "Output was zero bytes after transformation" }
            transformation.failed = true
            transformation.errorMessage = "Output was zero bytes after transformation"

            failedItems.incrementAndGet()
        } catch (e: IOException) {
            logger.debug(e) { "File in $transformation seems not to be a valid PDF." }
            transformation.failed = true
            transformation.errorMessage = "File is not a valid PDF (${e.message})"

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

    class EmptyByteArrayAfterTransformationException :
        Exception("ByteArray of OutputStream has zero bytes after transformation")
}