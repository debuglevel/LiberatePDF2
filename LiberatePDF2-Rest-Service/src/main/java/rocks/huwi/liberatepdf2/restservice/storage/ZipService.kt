package rocks.huwi.liberatepdf2.restservice.storage

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
    private val properties: StorageProperties
) {
    private val logger = KotlinLogging.logger {}

    fun createZip(ids: Array<UUID>): Path {
        logger.debug { ("Creating ZIP file for ids ${ids.joinToString()}") }
        val env: MutableMap<String, String?> = HashMap()
        env["create"] = "true"
        // locate file system by using the syntax
        // defined in java.net.JarURLConnection
        val zipPath = properties.locationPath?.resolve(ids.joinToString(""))

        logger.debug { ("Path of ZIP file: $zipPath") }
        val uri = URI.create("jar:" + zipPath?.toUri())

        logger.debug { ("URI of ZIP file: $uri") }
        FileSystems.newFileSystem(uri, env).use { zipFilesystem ->
            for (id in ids) {
                val pdf = storageService.getItem(id)
                if (pdf != null) {
                    val pathInZipfile = zipFilesystem.getPath("/${pdf.originalFilename}")
                    logger.debug { ("Copying PDF file {} into {} " + pdf.unrestrictedPath + pathInZipfile) }
                    Files.copy(pdf.unrestrictedPath, pathInZipfile, StandardCopyOption.REPLACE_EXISTING)
                }
            }
            zipFilesystem.close()
            return zipPath!!
        }
    }
}