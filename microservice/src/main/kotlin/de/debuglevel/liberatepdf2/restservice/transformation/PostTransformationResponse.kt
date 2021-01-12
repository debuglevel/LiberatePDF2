package de.debuglevel.liberatepdf2.restservice.transformation

import java.util.*

data class PostTransformationResponse(
    val id: UUID,
    val originalFilename: String,
    val finished: Boolean,
    val failed: Boolean? = null,
    val errorMessage: String? = null,
) {
    constructor(transformation: Transformation) : this(
        id = transformation.id!!,
        originalFilename = transformation.originalFilename,
        finished = transformation.finished,
        failed = transformation.failed,
        errorMessage = transformation.errorMessage
    )
}