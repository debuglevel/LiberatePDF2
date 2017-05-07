package de.huwi.liberatepdf2.restservice.storage;

import java.nio.file.Path;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import de.huwi.liberatepdf2.restservice.Pdf;

/**
 * Service to provide read and write access to data.
 */
public interface StorageService {

	/**
	 * Deletes all data in the storage.
	 */
	void deleteAll();

	/**
	 * Gets the Pdf of an item ID.
	 *
	 * @param itemId
	 *            ID of the item
	 * @return the Pdf of the item ID, or null if not found
	 */
	Pdf getItem(Long itemId);

	/**
	 * Initializes the storage.
	 */
	void init();

	// /**
	// * Returns the path of an item
	// *
	// * @param itemId
	// * the ID of an item
	// * @return
	// */
	// Path getPath(long itemId);

	Stream<Path> loadAll();

	Resource loadAsResource(long itemId);

	/**
	 * Stores a file
	 *
	 * @param file
	 */
	Pdf store(MultipartFile file);
}
