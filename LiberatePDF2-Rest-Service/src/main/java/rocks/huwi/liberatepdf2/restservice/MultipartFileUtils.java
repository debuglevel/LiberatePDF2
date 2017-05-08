package rocks.huwi.liberatepdf2.restservice;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

public class MultipartFileUtils {

	private static final Logger log = LoggerFactory.getLogger(MultipartFileUtils.class);

	/**
	 * Returns a Path for a MultipartFile by copying/moving its content.
	 *
	 * @param multipartFile
	 * @return
	 * @throws IOException
	 */
	public static Path getPath(final MultipartFile multipartFile) throws IOException {
		final Path tempDirectory = Files.createTempDirectory("LiberatePDF2");

		final Path tempPath = tempDirectory.resolve(multipartFile.getOriginalFilename());

		log.debug("Moving MultipartFile {} to {}", multipartFile.getName(), tempPath);
		multipartFile.transferTo(tempPath.toFile());

		return tempPath;
	}

	// /**
	// * Returns Paths for MultipartFiles by copying/moving their content.
	// *
	// * @param restrictedPdf
	// * @return
	// * @throws IOException
	// */
	// public static ArrayList<Path> getPaths(final MultipartFile[]
	// multipartFiles) throws IOException {
	// final ArrayList<Path> restrictedPdfsTempPaths = new ArrayList<>();
	//
	// for (final MultipartFile multipartFile : multipartFiles) {
	// final Path restrictedPdfTempPath =
	// MultipartFileUtils.getPath(multipartFile);
	// restrictedPdfsTempPaths.add(restrictedPdfTempPath);
	// }
	// return restrictedPdfsTempPaths;
	// }
}
