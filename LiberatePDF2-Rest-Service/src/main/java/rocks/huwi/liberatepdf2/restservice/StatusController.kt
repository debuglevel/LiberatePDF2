package rocks.huwi.liberatepdf2.restservice;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import rocks.huwi.liberatepdf2.restservice.restrictionsremover.RestrictionsRemoverService;
import rocks.huwi.liberatepdf2.restservice.storage.StorageService;

@RestController
@RequestMapping("/api/v1/status/")
public class StatusController {
	private static final Logger log = LoggerFactory.getLogger(StatusController.class);

	private final Environment environment;
	private final RestrictionsRemoverService restrictionsRemoverService;
	private final StorageService storageService;

	@Autowired
	public StatusController(final StorageService storageService,
			final RestrictionsRemoverService restrictionsRemoverService, final Environment environment) {
		this.storageService = storageService;
		this.restrictionsRemoverService = restrictionsRemoverService;
		this.environment = environment;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/maximum-upload-size")
	public ResponseEntity<?> maximumUploadSize() {
		log.debug("Received GET request for maximum-upload-size");

		// fetch the values of those two properties and take the smaller one
		final String size1 = this.environment.getProperty("spring.http.multipart.max-file-size");
		final String size2 = this.environment.getProperty("spring.http.multipart.max-request-size");

		final Long size = Math.min(Long.valueOf(size1), Long.valueOf(size2));

		return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(size);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/ping/{message}")
	public ResponseEntity<?> ping(@PathVariable final String message, final HttpServletResponse response) {
		log.debug("Received GET request for ping {}", message);

		return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body("pong: " + message);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/statistics")
	public ResponseEntity<?> statistics() throws JSONException {
		log.debug("Received GET request for statistics");

		final JSONObject json = new JSONObject();
		json.put("storedItems", this.storageService.getItemsCount());
		json.put("processedItems", this.restrictionsRemoverService.getItemsCount());
		json.put("failedItems", this.restrictionsRemoverService.getFailedItemsCount());

		return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(json.toString());
	}
}
