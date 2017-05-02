package de.huwi.liberatepdf2.restservice;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/restrictions-remover/")
public class RestrictionRemoveController {

	@RequestMapping(value = "/")
	public @ResponseBody FileSystemResource removeRestrictions(PdfsDTO restrictedPdfs, HttpServletResponse response)
			throws IOException {
		
		if (restrictedPdfs.getFiles() == null || restrictedPdfs.getFiles().length == 0)
		{
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
		
		final ArrayList<Path> restrictedPdfsTempPaths = MultipartFileUtils.getPaths(restrictedPdfs.getFiles());
		
		
		final RestrictionsRemover restrictionsRemover = new PdftkRestrictionsRemover();
		final Path unrestrictedPdfPath = restrictionsRemover.removeRestrictions(restrictedPdfsTempPaths, restrictedPdfs.getPassword());
		
		final FileSystemResource fileSystemResource = new FileSystemResource(unrestrictedPdfPath.toFile());
		
		
		String unrestrictedFilename = "unknown";
		if (restrictedPdfsTempPaths.size() == 1)
		{
			unrestrictedFilename = restrictedPdfs.getFiles()[0].getOriginalFilename() + " (unrestricted).pdf";
			response.setContentType("application/pdf");
		}else if (restrictedPdfsTempPaths.size() > 1) {
			unrestrictedFilename = "PDFs (unrestricted).zip";
			response.setContentType("application/zip");
		}else{
			
		}
		response.setHeader("Content-Disposition", "attachment; filename=\"" + unrestrictedFilename + "\"");

		return fileSystemResource;
	}



}
