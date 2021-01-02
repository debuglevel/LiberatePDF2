package rocks.huwi.liberatepdf2.restservice

import org.json.JSONException
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import rocks.huwi.liberatepdf2.restservice.restrictionsremover.RestrictionsRemoverService
import rocks.huwi.liberatepdf2.restservice.storage.StorageService
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/api/v1/status/")
class StatusController @Autowired constructor(
    private val storageService: StorageService,
    private val restrictionsRemoverService: RestrictionsRemoverService, private val environment: Environment
) {
    @RequestMapping(method = [RequestMethod.GET], value = ["/maximum-upload-size"])
    fun maximumUploadSize(): ResponseEntity<*> {
        log.debug("Received GET request for maximum-upload-size")

        // fetch the values of those two properties and take the smaller one
        val size1 = environment.getProperty("spring.http.multipart.max-file-size")
        val size2 = environment.getProperty("spring.http.multipart.max-request-size")
        val size = Math.min(java.lang.Long.valueOf(size1), java.lang.Long.valueOf(size2))
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(size)
    }

    @RequestMapping(method = [RequestMethod.GET], value = ["/ping/{message}"])
    fun ping(@PathVariable message: String, response: HttpServletResponse?): ResponseEntity<*> {
        log.debug("Received GET request for ping {}", message)
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body("pong: $message")
    }

    @RequestMapping(method = [RequestMethod.GET], value = ["/statistics"])
    @Throws(JSONException::class)
    fun statistics(): ResponseEntity<*> {
        log.debug("Received GET request for statistics")
        val json = JSONObject()
        json.put("storedItems", storageService.itemsCount)
        json.put("processedItems", restrictionsRemoverService.itemsCount)
        json.put("failedItems", restrictionsRemoverService.failedItemsCount)
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(json.toString())
    }

    companion object {
        private val log = LoggerFactory.getLogger(StatusController::class.java)
    }
}