package de.huwi.liberatepdf2.restservice;

import java.nio.file.Path;
import java.util.Arrays;

/**
 * Removes restrictions from a PDF file using PDFtk
 */
public class PdftkRestrictionsRemover implements RestrictionsRemover {

	@Override
	public Path removeRestrictions(final Iterable<Path> originalPdfs) {
		return this.removeRestrictions(originalPdfs, null);
	}

	@Override
	public Path removeRestrictions(final Iterable<Path> originalPdfs, final String password) {
		final BatchProcessor batchProcessor = new BatchProcessor();
		final Path unrestrictedPdfOrZip = batchProcessor.RemoveRestrictions(originalPdfs, password);

		return unrestrictedPdfOrZip;
	}

	@Override
	public Path removeRestrictions(Path original) {
		return this.removeRestrictions(original, null);
	}

	@Override
	public Path removeRestrictions(Path original, String password) {
		return this.removeRestrictions(Arrays.asList(new Path[] { original }), password);
	}
}
