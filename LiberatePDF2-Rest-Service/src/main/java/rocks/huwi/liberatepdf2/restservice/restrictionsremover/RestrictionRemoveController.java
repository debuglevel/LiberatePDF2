package rocks.huwi.liberatepdf2.restservice.restrictionsremover;

import java.io.IOException;
import java.nio.file.Files;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import rocks.huwi.liberatepdf2.restservice.Pdf;
import rocks.huwi.liberatepdf2.restservice.PdfDTO;
import rocks.huwi.liberatepdf2.restservice.storage.StorageService;

@RestController
@RequestMapping("/api/v1/documents/")
public class RestrictionRemoveController {

	private static final Logger log = LoggerFactory.getLogger(RestrictionRemoveController.class);

	private final RestrictionsRemoverService restrictionsRemoverService;
	private final StorageService storageService;

	@Autowired
	public RestrictionRemoveController(final StorageService storageService,
			final RestrictionsRemoverService restrictionsRemoverService, final TaskExecutor taskExecutor) {
		this.storageService = storageService;
		this.restrictionsRemoverService = restrictionsRemoverService;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{documentId}")
	public @ResponseBody FileSystemResource downloadUnrestricted(@PathVariable final Long documentId,
			final HttpServletResponse response) throws IOException {
		log.debug("Received GET request for document {}", documentId);

		final Pdf pdf = this.storageService.getItem(documentId);

		if (pdf == null) {
			// no item found with this ID (because no request was assigned this
			// ID by now)
			log.debug("No document with ID={} found", documentId);

			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.getWriter().println(String.format("No document found for ID={}", documentId));
			return null;
		} else if (pdf.isDone() == false) {
			// the request exists, but was not transformed by now
			log.debug("Document with ID={} found, but pdf.isDone=false (not processed by now)", documentId);

			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.getWriter()
					.println(String.format("The document was not processed by now. Please try again later."));
			return null;
		} else if (Files.exists(pdf.getUnrectrictedPath()) == false) {
			// the request was transformed, but the file does not exist (somehow
			// failed?)
			log.debug("Document with ID={} found, but no file exists", documentId);

			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.getWriter().println(String.format(
					"The document was processed, but produced no result. Maybe the password was wrong or another error occurred."));
			return null;
		} else {
			// request should be okay
			response.setContentType("application/pdf");
			final String filename = this.storageService.getItem(documentId).getOriginalFilename();
			response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
			final FileSystemResource filesystemResource = new FileSystemResource(pdf.getUnrectrictedPath().toFile());

			log.debug("Document with ID={} found and set for delivery", documentId);

			return filesystemResource;
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/")
	public ResponseEntity<?> uploadAndRemoveRestrictions(final PdfDTO restrictedPdf, UriComponentsBuilder uriComponentsBuilder) {
		log.debug("Received POST request for document {}", restrictedPdf.getFile().getName());

		final Pdf pdf = this.storageService.store(restrictedPdf.getFile());
		pdf.setPassword(restrictedPdf.getPassword());

		this.restrictionsRemoverService.removeRestrictionsAsync(pdf);
		
		UriComponents uriComponents = uriComponentsBuilder.path("/api/v1/documents/{id}").buildAndExpand(pdf.getId());
		return ResponseEntity.created(uriComponents.toUri()).body(pdf.getId());
	}
}
