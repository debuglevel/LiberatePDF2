package rocks.huwi.liberatepdf2.restservice.storage

import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.util.FileSystemUtils
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import rocks.huwi.liberatepdf2.restservice.Pdf
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Files
import java.util.*

/**
 * Storage service which uses the file system.
 */
@Service
class FilesystemStorageService @Autowired constructor(private val properties: StorageProperties) : StorageService {
    private val log = LoggerFactory.getLogger(FilesystemStorageService::class.java)
    val SUFFIX_PDF = ".pdf"


    private val items = HashMap<String, Pdf>()
    override fun deleteAll() {
        log.debug("Deleting all files in {}", properties.locationPath)
        FileSystemUtils.deleteRecursively(properties.locationPath?.toFile())
    }

    private fun generateID(): String {
        return UUID.randomUUID().toString()
    }

    override fun getItem(itemId: String): Pdf? {
        val pdf = items[itemId]
        log.debug("Getting PDF with ID={} from HashMap: {}", itemId, pdf)
        return pdf
    }

    override val itemsCount
        get() = items.size.toLong()

    override fun initialize() {
        log.debug("Initializing storage")

        log.debug("'Clear on Initilization' is set to " + properties.isClearOnInitialization)
        if (properties.isClearOnInitialization) {
            log.debug("Deleting storage directory " + properties.locationPath)
            deleteAll()
        }
        try {
            if (!Files.exists(properties.locationPath)) {
                log.debug("Creating storage directory " + properties.locationPath)
                Files.createDirectory(properties.locationPath)
            } else {
                log.debug(
                    "Skipping creation of storage directory " + properties.locationPath
                            + " because it already exists"
                )
            }
        } catch (e: IOException) {
            throw StorageException("Could not initialize storage", e)
        }
    }

    override fun store(file: MultipartFile, password: String?): Pdf {
        val itemId = generateID()

        log.debug("Storing MultipartFile {} as ID={}", file.name, itemId)
        val pdf = Pdf(itemId, file.originalFilename)
        items[itemId] = pdf
        val itemLocation = properties.locationPath?.resolve(itemId + SUFFIX_PDF)
        try {
            log.debug("Copying file {} to {}", file.name, itemLocation)
            Files.copy(file.inputStream, itemLocation)
            if (!StringUtils.isEmpty(password)) {
                val passwordLocation = itemLocation?.resolveSibling(itemLocation.fileName.toString() + ".password")
                log.debug("Save password into $passwordLocation")
                FileUtils.writeStringToFile(passwordLocation?.toFile(), password, Charset.defaultCharset())
            }
        } catch (e: IOException) {
            log.error("Failed to store file " + file.originalFilename, e)
            throw StorageException("Failed to store file " + file.originalFilename, e)
        }
        pdf.restrictedPath = itemLocation
        return pdf
    }
}