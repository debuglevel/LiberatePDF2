package de.huwi.liberatepdf2.restservice.restrictionsremover;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import de.huwi.liberatepdf2.restservice.Pdf;
import de.huwi.liberatepdf2.restservice.PdfDTO;
import de.huwi.liberatepdf2.restservice.storage.StorageService;

@RestController
@RequestMapping("/api/v1/documents/")
public class RestrictionRemoveController {

	private final RestrictionsRemoverService restrictionsRemoverService;
	private final StorageService storageService;
	private final TaskExecutor taskExecutor;
	
	@Autowired
	public RestrictionRemoveController(final StorageService storageService,
			final RestrictionsRemoverService restrictionsRemoverService,
			final TaskExecutor taskExecutor) {
		this.storageService = storageService;
		this.restrictionsRemoverService = restrictionsRemoverService;
		this.taskExecutor = taskExecutor;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{documentId}")
	public @ResponseBody FileSystemResource downloadUnrestricted(@PathVariable final Long documentId,
			final HttpServletResponse response) throws IOException {
		final Pdf pdf = this.storageService.getItem(documentId);

		if (pdf == null) {
			// no item found with this ID (because no request was assigned this
			// ID by now)
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.getWriter().println(String.format("No document found for ID={}", documentId));
			return null;
		} else if (pdf.isDone() == false) {
			// the request exists, but was not transformed by now
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.getWriter()
					.println(String.format("The document was not processed by now. Please try again later."));
			return null;
		} else if (Files.exists(pdf.getUnrectrictedPath()) == false) {
			// the request was transformed, but the file does not exist (somehow
			// failed?)
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

			return filesystemResource;
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/")
	public @ResponseBody long uploadAndRemoveRestrictions(final PdfDTO restrictedPdf) {
		final Pdf pdf = this.storageService.store(restrictedPdf.getFile());

		this.removeRestrictionsAsync(restrictedPdf, pdf);

		return pdf.getId();
	}
	
	/**
	 * Enqueue a task to remove removes restrictions
	 * @param restrictedPdf
	 * @param pdf
	 */
	@Async
	private void removeRestrictionsAsync(final PdfDTO restrictedPdf, final Pdf pdf)
	{
		final Path unrestrictedPdfPath = this.restrictionsRemoverService.removeRestrictions(pdf.getRestrictedPath(),
				restrictedPdf.getPassword());

		pdf.setUnrectrictedPath(unrestrictedPdfPath);
		pdf.setDone(true);
	}

	// @RequestMapping(method = RequestMethod.POST, value = "/legacy")
	// public @ResponseBody FileSystemResource removeRestrictions(final PdfsDTO
	// restrictedPdfs,
	// final HttpServletResponse response) throws IOException {
	//
	// if ((restrictedPdfs.getFiles() == null) ||
	// (restrictedPdfs.getFiles().length == 0)) {
	// response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	// return null;
	// }
	//
	// final ArrayList<Path> restrictedPdfsTempPaths =
	// MultipartFileUtils.getPaths(restrictedPdfs.getFiles());
	//
	// final RestrictionsRemover restrictionsRemover = new
	// PdftkRestrictionsRemover();
	// final Path unrestrictedPdfPath =
	// restrictionsRemover.removeRestrictions(restrictedPdfsTempPaths,
	// restrictedPdfs.getPassword());
	//
	// final FileSystemResource fileSystemResource = new
	// FileSystemResource(unrestrictedPdfPath.toFile());
	//
	// String unrestrictedFilename = "unknown";
	// if (restrictedPdfsTempPaths.size() == 1) {
	// unrestrictedFilename = restrictedPdfs.getFiles()[0].getOriginalFilename()
	// + " (unrestricted).pdf";
	// response.setContentType("application/pdf");
	// } else if (restrictedPdfsTempPaths.size() > 1) {
	// unrestrictedFilename = "PDFs (unrestricted).zip";
	// response.setContentType("application/zip");
	// } else {
	//
	// }
	// response.setHeader("Content-Disposition", "attachment; filename=\"" +
	// unrestrictedFilename + "\"");
	//
	// return fileSystemResource;
	// }
}
