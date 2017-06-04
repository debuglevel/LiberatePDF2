package rocks.huwi.liberatepdf2.restservice.restrictionsremover;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.buf.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import rocks.huwi.liberatepdf2.restservice.Pdf;
import rocks.huwi.liberatepdf2.restservice.PdfDTO;
import rocks.huwi.liberatepdf2.restservice.storage.StorageService;
import rocks.huwi.liberatepdf2.restservice.storage.ZipService;

@RestController
@RequestMapping("/api/v1/documents/")
public class RestrictionRemoveController {

	private static final int HTTP_STATUS_FAILED = 560;

	private static final int HTTP_STATUS_IN_PROGRESS = 260;

	private static final Logger log = LoggerFactory.getLogger(RestrictionRemoveController.class);

	private final RestrictionsRemoverService restrictionsRemoverService;
	private final StorageService storageService;
	private final ZipService zipService;

	@Autowired
	public RestrictionRemoveController(final StorageService storageService,
			final RestrictionsRemoverService restrictionsRemoverService, final ZipService zipService, final TaskExecutor taskExecutor) {
		this.storageService = storageService;
		this.restrictionsRemoverService = restrictionsRemoverService;
		this.zipService = zipService;
	}
	
	@RequestMapping(method = { RequestMethod.GET } , value = "/zip")
	public ResponseEntity<?> downloadZip(@RequestParam final String[] id,
			final HttpServletResponse response) throws IOException {
		log.debug("Received GET or HEAD request for multiple {} documents {}", id.length, StringUtils.join(id));
		
		Path zip = this.zipService.createZip(id);
		
		final FileSystemResource filesystemResource = new FileSystemResource(zip.toFile());

		return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
				.header("Content-Disposition", "attachment; filename=\"" + "unrestricted PDFs.zip" + "\"")
				.body(filesystemResource);
	}

	@RequestMapping(method = { RequestMethod.GET, RequestMethod.HEAD } , value = "/{documentId}")
	public ResponseEntity<?> downloadUnrestricted(@PathVariable final String documentId,
			final HttpServletResponse response) throws IOException {
		log.debug("Received GET or HEAD request for document {}", documentId);

		final Pdf pdf = this.storageService.getItem(documentId);

		if (pdf == null) {
			// no item found with this ID (because no request was assigned this
			// ID by now)
			log.debug("No document with ID={} found", documentId);

			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(String.format("No document found for ID=%s", documentId));
		} else if (pdf.isDone() == false) {
			// the request exists, but was not transformed by now
			log.debug("Document with ID={} found, but pdf.isDone=false (not processed by now)", documentId);

			return ResponseEntity.status(HTTP_STATUS_IN_PROGRESS)
					.body("The document was not processed by now. Please try again later.");
		} else if (Files.exists(pdf.getUnrectrictedPath()) == false) {
			// the request was transformed, but the file does not exist (somehow
			// failed?)
			log.debug("Document with ID={} found, but no file exists", documentId);

			return ResponseEntity.status(HTTP_STATUS_FAILED).body(
					"The document was processed, but produced no result. Maybe the password was wrong or another error occurred.");
		} else {
			// request should be okay
			final String filename = this.storageService.getItem(documentId).getOriginalFilename();
			final FileSystemResource filesystemResource = new FileSystemResource(pdf.getUnrectrictedPath().toFile());

			log.debug("Document with ID={} found and set for delivery", documentId);

			return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
					.header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
					.body(filesystemResource);
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/")
	public ResponseEntity<?> uploadAndRemoveRestrictions(final PdfDTO restrictedPdf,
			final UriComponentsBuilder uriComponentsBuilder) {
		log.debug("Received POST request for document {}", restrictedPdf.getFile().getName());

		final Pdf pdf = this.storageService.store(restrictedPdf.getFile(), restrictedPdf.getPassword());
		pdf.setPassword(restrictedPdf.getPassword());

		this.restrictionsRemoverService.removeRestrictionsAsync(pdf);

		final UriComponents uriComponents = uriComponentsBuilder.path("/api/v1/documents/{id}")
				.buildAndExpand(pdf.getId());
		return ResponseEntity.accepted().location(uriComponents.toUri()).body(pdf.getId());
	}
}
