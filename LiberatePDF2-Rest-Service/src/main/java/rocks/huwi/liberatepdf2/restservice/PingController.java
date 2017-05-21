package rocks.huwi.liberatepdf2.restservice;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/info/")
public class PingController {
	private static final Logger log = LoggerFactory.getLogger(PingController.class);

	@RequestMapping(method = RequestMethod.GET, value = "/ping/{message}")
	public ResponseEntity<?> ping(@PathVariable final String message,
			final HttpServletResponse response) throws IOException {
		log.debug("Received GET request for ping {}", message);

			return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN)
					.body("pong: " + message);
	}
}
