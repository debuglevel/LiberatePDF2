package de.debuglevel.liberatepdf2.restservice.transformation

import java.nio.file.Path
import java.util.*

data class Transformation(
    val id: UUID,
    val originalFilename: String,
    val password: String?,
    var finished: Boolean,
    var failed: Boolean? = null,
    var errorMessage: String? = null,
    val restrictedPath: Path,
    var unrestrictedPath: Path? = null,
)
