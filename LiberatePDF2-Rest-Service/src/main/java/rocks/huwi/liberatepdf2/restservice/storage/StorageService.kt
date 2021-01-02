package rocks.huwi.liberatepdf2.restservice.storage

import org.springframework.web.multipart.MultipartFile
import rocks.huwi.liberatepdf2.restservice.Pdf

/**
 * Service to provide read and write access to data.
 */
interface StorageService {
    /**
     * Deletes all data in the storage.
     */
    fun deleteAll()

    /**
     * Gets the Pdf of an item ID.
     *
     * @param itemId
     * ID of the item
     * @return the Pdf of the item ID, or null if not found
     */
    fun getItem(itemId: String): Pdf?

    /**
     * Gets the count of stored items.
     */
    val itemsCount: Long

    /**
     * Initializes the storage.
     */
    fun initialize()

    /**
     * Stores a file and its password
     *
     * @param file
     */
    fun store(file: MultipartFile, password: String?): Pdf
}