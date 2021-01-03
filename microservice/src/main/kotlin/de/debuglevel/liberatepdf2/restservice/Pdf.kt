package de.debuglevel.liberatepdf2.restservice

import java.nio.file.Path
import java.util.*

/**
 * A PDF file
 */
class Pdf(
    val id: UUID,
    /**
     * original filename of the PDF
     */
    val originalFilename: String
) {
    var isDone = false
    var failed: Boolean? = null

    var password: String? = null
    var restrictedPath: Path? = null
    var unrestrictedPath: Path? = null
}