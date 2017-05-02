package de.huwi.liberatepdf2.restservice.restrictionsremover.pdftk;

import java.nio.file.Path;
import java.util.Arrays;

import de.huwi.liberatepdf2.restservice.restrictionsremover.RestrictionsRemover;
import de.huwi.liberatepdf2.restservice.restrictionsremover.pdftk.legacy.LegacyPdftkBatchProcessor;

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
		final LegacyPdftkBatchProcessor batchProcessor = new LegacyPdftkBatchProcessor();
		final Path unrestrictedPdfOrZip = batchProcessor.RemoveRestrictions(originalPdfs, password);

		return unrestrictedPdfOrZip;
	}

	@Override
	public Path removeRestrictions(final Path original) {
		return this.removeRestrictions(original, null);
	}

	@Override
	public Path removeRestrictions(final Path original, final String password) {
		return this.removeRestrictions(Arrays.asList(new Path[] { original }), password);
	}
}
