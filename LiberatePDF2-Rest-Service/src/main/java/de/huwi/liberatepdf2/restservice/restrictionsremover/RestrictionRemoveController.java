package de.huwi.liberatepdf2.restservice.restrictionsremover;

import java.nio.file.Files;
import java.nio.file.Path;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import de.huwi.liberatepdf2.restservice.Pdf;
import de.huwi.liberatepdf2.restservice.PdfDTO;
import de.huwi.liberatepdf2.restservice.restrictionsremover.pdftk.PdftkRestrictionsRemover;
import de.huwi.liberatepdf2.restservice.storage.StorageService;

@RestController
@RequestMapping("/api/v1/documents/")
public class RestrictionRemoveController {

	private final StorageService storageService;

	@Autowired
	public RestrictionRemoveController(final StorageService storageService) {
		this.storageService = storageService;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{documentId}")
	public @ResponseBody FileSystemResource downloadUnrestricted(@PathVariable final Long documentId,
			final HttpServletResponse response) {
		final Path documentPath = this.storageService.load(documentId);

		if (Files.exists(documentPath) == false) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}

		response.setContentType("application/pdf");
		final String filename = this.storageService.getItem(documentId).getOriginalFilename();
		response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
		final FileSystemResource filesystemResource = new FileSystemResource(documentPath.toFile());

		return filesystemResource;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/")
	public long uploadAndRemoveRestrictions(final PdfDTO restrictedPdf) {
		final Pdf pdf = this.storageService.store(restrictedPdf.getFile());

		// start background task to remove restrictions
		final Runnable task = () -> {
			final String threadName = Thread.currentThread().getName();
			System.out.println(threadName);

			final RestrictionsRemover restrictionsRemover = new PdftkRestrictionsRemover();
			final Path unrestrictedPdfPath = restrictionsRemover.removeRestrictions(pdf.getRestrictedPath(),
					restrictedPdf.getPassword());

			pdf.setUnrectrictedPath(unrestrictedPdfPath);
			pdf.setDone(true);
		};

		task.run();

		final Thread thread = new Thread(task);
		thread.start();

		return pdf.getId();
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
