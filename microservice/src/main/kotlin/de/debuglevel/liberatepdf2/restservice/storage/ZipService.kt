package de.debuglevel.liberatepdf2.restservice.storage

import mu.KotlinLogging
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Singleton

@Singleton
class ZipService {
    private val logger = KotlinLogging.logger {}

    data class ZipItem(val filename: String, val inputStream: InputStream)

    /**
     * Archives the [zipItems] into a .zip and writes it into the [outputStream]
     */
    fun writeZip(zipItems: Set<ZipItem>, zipOutputStream: OutputStream) {
        logger.debug { "Creating zip file..." }

        ZipOutputStream(BufferedOutputStream(zipOutputStream)).use { outputStream ->
            for (zipItem in zipItems) {
                zipItem.inputStream.use { inputStream ->
                    BufferedInputStream(inputStream).use { bufferedInputStream ->
                        logger.trace { "Adding zip entry with filename '${zipItem.filename}'..." }

                        val zipEntry = ZipEntry(zipItem.filename)
                        outputStream.putNextEntry(zipEntry)
                        bufferedInputStream.copyTo(outputStream, 1024)

                        logger.trace { "Added zip entry with filename '${zipItem.filename}'" }
                    }
                }
            }
        }

        logger.debug { "Created zip file" }
    }
}

//@Singleton
//class ZipService() {
//    private val logger = KotlinLogging.logger {}
//
//    fun createZip(storedFiles: Collection<StoredFile>): Path {
//        logger.debug { "Creating ZIP file for ${storedFiles.size} files..." }
//
//        val properties: MutableMap<String, String?> = HashMap()
//        properties["create"] = "true"
//
//        // locate file system by using the syntax defined in java.net.JarURLConnection
//        val zipPath = this.storageProperties.locationPath.resolve(UUID.randomUUID().toString())
//        logger.debug { "Path of ZIP file: $zipPath" }
//
//        val uri = URI.create("jar:" + zipPath.toUri())
//        logger.debug { "URI of ZIP file: $uri" }
//
//        // TODO: create ZIP in memory or even better into an OutputStream
//        return FileSystems.newFileSystem(uri, properties).use { zipFilesystem ->
//            for (storedFile in storedFiles) {
//                val pathInZipFile = zipFilesystem.getPath("/${storedFile.filename}")
//                logger.debug { "Copying input stream into $pathInZipFile..." }
//                Files.copy(storedFile.inputStream, pathInZipFile, StandardCopyOption.REPLACE_EXISTING)
//            }
//            zipPath
//        }
//    }
//}