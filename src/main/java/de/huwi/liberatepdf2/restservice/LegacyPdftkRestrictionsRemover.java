package de.huwi.liberatepdf2.restservice;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LegacyPdftkRestrictionsRemover {

	private final Logger Logger = LoggerFactory.getLogger(RestrictionsRemover.class);

	public File RemoveRestrictions(File fileOriginal, String password) {
		Logger.info("Filename = \"{}\"", fileOriginal);
		Logger.info("Password = \"{}\"", password);

		// String exePath = "C:\\Program Files (x86)\\PDFtk\\bin\\pdftk.exe";
		// String exePath = "/usr/bin/pdftk";
		String pdftkExecutable = "pdftk";
		File fileNew = new File(fileOriginal + " (liberated).pdf");
		Logger.info("New filename = \"{}\"", fileNew);

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
			Process p = processBuilder.start();
			Logger.info("Command = \"{}\"", processBuilder.command());

			BufferedReader stdOutput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

			// read the output from the command
			while ((s = stdOutput.readLine()) != null) {
				stdoutLog += s + "\n";
			}

			// read any errors from the attempted command
			while ((s = stdError.readLine()) != null) {
				stderrLog += s + "\n";
			}

			Logger.info("Command standard output: {}" + stdoutLog);
			Logger.info("Command error output: {}" + stderrLog);
		} catch (IOException e) {
			Logger.error("Something went wrong during converting PDF", e);
			System.exit(-1);
		}

		return fileNew;
	}

	public Path[] RemoveRestrictions(Iterable<Path> filesOriginal, String password) {
		Logger.info("Removing restrictions from multiple files");
		ArrayList<Path> filesNew = new ArrayList<Path>();

		for (Path fileOriginal : filesOriginal) {
			Path fileNew = this.RemoveRestrictions(fileOriginal.toFile(), password).toPath();
			filesNew.add(fileNew);
		}

		return filesNew.toArray(new Path[] {});
	}
}
