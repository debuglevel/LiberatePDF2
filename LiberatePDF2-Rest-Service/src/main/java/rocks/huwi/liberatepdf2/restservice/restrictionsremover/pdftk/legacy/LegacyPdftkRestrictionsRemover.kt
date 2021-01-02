package rocks.huwi.liberatepdf2.restservice.restrictionsremover.pdftk.legacy

import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import rocks.huwi.liberatepdf2.restservice.restrictionsremover.RestrictionsRemoverService
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Path

class LegacyPdftkRestrictionsRemover {
    private val log = LoggerFactory.getLogger(RestrictionsRemoverService::class.java)

    val SUFFIX_PDF_UNRESTRICTED = ".unrestricted.pdf"

    private fun buildProcess(
        restrictedPdfPath: Path?, password: String?,
        unrestrictedPdfPath: Path
    ): ProcessBuilder {
        log.debug("Building PDFtk process")

        // String exePath = "C:\\Program Files (x86)\\PDFtk\\bin\\pdftk.exe";
        // String exePath = "/usr/bin/pdftk";
        val pdftkExecutable = "pdftk"
        val processBuilder: ProcessBuilder
        processBuilder = if (password != null && !password.isEmpty()) {
            log.debug("No password given or password is empty, building command without password argument")
            val passwordArgument = "input_pw"
            ProcessBuilder(
                pdftkExecutable, restrictedPdfPath!!.toAbsolutePath().toString(),
                passwordArgument, password, "output", unrestrictedPdfPath.toAbsolutePath().toString(), "allow",
                "AllFeatures"
            )
        } else {
            log.debug("Password given, building command with password argument")
            ProcessBuilder(
                pdftkExecutable, restrictedPdfPath!!.toAbsolutePath().toString(),
                "output", unrestrictedPdfPath.toAbsolutePath().toString(), "allow", "AllFeatures"
            )
        }
        log.debug("Final PDFtk command is: {}", java.lang.String.join(" ", processBuilder.command()))
        return processBuilder
    }

    @Throws(IOException::class)
    private fun readOutput(process: Process) {
        val outputInputStream = process.inputStream
        val errorInputStream = process.errorStream
        val stdoutLog = IOUtils.toString(outputInputStream, StandardCharsets.UTF_8)
        val stderrLog = IOUtils.toString(errorInputStream, StandardCharsets.UTF_8)
        outputInputStream.close()
        errorInputStream.close()
        log.debug("command standard output: {}$stdoutLog")
        log.debug("command error output: {}$stderrLog")
    }

    fun removeRestrictions(restrictedPdfPath: Path?, password: String?): Path? {
        log.debug("restricted filename = \"{}\"", restrictedPdfPath)
        log.debug("password = \"{}\"", password)
        val unrestrictedPdfPath: Path = restrictedPdfPath
            ?.resolveSibling(restrictedPdfPath.fileName.toString() + SUFFIX_PDF_UNRESTRICTED)!!
        log.debug("unrestricted filename = \"{}\"", unrestrictedPdfPath)
        try {
            val processBuilder = buildProcess(restrictedPdfPath, password, unrestrictedPdfPath)

            // run command
            val process = processBuilder.start()
            readOutput(process)
        } catch (e: IOException) {
            log.debug("Something went wrong during converting PDF", e)
            return null
        }
        return unrestrictedPdfPath
    }
}