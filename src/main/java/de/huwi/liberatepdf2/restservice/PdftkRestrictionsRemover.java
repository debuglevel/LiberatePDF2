package de.huwi.liberatepdf2.restservice;

import java.io.File;
import java.nio.file.Path;

/**
 * Removes restrictions from a PDF file using PDFtk
 */
public class PdftkRestrictionsRemover implements RestrictionsRemover {

	@Override
	public Path removeRestrictions(Iterable<Path> originalPdfs, String password) {
//		final LegacyPdftkRestrictionsRemover legacyPdftkRestrictionsRemover = new LegacyPdftkRestrictionsRemover();
//		final File unrestrictedPdf = legacyPdftkRestrictionsRemover.RemoveRestrictions(originalPdf.toFile(), password);
//		
//		return unrestrictedPdf.toPath();
		
		final BatchProcessor batchProcessor = new BatchProcessor();
		final Path unrestrictedPdfOrZip = batchProcessor.RemoveRestrictions(originalPdfs, password);
		
		return unrestrictedPdfOrZip;
	}

	@Override
	public Path removeRestrictions(Iterable<Path> originalPdfs) {
		return this.removeRestrictions(originalPdfs, null);
	}
}
