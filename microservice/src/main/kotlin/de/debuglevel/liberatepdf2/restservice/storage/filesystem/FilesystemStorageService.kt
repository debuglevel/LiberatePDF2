package de.debuglevel.liberatepdf2.restservice.storage.filesystem

import de.debuglevel.liberatepdf2.restservice.storage.StorageService
import de.debuglevel.liberatepdf2.restservice.storage.StoredFile
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import javax.annotation.PostConstruct
import javax.inject.Singleton

/**
 * Storage service which uses the file system.
 */
@Singleton
class FilesystemStorageService(
    private val properties: StorageProperties
) : StorageService {
    private val logger = KotlinLogging.logger {}

    private val storedItems = HashMap<UUID, StoredFile>()
    override val storedItemsCount = storedItems.size.toLong()

    override fun deleteAll() {
        logger.debug { "Deleting all files in ${properties.locationPath}" }
        FileUtils.deleteDirectory(properties.locationPath?.toFile())
    }

    @PostConstruct
    override fun initialize() {
        logger.debug { "Initializing storage..." }

        logger.debug { "'Clear on Initialization' is set to ${properties.clearOnInitialization}" }
        if (properties.clearOnInitialization) {
            logger.debug { "Deleting storage directory '${properties.locationPath}'" }
            deleteAll()
        }

        try {
            if (!Files.exists(properties.locationPath)) {
                logger.debug { "Creating storage directory '${properties.locationPath}'..." }
                Files.createDirectory(properties.locationPath)
            } else {
                logger.debug { "Skipping creation of storage directory '${properties.locationPath}' because it already exists..." }
            }
        } catch (e: IOException) {
            throw StorageService.InitializationException(e)
        }

        // TODO: would be nice to index the files to restore the storage
    }

    override fun get(itemId: UUID): StoredFile {
        logger.debug { "Getting stored file with id=$itemId..." }
        val storedFile = storedItems.getOrElse(itemId) {
            logger.error { "No StoredFile found for id=$itemId" }
            throw StorageService.NotFoundException(itemId)
        }

        val storedFilePath = properties.locationPath.resolve("${storedFile.id}.pdf")
        val inputStream = try {
            storedFilePath.toFile().inputStream()
        } catch (e: FileNotFoundException) {
            throw FilesystemFileNotFound(storedFilePath, e)
        }

        val populatedStoredFile = storedFile.copy(inputStream = inputStream)

        logger.debug { "Got stored file with id=$itemId: $populatedStoredFile" }
        return populatedStoredFile
    }

    override fun store(filename: String, inputStream: InputStream, password: String): StoredFile {
        val id = UUID.randomUUID()
        logger.debug { "Storing '$filename' as id=${id}..." }

        try {
            val filePath = storeFile(id, inputStream, properties.locationPath)
            val passwordPath = storePassword(id, password, properties.locationPath)

            val fileSize = filePath.toFile().length()
            val storedFile = StoredFile(id, filename, fileSize)
            storedItems[id] = storedFile

            return get(storedFile.id) // call get() to populate inputStream of StoredFile
        } catch (e: IOException) {
            logger.error(e) { "Failed to store file $filename ($id)" }
            throw StorageService.StoreException(filename, e)
        }
    }

    private fun storeFile(
        id: UUID,
        inputStream: InputStream,
        storageLocation: Path,
    ): Path {
        val filename = "$id.pdf"
        val path = storageLocation.resolve(filename)
        logger.debug { "Writing file id=$id to '$path'..." }
        Files.copy(inputStream, path)
        logger.debug { "Wrote file id=$id to '$path'" }
        return path
    }

    private fun storePassword(
        id: UUID,
        password: String,
        storageLocation: Path,
    ): Path {
        val filename = "$id.password"
        val path = storageLocation.resolve(filename)
        logger.debug { "Saving password id=$id to $path..." }
        path.toFile().writeText(password)
        logger.debug { "Saved password id=$id to $path..." }
        return path
    }

    data class FilesystemFileNotFound(val path: Path, val inner: Exception) :
        Exception("No file found at $path (inner exception: $inner)", inner)
}