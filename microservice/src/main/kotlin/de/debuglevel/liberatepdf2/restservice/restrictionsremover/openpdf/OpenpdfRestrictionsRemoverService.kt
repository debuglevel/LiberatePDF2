package de.debuglevel.liberatepdf2.restservice.restrictionsremover.openpdf

import com.lowagie.text.exceptions.BadPasswordException
import com.lowagie.text.exceptions.InvalidPdfException
import com.lowagie.text.pdf.PdfReader
import com.lowagie.text.pdf.PdfStamper
import de.debuglevel.liberatepdf2.restservice.restrictionsremover.RestrictionsRemoverService
import de.debuglevel.liberatepdf2.restservice.storage.StorageService
import de.debuglevel.liberatepdf2.restservice.transformation.Transformation
import io.micronaut.context.annotation.Requires
import mu.KotlinLogging
import java.io.ByteArrayOutputStream
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Singleton

/**
 * Removes restrictions from a PDF file using the OpenPDF library.
 */
@Singleton
@Requires(property = "app.liberatepdf2.transformation.backend", value = "openpdf")
class OpenpdfRestrictionsRemoverService(
    private val storageService: StorageService
) : RestrictionsRemoverService {
    private val logger = KotlinLogging.logger {}

    private val failedItems = AtomicLong()
    override val failedItemsCount: Long
        get() = failedItems.get()

    private val successfulItems = AtomicLong()
    override val successfulItemsCount: Long
        get() = successfulItems.get()

    val SUFFIX_PDF_UNRESTRICTED = ".unrestricted.pdf"

    override fun removeRestrictions(transformation: Transformation) {
        logger.debug { "Removing restrictions from PDF $transformation..." }

        val restrictedStoredFile = storageService.get(transformation.restrictedStoredFileId)

        try {
            val passwordBytes = transformation.password?.encodeToByteArray()
            logger.debug { "Reading PDF $transformation..." }
            val pdfReader = PdfReader(restrictedStoredFile.inputStream, passwordBytes)
            logger.debug { "Read PDF $transformation" }

            val restrictedFilename = restrictedStoredFile.filename.toString()
            val unrestrictedFilename = "$restrictedFilename$SUFFIX_PDF_UNRESTRICTED"
            val unrestrictedOutputStream = ByteArrayOutputStream()

            logger.debug { "Writing PDF without encryption $transformation..." }
            val pdfStamper = PdfStamper(pdfReader, unrestrictedOutputStream)
            pdfStamper.close()
            logger.debug { "Wrote PDF without encryption $transformation" }

            pdfReader.close()

            // double check if PDF contains some bytes in case something odd happened
            if (unrestrictedOutputStream.size() == 0) {
                throw EmptyByteArrayAfterTransformationException()
            }

            val inputStream = unrestrictedOutputStream.toByteArray()
                .inputStream() // TODO: using PipedOutputStream might be better: https://stackoverflow.com/a/23874232/4764279
            val unrestrictedStoredFile = storageService.store(unrestrictedFilename, inputStream, "")

            transformation.unrestrictedStoredFileId = unrestrictedStoredFile.id
            transformation.failed = false

            successfulItems.incrementAndGet()
        } catch (e: BadPasswordException) {
            logger.debug { "Removing restrictions from PDF $transformation failed due to a bad password." }
            transformation.failed = true
            transformation.errorMessage = "Bad password"

            failedItems.incrementAndGet()
        } catch (e: EmptyByteArrayAfterTransformationException) {
            logger.error(e) { "Output was zero bytes after transformation" }
            transformation.failed = true
            transformation.errorMessage = "Output was zero bytes after transformation"

            failedItems.incrementAndGet()
        } catch (e: InvalidPdfException) {
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