package de.debuglevel.liberatepdf2.restservice.storage

import de.debuglevel.liberatepdf2.restservice.Pdf
import java.io.InputStream
import java.util.*

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
     * @param itemId ID of the item
     * @return the Pdf of the item ID, or null if not found
     */
    fun getItem(itemId: UUID): Pdf?

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
    fun store(filename: String, inputStream: InputStream, password: String): Pdf

    data class StorageException(val msg: String, val inner: Throwable) : Exception(msg, inner)
}