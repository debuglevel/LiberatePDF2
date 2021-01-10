package de.debuglevel.liberatepdf2.restservice.storage

import de.debuglevel.liberatepdf2.restservice.Pdf
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
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

    private val storedItems = HashMap<UUID, Pdf>()
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
    }

    override fun get(itemId: UUID): Pdf {
        logger.debug { "Getting stored file with id=$itemId..." }
        val pdf = storedItems.getOrElse(itemId) {
            logger.error { "No file found for id=$itemId" }
            throw StorageService.NotFoundException(itemId)
        }
        logger.debug { "Got stored file with id=$itemId..." }
        return pdf
    }

    override fun store(filename: String, inputStream: InputStream, password: String): Pdf {
        val itemId = UUID.randomUUID()
        logger.debug { "Storing '$filename' as id=${itemId}..." }

        val pdf = Pdf(itemId, filename)

        val idFilename = "$itemId.pdf"
        val filePath = properties.locationPath.resolve(idFilename)
        try {
            logger.debug { "Writing file '$filename' to '$filePath'..." }
            Files.copy(inputStream, filePath)

            // storing password is not necessary for PDFtk, but maybe useful to persist
            if (password.isNotEmpty()) {
                val passwordPath = filePath.resolveSibling("${filePath.fileName}.password")
                logger.debug { "Saving password to $passwordPath..." }

                passwordPath.toFile().writeText(password)
            }
        } catch (e: IOException) {
            logger.error(e) { "Failed to store file $filename" }
            throw StorageService.StoreException(filename, e)
        }

        pdf.restrictedPath = filePath
        storedItems[itemId] = pdf

        return pdf
    }
}