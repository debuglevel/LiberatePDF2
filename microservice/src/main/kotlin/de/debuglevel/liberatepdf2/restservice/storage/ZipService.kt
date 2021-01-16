package de.debuglevel.liberatepdf2.restservice.storage

import de.debuglevel.liberatepdf2.restservice.storage.filesystem.StorageProperties
import mu.KotlinLogging
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.*
import javax.inject.Singleton

@Singleton
class ZipService(
    private val storageProperties: StorageProperties
) {
    private val logger = KotlinLogging.logger {}

    fun createZip(storedFiles: Collection<StoredFile>): Path {
        logger.debug { "Creating ZIP file for ${storedFiles.size} files..." }

        val properties: MutableMap<String, String?> = HashMap()
        properties["create"] = "true"

        // locate file system by using the syntax defined in java.net.JarURLConnection
        val zipPath = this.storageProperties.locationPath.resolve(UUID.randomUUID().toString())
        logger.debug { "Path of ZIP file: $zipPath" }

        val uri = URI.create("jar:" + zipPath.toUri())
        logger.debug { "URI of ZIP file: $uri" }

        // TODO: create ZIP in memory or even better into an OutputStream
        return FileSystems.newFileSystem(uri, properties).use { zipFilesystem ->
            for (storedFile in storedFiles) {
                val pathInZipFile = zipFilesystem.getPath("/${storedFile.filename}")
                logger.debug { "Copying input stream into $pathInZipFile..." }
                Files.copy(storedFile.inputStream, pathInZipFile, StandardCopyOption.REPLACE_EXISTING)
            }
            zipPath
        }
    }
}