package de.debuglevel.liberatepdf2.restservice.storage

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
    private val storageService: StorageService,
    private val storageProperties: StorageProperties
) {
    private val logger = KotlinLogging.logger {}

    fun createZip(ids: Array<UUID>): Path {
        logger.debug { "Creating ZIP file for documents ${ids.joinToString()}..." }

        val properties: MutableMap<String, String?> = HashMap()
        properties["create"] = "true"

        // locate file system by using the syntax defined in java.net.JarURLConnection
        val zipPath = this.storageProperties.locationPath.resolve(UUID.randomUUID().toString())
        logger.debug { "Path of ZIP file: $zipPath" }

        val uri = URI.create("jar:" + zipPath.toUri())
        logger.debug { "URI of ZIP file: $uri" }

        return FileSystems.newFileSystem(uri, properties).use { zipFilesystem ->
            for (id in ids) {
                storageService.get(id)?.let { pdf ->
                    val pathInZipFile = zipFilesystem.getPath("/${pdf.originalFilename}")
                    logger.debug { "Copying PDF file ${pdf.unrestrictedPath} into $pathInZipFile..." }
                    Files.copy(pdf.unrestrictedPath, pathInZipFile, StandardCopyOption.REPLACE_EXISTING)
                }
            }
            zipPath
        }
    }
}