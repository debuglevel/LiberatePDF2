package de.huwi.liberatepdf2.restservice.storage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import de.huwi.liberatepdf2.restservice.Pdf;

/**
 * Storage service which uses the file system.
 */
@Service
public class FilesystemStorageService implements StorageService {

	public static final String SUFFIX_PDF = ".pdf";
	public static final String SUFFIX_PDF_UNRESTRICTED = ".unrestricted.pdf";

	private final AtomicLong itemId = new AtomicLong();

	private final HashMap<Long, Pdf> items = new HashMap<>();

	private final Path rootLocation;

	@Autowired
	public FilesystemStorageService(final StorageProperties properties) {
		this.rootLocation = Paths.get(properties.getLocation());
	}

	@Override
	public void deleteAll() {
		FileSystemUtils.deleteRecursively(this.rootLocation.toFile());
	}

	@Override
	public Pdf getItem(final Long itemId) {
		return this.items.get(itemId);
	}

	@Override
	public void init() {
		try {
			Files.createDirectory(this.rootLocation);
		} catch (final IOException e) {
			throw new StorageException("Could not initialize storage", e);
		}
	}

//	@Override
//	public Path getPath(final long itemId) {
//		return this.rootLocation.resolve(itemId + SUFFIX_PDF + SUFFIX_PDF_UNRESTRICTED);
//	}

	@Override
	public Stream<Path> loadAll() {
		try {
			return Files.walk(this.rootLocation, 1).filter(path -> !path.equals(this.rootLocation))
					.map(path -> this.rootLocation.relativize(path));
		} catch (final IOException e) {
			throw new StorageException("Failed to read stored files", e);
		}
	}

	@Override
	public Resource loadAsResource(final long itemId) {
		try {
			final Path file = this.getItem(itemId).getUnrectrictedPath();
			final Resource resource = new UrlResource(file.toUri());
			if (resource.exists() || resource.isReadable()) {
				return resource;
			} else {
				throw new StorageFileNotFoundException("Could not read file: " + itemId);

			}
		} catch (final MalformedURLException e) {
			throw new StorageFileNotFoundException("Could not read file: " + itemId, e);
		}
	}

	@Override
	public Pdf store(final MultipartFile file) {
		final long itemId = this.itemId.incrementAndGet();

		final Pdf pdf = new Pdf(itemId, file.getOriginalFilename());
		this.items.put(itemId, pdf);

		final Path itemLocation = this.rootLocation.resolve(itemId + SUFFIX_PDF);

		try {
			Files.copy(file.getInputStream(), itemLocation);
		} catch (final IOException e) {
			throw new StorageException("Failed to store file " + file.getOriginalFilename(), e);
		}

		pdf.setRestrictedPath(itemLocation);

		return pdf;
	}
}