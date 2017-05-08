package rocks.huwi.liberatepdf2.restservice.restrictionsremover.pdftk;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import rocks.huwi.liberatepdf2.restservice.Pdf;
import rocks.huwi.liberatepdf2.restservice.restrictionsremover.RestrictionsRemoverService;
import rocks.huwi.liberatepdf2.restservice.restrictionsremover.pdftk.legacy.LegacyPdftkRestrictionsRemover;

/**
 * Removes restrictions from a PDF file using PDFtk
 */
@Service
public class PdftkRestrictionsRemoverService implements RestrictionsRemoverService {

	private static final Logger log = LoggerFactory.getLogger(PdftkRestrictionsRemoverService.class);

	@Override
	public void removeRestrictions(final Pdf pdf) {
		log.debug("Removing restrictions");

		final LegacyPdftkRestrictionsRemover restrictionsRemover = new LegacyPdftkRestrictionsRemover();
		final Path unrestrictedPdfPath = restrictionsRemover.removeRestrictions(pdf.getRestrictedPath(),
				pdf.getPassword());

		if (unrestrictedPdfPath == null) {
			log.debug("Setting PDF to failed, as unrestricted PDF path is null");
			pdf.setFailed(true);
		}

		pdf.setUnrectrictedPath(unrestrictedPdfPath);
		pdf.setDone(true);
	}

	@Async
	@Override
	public void removeRestrictionsAsync(final Pdf pdf) {
		log.debug("Removing restrictions asynchronously");

		this.removeRestrictions(pdf);
	}
}
