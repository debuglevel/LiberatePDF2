package de.debuglevel.liberatepdf2.restservice

import java.nio.file.Path
import java.util.*

/**
 * A PDF file
 */
data class Pdf(
    val id: UUID,
    /**
     * original filename of the PDF
     */
    val originalFilename: String
) {
    var done = false
    var failed: Boolean? = null
    var error: String? = null

    var password: String? = null
    var restrictedPath: Path? = null
    var unrestrictedPath: Path? = null
}