package de.debuglevel.liberatepdf2.restservice.transformation

import java.util.*

data class Transformation(
    val id: UUID,
    val originalFilename: String,
    val password: String?,
    val finished: Boolean,
    val failed: Boolean? = null,
    val errorMessage: String? = null,
)
