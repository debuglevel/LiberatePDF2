package rocks.huwi.liberatepdf2.restservice.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import rocks.huwi.liberatepdf2.restservice.Pdf;

/**
 * Storage service which uses the file system.
 */
@Service
public class FilesystemStorageService implements StorageService {

	private static final Logger log = LoggerFactory.getLogger(FilesystemStorageService.class);

	public static final String SUFFIX_PDF = ".pdf";
	public static final String SUFFIX_PDF_UNRESTRICTED = ".unrestricted.pdf";

	private final HashMap<String, Pdf> items = new HashMap<>();

	private final StorageProperties properties;

	@Autowired
	public FilesystemStorageService(final StorageProperties properties) {
		this.properties = properties;
	}

	@Override
	public void deleteAll() {
		log.debug("Deleting all files in {}", this.properties.getLocationPath());
		FileSystemUtils.deleteRecursively(this.properties.getLocationPath().toFile());
	}

	private String generateID() {
		return UUID.randomUUID().toString();
	}

	@Override
	public Pdf getItem(final String itemId) {
		Pdf pdf = this.items.get(itemId);
		log.debug("Getting PDF with ID={} from HashMap: {}", itemId, pdf);
		
		return pdf;
	}

	@Override
	public Long getItemsCount() {
		return (long) this.items.size();
	}

	@Override
	public void initialize() {
		log.debug("Initializing storage");

		log.debug("Clear on Initilization is set to " + this.properties.isClearOnInitialization());
		if (this.properties.isClearOnInitialization()) {
			log.debug("Deleting storage directory " + this.properties.getLocationPath());
			this.deleteAll();
		}

		try {
			if (Files.exists(this.properties.getLocationPath()) == false) {
				log.debug("Creating storage directory " + this.properties.getLocationPath());
				Files.createDirectory(this.properties.getLocationPath());
			} else {
				log.debug("Skipping creation of storage directory " + this.properties.getLocationPath()
						+ " because it already exists");
			}
		} catch (final IOException e) {
			throw new StorageException("Could not initialize storage", e);
		}
	}

	@Override
	public Pdf store(final MultipartFile file) {
		final String itemId = this.generateID();

		log.debug("Storing MultipartFile {} as ID={}", file.getName(), itemId);

		final Pdf pdf = new Pdf(itemId, file.getOriginalFilename());
		this.items.put(itemId, pdf);

		final Path itemLocation = this.properties.getLocationPath().resolve(itemId + SUFFIX_PDF);

		try {
			log.debug("Copying file {} to {}", file.getName(), itemLocation);
			Files.copy(file.getInputStream(), itemLocation);
		} catch (final IOException e) {
			log.error("Failed to store file " + file.getOriginalFilename(), e);
			throw new StorageException("Failed to store file " + file.getOriginalFilename(), e);
		}

		pdf.setRestrictedPath(itemLocation);

		return pdf;
	}
}