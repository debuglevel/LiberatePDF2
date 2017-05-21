package rocks.huwi.liberatepdf2.restservice.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

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
public class FilesystemTemporaryStorageService implements StorageService {

	private static final Logger log = LoggerFactory.getLogger(FilesystemTemporaryStorageService.class);

	public static final String SUFFIX_PDF = ".pdf";
	public static final String SUFFIX_PDF_UNRESTRICTED = ".unrestricted.pdf";

	private final AtomicLong count = new AtomicLong();

	private final HashMap<String, Pdf> items = new HashMap<>();

	private final Path rootLocation;

	@Autowired
	public FilesystemTemporaryStorageService(final StorageProperties properties) {
		this.rootLocation = Paths.get(properties.getLocation());
	}

	@Override
	public void deleteAll() {
		log.debug("Deleting all files in {}", this.rootLocation);
		FileSystemUtils.deleteRecursively(this.rootLocation.toFile());
	}

	@Override
	public Pdf getItem(final String itemId) {
		log.debug("Getting PDF with ID={} from HashMap", itemId);
		return this.items.get(itemId);
	}

	@Override
	public void init() {
		log.debug("Initializing storage");
		try {
			Files.createDirectory(this.rootLocation);
		} catch (final IOException e) {
			throw new StorageException("Could not initialize storage", e);
		}
	}

	// @Override
	// public Stream<Path> loadAll() {
	// try {
	// return Files.walk(this.rootLocation, 1).filter(path ->
	// !path.equals(this.rootLocation))
	// .map(path -> this.rootLocation.relativize(path));
	// } catch (final IOException e) {
	// throw new StorageException("Failed to read stored files", e);
	// }
	// }

	// @Override
	// public Resource loadAsResource(final long itemId) {
	// try {
	// final Path file = this.getItem(itemId).getUnrectrictedPath();
	// final Resource resource = new UrlResource(file.toUri());
	// if (resource.exists() || resource.isReadable()) {
	// return resource;
	// } else {
	// throw new StorageFileNotFoundException("Could not read file: " + itemId);
	//
	// }
	// } catch (final MalformedURLException e) {
	// throw new StorageFileNotFoundException("Could not read file: " + itemId,
	// e);
	// }
	// }
	
	private String generateID()
	{
		return UUID.randomUUID().toString();
	}

	@Override
	public Pdf store(final MultipartFile file) {
		final String itemId = this.generateID();
		this.count.incrementAndGet();

		log.debug("Storing MultipartFile {} as ID={}", file.getName(), itemId);

		final Pdf pdf = new Pdf(itemId, file.getOriginalFilename());
		this.items.put(itemId, pdf);

		final Path itemLocation = this.rootLocation.resolve(itemId + SUFFIX_PDF);

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