package rocks.huwi.liberatepdf2.restservice.storage;

import org.springframework.web.multipart.MultipartFile;

import rocks.huwi.liberatepdf2.restservice.Pdf;

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
	Pdf getItem(String itemId);

	/**
	 * Gets the count of stored items.
	 */
	Long getItemsCount();

	/**
	 * Initializes the storage.
	 */
	void initialize();

	/**
	 * Stores a file and its password
	 *
	 * @param file
	 */
	Pdf store(MultipartFile file, String password);
}
