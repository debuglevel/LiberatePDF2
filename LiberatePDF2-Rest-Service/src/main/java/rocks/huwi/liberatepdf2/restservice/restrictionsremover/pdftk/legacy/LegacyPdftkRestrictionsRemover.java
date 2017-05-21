package rocks.huwi.liberatepdf2.restservice.restrictionsremover.pdftk.legacy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.huwi.liberatepdf2.restservice.restrictionsremover.RestrictionsRemoverService;
import rocks.huwi.liberatepdf2.restservice.storage.TemporaryFilesystemStorageService;

public class LegacyPdftkRestrictionsRemover {

	private final Logger log = LoggerFactory.getLogger(RestrictionsRemoverService.class);

	private ProcessBuilder buildProcess(final Path restrictedPdfPath, final String password,
			final Path unrestrictedPdfPath) {
		this.log.debug("Building PDFtk process");

		// String exePath = "C:\\Program Files (x86)\\PDFtk\\bin\\pdftk.exe";
		// String exePath = "/usr/bin/pdftk";
		final String pdftkExecutable = "pdftk";

		ProcessBuilder processBuilder;
		if ((password != null) && !password.isEmpty()) {
			this.log.debug("No password given or password is empty, building command without password argument");

			final String passwordArgument = "input_pw";
			processBuilder = new ProcessBuilder(pdftkExecutable, restrictedPdfPath.toAbsolutePath().toString(),
					passwordArgument, password, "output", unrestrictedPdfPath.toAbsolutePath().toString(), "allow",
					"AllFeatures");
		} else {
			this.log.debug("Password given, building command with password argument");

			processBuilder = new ProcessBuilder(pdftkExecutable, restrictedPdfPath.toAbsolutePath().toString(),
					"output", unrestrictedPdfPath.toAbsolutePath().toString(), "allow", "AllFeatures");
		}

		this.log.debug("Final PDFtk command is: {}", String.join(" ", processBuilder.command()));

		return processBuilder;
	}

	private void readOutput(final Process process) throws IOException {
		final InputStream outputInputStream = process.getInputStream();
		final InputStream errorInputStream = process.getErrorStream();

		final String stdoutLog = IOUtils.toString(outputInputStream, StandardCharsets.UTF_8);
		final String stderrLog = IOUtils.toString(errorInputStream, StandardCharsets.UTF_8);

		outputInputStream.close();
		errorInputStream.close();

		this.log.debug("command standard output: {}" + stdoutLog);
		this.log.debug("command error output: {}" + stderrLog);
	}

	public Path removeRestrictions(final Path restrictedPdfPath, final String password) {
		this.log.debug("restricted filename = \"{}\"", restrictedPdfPath);
		this.log.debug("password = \"{}\"", password);

		final Path unrestrictedPdfPath = restrictedPdfPath.resolveSibling(
				restrictedPdfPath.getFileName() + TemporaryFilesystemStorageService.SUFFIX_PDF_UNRESTRICTED);
		this.log.debug("unrestricted filename = \"{}\"", unrestrictedPdfPath);

		try {
			final ProcessBuilder processBuilder = this.buildProcess(restrictedPdfPath, password, unrestrictedPdfPath);

			// run command
			final Process process = processBuilder.start();

			this.readOutput(process);
		} catch (final IOException e) {
			this.log.debug("Something went wrong during converting PDF", e);
			return null;
		}

		return unrestrictedPdfPath;
	}
}
