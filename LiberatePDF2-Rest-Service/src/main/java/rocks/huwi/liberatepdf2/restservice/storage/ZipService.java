package rocks.huwi.liberatepdf2.restservice.storage;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import org.apache.tomcat.util.buf.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import rocks.huwi.liberatepdf2.restservice.Pdf;

@Service
public class ZipService {
	private static final Logger log = LoggerFactory.getLogger(ZipService.class);

	private final StorageService storageService;
	private final StorageProperties properties;

	@Autowired
	public ZipService(final StorageService storageService, final StorageProperties properties) {
		this.storageService = storageService;
		this.properties = properties;
	}

	public Path createZip(String[] ids) throws IOException {
		log.debug("Creating ZIP file for ids " + StringUtils.join(ids));

		Map<String, String> env = new HashMap<>();
		env.put("create", "true");
		// locate file system by using the syntax
		// defined in java.net.JarURLConnection
		Path zipPath = properties.getLocationPath().resolve(StringUtils.join(ids));
		log.debug("Path of ZIP file: " + zipPath);

		URI uri = URI.create("jar:" + zipPath.toUri());
		log.debug("URI of ZIP file: " + uri);

		try (FileSystem zipFilesystem = FileSystems.newFileSystem(uri, env)) {
			for (String id : ids) {
				Pdf pdf = this.storageService.getItem(id);
				if (pdf != null) {
					Path pathInZipfile = zipFilesystem.getPath("/" + pdf.getOriginalFilename());

					log.debug("Copying PDF file {} into {} ", pdf.getUnrectrictedPath(), pathInZipfile);
					Files.copy(pdf.getUnrectrictedPath(), pathInZipfile, StandardCopyOption.REPLACE_EXISTING);
				}
			}
			
			zipFilesystem.close();
			
			return zipPath;
		}
	}
}
