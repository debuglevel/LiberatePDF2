package de.debuglevel.liberatepdf2.restservice.configuration

import io.micronaut.http.server.HttpServerConfiguration
import io.micronaut.runtime.server.EmbeddedServer
import mu.KotlinLogging
import javax.inject.Singleton

@Singleton
class ConfigurationService(
    private val embeddedServer: EmbeddedServer,
    httpServerConfiguration: HttpServerConfiguration,
) {
    private val logger = KotlinLogging.logger {}

    val maximumRequestSize = httpServerConfiguration.maxRequestSize
    val maximumMultipartFileSize = httpServerConfiguration.multipart.maxFileSize
    val multipartEnabled =
        httpServerConfiguration.multipart.isEnabled // CAVEAT: might not be the true behavior https://github.com/micronaut-projects/micronaut-core/issues/4773

    /**
     * The effective maximum upload size is probably the smaller value of
     * "micronaut.server.max-request-size" and "micronaut.server.multipart.max-file-size"
     * (at least as long as the upload is done via Multipart)
     */
    val maximumMultipartUploadSize = minOf(maximumMultipartFileSize, maximumRequestSize)
}
