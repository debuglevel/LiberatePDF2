package de.debuglevel.liberatepdf2.restservice.configuration

class GetConfigurationResponse(
    val maximumMultipartUploadSize: Long,
    val maximumRequestSize: Long,
    val maximumMultipartFileSize: Long,
    val multipartEnabled: Boolean
)
