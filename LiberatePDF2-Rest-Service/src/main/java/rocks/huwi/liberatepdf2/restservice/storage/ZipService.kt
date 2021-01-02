package rocks.huwi.liberatepdf2.restservice.storage

import org.apache.tomcat.util.buf.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.IOException
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.*

@Service
class ZipService @Autowired constructor(
    private val storageService: StorageService,
    private val properties: StorageProperties
) {
    @Throws(IOException::class)
    fun createZip(ids: Array<String>): Path {
        log.debug("Creating ZIP file for ids " + StringUtils.join(ids))
        val env: MutableMap<String, String?> = HashMap()
        env["create"] = "true"
        // locate file system by using the syntax
        // defined in java.net.JarURLConnection
        val zipPath = properties.locationPath?.resolve(StringUtils.join(ids))
        log.debug("Path of ZIP file: $zipPath")
        val uri = URI.create("jar:" + zipPath?.toUri())
        log.debug("URI of ZIP file: $uri")
        FileSystems.newFileSystem(uri, env).use { zipFilesystem ->
            for (id in ids) {
                val pdf = storageService.getItem(id)
                if (pdf != null) {
                    val pathInZipfile = zipFilesystem.getPath("/" + pdf.originalFilename)
                    log.debug("Copying PDF file {} into {} ", pdf.unrestrictedPath, pathInZipfile)
                    Files.copy(pdf.unrestrictedPath, pathInZipfile, StandardCopyOption.REPLACE_EXISTING)
                }
            }
            zipFilesystem.close()
            return zipPath!!
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(ZipService::class.java)
    }
}