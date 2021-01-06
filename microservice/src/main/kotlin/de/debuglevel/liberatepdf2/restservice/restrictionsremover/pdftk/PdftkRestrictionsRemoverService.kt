package de.debuglevel.liberatepdf2.restservice.restrictionsremover.pdftk

import de.debuglevel.liberatepdf2.restservice.Pdf
import de.debuglevel.liberatepdf2.restservice.restrictionsremover.RestrictionsRemoverService
import io.micronaut.context.annotation.Requires
import mu.KotlinLogging
import org.apache.commons.io.IOUtils
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Singleton

/**
 * Removes restrictions from a PDF file using the command line version of PDFtk.
 */
@Singleton
@Requires(property = "app.liberatepdf2.backend", value = "pdftk-cli")
class PdftkRestrictionsRemoverService : RestrictionsRemoverService {
    private val logger = KotlinLogging.logger {}

    private val failedItems = AtomicLong()
    override val failedItemsCount = failedItems.get()

    private val successfulItems = AtomicLong()
    override val successfulItemsCount = successfulItems.get()

    override fun removeRestrictions(pdf: Pdf) {
        logger.debug { "Removing restrictions from pdf $pdf..." }

        val unrestrictedPdfPath = removeRestrictions(
            pdf.restrictedPath!!,
            pdf.password
        )

        if (unrestrictedPdfPath == null) {
            logger.debug { "Unrestricted PDF path is null; setting failed=true" }
            pdf.failed = true // TODO: might better throw an exception
            failedItems.incrementAndGet()
        } else {
            pdf.unrestrictedPath = unrestrictedPdfPath
            pdf.done = true
            successfulItems.incrementAndGet()
        }

        logger.debug { "Removed restrictions from pdf $pdf" }
    }

    val SUFFIX_PDF_UNRESTRICTED = ".unrestricted.pdf"

    private fun buildProcess(
        restrictedPdfPath: Path,
        password: String?,
        unrestrictedPdfPath: Path
    ): ProcessBuilder {
        logger.debug { "Building PDFtk process..." }

        // String pdftkExecutable = "C:\\Program Files (x86)\\PDFtk\\bin\\pdftk.exe";
        // String pdftkExecutable = "/usr/bin/pdftk";
        val pdftkExecutable = "pdftk" // TODO: should be configurable

        val processArguments = mutableListOf<String>(pdftkExecutable, restrictedPdfPath.toAbsolutePath().toString())

        if (password?.isNullOrEmpty() == false) {
            logger.debug { "Password given; building command with password argument" }
            processArguments.add("input_pw")
            processArguments.add(password)
        }

        processArguments.addAll(
            listOf(
                "output",
                unrestrictedPdfPath.toAbsolutePath().toString(),
                "allow",
                "AllFeatures"
            )
        )
        val processBuilder = ProcessBuilder(*processArguments.toTypedArray())

        logger.debug { "PDFtk command: " + processBuilder.command().joinToString(" ") }
        return processBuilder
    }

    private fun readOutput(process: Process) {
        val outputInputStream = process.inputStream
        val errorInputStream = process.errorStream
        val stdoutLog = IOUtils.toString(outputInputStream, StandardCharsets.UTF_8)
        val stderrLog = IOUtils.toString(errorInputStream, StandardCharsets.UTF_8)
        outputInputStream.close()
        errorInputStream.close()
        logger.debug { "command standard output: $stdoutLog" }
        logger.debug { "command error output: $stderrLog" }
    }

    fun removeRestrictions(restrictedPdfPath: Path, password: String?): Path? {
        logger.debug { "Removing restrictions from '$restrictedPdfPath' with password='$password'..." }

        val unrestrictedFilename = restrictedPdfPath.fileName.toString() + SUFFIX_PDF_UNRESTRICTED
        val unrestrictedPdfPath: Path = restrictedPdfPath.resolveSibling(unrestrictedFilename)
        logger.trace { "Unrestricted PDF path: $unrestrictedPdfPath" }

        try {
            val processBuilder = buildProcess(restrictedPdfPath, password, unrestrictedPdfPath)

            // run command
            val process = processBuilder.start()
            readOutput(process)
        } catch (e: IOException) {
            logger.error(e) { "Something went wrong during converting PDF" }
            return null // TODO: should probably better throw an exception
        }

        return unrestrictedPdfPath
    }
}