package de.debuglevel.liberatepdf2.restservice.storage

import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import de.debuglevel.liberatepdf2.restservice.Pdf
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.util.*
import javax.annotation.PostConstruct
import javax.inject.Singleton

/**
 * Storage service which uses the file system.
 */
@Singleton
class FilesystemStorageService(private val properties: StorageProperties) : StorageService {
    private val logger = KotlinLogging.logger {}

    private val SUFFIX_PDF = ".pdf"

    private val items = HashMap<UUID, Pdf>()

    override fun deleteAll() {
        logger.debug { "Deleting all files in " + properties.locationPath }
        FileUtils.deleteDirectory(properties.locationPath?.toFile())
    }

    private fun generateID(): UUID {
        return UUID.randomUUID()
    }

    override fun getItem(itemId: UUID): Pdf? {
        val pdf = items[itemId]
        logger.debug { "Getting PDF with ID=$itemId from HashMap: $pdf" }
        return pdf
    }

    override val itemsCount = items.size.toLong()

    @PostConstruct
    override fun initialize() { // TODO: was probably executed automatically on Spring Boot
        logger.debug { "Initializing storage" }

        logger.debug { "'Clear on Initilization' is set to " + properties.isClearOnInitialization }
        if (properties.isClearOnInitialization) {
            logger.debug { "Deleting storage directory ${properties.locationPath}" }
            deleteAll()
        }
        try {
            if (!Files.exists(properties.locationPath)) {
                logger.debug { "Creating storage directory ${properties.locationPath}" }
                Files.createDirectory(properties.locationPath)
            } else {
                logger.debug { "Skipping creation of storage directory ${properties.locationPath} because it already exists" }
            }
        } catch (e: IOException) {
            throw StorageException("Could not initialize storage", e)
        }
    }

    override fun store(filename: String, inputStream: InputStream, password: String): Pdf {
        val itemId = generateID()

        logger.debug { "Storing MultipartFile $filename as ID=${itemId}" }
        val pdf = Pdf(itemId, filename)
        items[itemId] = pdf
        val itemLocation = properties.locationPath.resolve(itemId.toString() + SUFFIX_PDF)
        try {
            logger.debug { "Copying file $filename to $itemLocation" }
            Files.copy(inputStream, itemLocation)
            if (password.isNotEmpty()) {
                val passwordLocation = itemLocation.resolveSibling(itemLocation.fileName.toString() + ".password")
                logger.debug { ("Save password into $passwordLocation") }
                passwordLocation.toFile().writeText(password)
            }
        } catch (e: IOException) {
            logger.error(e) { "Failed to store file $filename" }
            throw StorageException("Failed to store file $filename", e)
        }
        pdf.restrictedPath = itemLocation
        return pdf
    }
}