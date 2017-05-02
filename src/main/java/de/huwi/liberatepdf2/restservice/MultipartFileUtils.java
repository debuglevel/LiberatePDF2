package de.huwi.liberatepdf2.restservice;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.springframework.web.multipart.MultipartFile;

public class MultipartFileUtils {
	/**
	 * Returns a Path for a MultipartFile by copying/moving its content.
	 * @param multipartFile
	 * @return
	 * @throws IOException
	 */
	public static Path getPath(MultipartFile multipartFile) throws IOException {
		Path tempDirectory = Files.createTempDirectory("LiberatePDF2");
		
		Path tempPath = tempDirectory.resolve(multipartFile.getOriginalFilename());
		multipartFile.transferTo(tempPath.toFile());
		
		return tempPath;
	}
	
	/**
	 * Returns Paths for MultipartFiles by copying/moving their content.
	 * @param restrictedPdf
	 * @return
	 * @throws IOException
	 */
	public static ArrayList<Path> getPaths(MultipartFile[] multipartFiles) throws IOException {
		final ArrayList<Path> restrictedPdfsTempPaths = new ArrayList<Path>(); 
		
		for (MultipartFile multipartFile : multipartFiles) {
			final Path restrictedPdfTempPath = MultipartFileUtils.getPath(multipartFile);
			restrictedPdfsTempPaths.add(restrictedPdfTempPath);
		}
		return restrictedPdfsTempPaths;
	}
}
