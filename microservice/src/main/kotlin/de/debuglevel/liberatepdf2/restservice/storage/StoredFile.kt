package de.debuglevel.liberatepdf2.restservice.storage

import java.io.InputStream
import java.util.*

data class StoredFile(
    val id: UUID,
    val filename: String,
    val size: Long,
    val inputStream: InputStream? = null,
)
