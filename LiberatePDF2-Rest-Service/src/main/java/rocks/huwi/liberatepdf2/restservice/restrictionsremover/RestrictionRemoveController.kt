package rocks.huwi.liberatepdf2.restservice.restrictionsremover

import org.apache.tomcat.util.buf.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.FileSystemResource
import org.springframework.core.task.TaskExecutor
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import rocks.huwi.liberatepdf2.restservice.PdfDTO
import rocks.huwi.liberatepdf2.restservice.storage.StorageService
import rocks.huwi.liberatepdf2.restservice.storage.ZipService
import java.io.IOException
import java.nio.file.Files
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/api/v1/documents/")
class RestrictionRemoveController @Autowired constructor(
    private val storageService: StorageService,
    private val restrictionsRemoverService: RestrictionsRemoverService,
    private val zipService: ZipService,
    taskExecutor: TaskExecutor?
) {
    @RequestMapping(method = [RequestMethod.GET], value = ["/zip"])
    @Throws(IOException::class)
    fun downloadZip(
        @RequestParam id: Array<String>,
        response: HttpServletResponse?
    ): ResponseEntity<*> {
        log.debug("Received GET or HEAD request for multiple {} documents {}", id.size, StringUtils.join(id))
        val zip = zipService.createZip(id)
        val filesystemResource = FileSystemResource(zip.toFile())
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition", "attachment; filename=\"" + "unrestricted PDFs.zip" + "\"")
            .body(filesystemResource)
    }

    @RequestMapping(method = [RequestMethod.GET, RequestMethod.HEAD], value = ["/{documentId}"])
    @Throws(IOException::class)
    fun downloadUnrestricted(
        @PathVariable documentId: String?,
        response: HttpServletResponse?
    ): ResponseEntity<*> {
        log.debug("Received GET or HEAD request for document {}", documentId)
        val pdf = storageService.getItem(documentId!!)
        return if (pdf == null) {
            // no item found with this ID (because no request was assigned this
            // ID by now)
            log.debug("No document with ID={} found", documentId)
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(String.format("No document found for ID=%s", documentId))
        } else if (pdf.isDone == false) {
            // the request exists, but was not transformed by now
            log.debug(
                "Document with ID={} found, but pdf.isDone=false (not processed by now)",
                documentId
            )
            ResponseEntity.status(HTTP_STATUS_IN_PROGRESS)
                .body("The document was not processed by now. Please try again later.")
        } else if (Files.exists(pdf.unrectrictedPath) == false) {
            // the request was transformed, but the file does not exist (somehow
            // failed?)
            log.debug("Document with ID={} found, but no file exists", documentId)
            ResponseEntity.status(HTTP_STATUS_FAILED).body(
                "The document was processed, but produced no result. Maybe the password was wrong or another error occurred."
            )
        } else {
            // request should be okay
            val filename = storageService.getItem(documentId)!!.originalFilename
            val filesystemResource = FileSystemResource(pdf.unrectrictedPath!!.toFile())
            log.debug("Document with ID={} found and set for delivery", documentId)
            ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "attachment; filename=\"$filename\"")
                .body(filesystemResource)
        }
    }

    @RequestMapping(method = [RequestMethod.POST], value = ["/"])
    fun uploadAndRemoveRestrictions(
        restrictedPdf: PdfDTO,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<*> {
        log.debug("Received POST request for document {}", restrictedPdf.file!!.name)
        val pdf = storageService.store(restrictedPdf.file!!, restrictedPdf.password)
        pdf.password = restrictedPdf.password
        restrictionsRemoverService.removeRestrictionsAsync(pdf)
        val uriComponents = uriComponentsBuilder.path("/api/v1/documents/{id}")
            .buildAndExpand(pdf.id)
        return ResponseEntity.accepted().location(uriComponents.toUri()).body(pdf.id)
    }

    companion object {
        private const val HTTP_STATUS_FAILED = 560
        private const val HTTP_STATUS_IN_PROGRESS = 260
        private val log = LoggerFactory.getLogger(RestrictionRemoveController::class.java)
    }
}