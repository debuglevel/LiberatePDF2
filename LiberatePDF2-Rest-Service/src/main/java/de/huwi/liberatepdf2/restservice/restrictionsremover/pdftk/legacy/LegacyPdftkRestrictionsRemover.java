package de.huwi.liberatepdf2.restservice.restrictionsremover.pdftk.legacy;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.huwi.liberatepdf2.restservice.restrictionsremover.RestrictionsRemoverService;
import de.huwi.liberatepdf2.restservice.storage.FilesystemStorageService;

public class LegacyPdftkRestrictionsRemover {

	private final Logger Logger = LoggerFactory.getLogger(RestrictionsRemoverService.class);

	public File RemoveRestrictions(final File fileOriginal, final String password) {
		this.Logger.info("Filename = \"{}\"", fileOriginal);
		this.Logger.info("Password = \"{}\"", password);

		// String exePath = "C:\\Program Files (x86)\\PDFtk\\bin\\pdftk.exe";
		// String exePath = "/usr/bin/pdftk";
		final String pdftkExecutable = "pdftk";
		final File fileNew = new File(fileOriginal + FilesystemStorageService.SUFFIX_PDF_UNRESTRICTED);
		this.Logger.info("New filename = \"{}\"", fileNew);

		try {
			String stdoutLog = "";
			String stderrLog = "";
			String s;

			// build command
			ProcessBuilder processBuilder;
			if ((password != null) && !password.isEmpty()) {
				String passwordArgument = "";

				passwordArgument = "input_pw";
				processBuilder = new ProcessBuilder(pdftkExecutable, fileOriginal.getAbsolutePath(), passwordArgument,
						password, "output", fileNew.getAbsolutePath(), "allow", "AllFeatures");
			} else {
				processBuilder = new ProcessBuilder(pdftkExecutable, fileOriginal.getAbsolutePath(), "output",
						fileNew.getAbsolutePath(), "allow", "AllFeatures");
			}

			// run command
			final Process p = processBuilder.start();
			this.Logger.info("Command = \"{}\"", processBuilder.command());

			final BufferedReader stdOutput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			final BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

			// read the output from the command
			while ((s = stdOutput.readLine()) != null) {
				stdoutLog += s + "\n";
			}

			// read any errors from the attempted command
			while ((s = stdError.readLine()) != null) {
				stderrLog += s + "\n";
			}

			this.Logger.info("Command standard output: {}" + stdoutLog);
			this.Logger.info("Command error output: {}" + stderrLog);
		} catch (final IOException e) {
			this.Logger.error("Something went wrong during converting PDF", e);
			System.exit(-1);
		}

		return fileNew;
	}

	public Path[] RemoveRestrictions(final Iterable<Path> filesOriginal, final String password) {
		this.Logger.info("Removing restrictions from multiple files");
		final ArrayList<Path> filesNew = new ArrayList<>();

		for (final Path fileOriginal : filesOriginal) {
			final Path fileNew = this.RemoveRestrictions(fileOriginal.toFile(), password).toPath();
			filesNew.add(fileNew);
		}

		return filesNew.toArray(new Path[] {});
	}
}
