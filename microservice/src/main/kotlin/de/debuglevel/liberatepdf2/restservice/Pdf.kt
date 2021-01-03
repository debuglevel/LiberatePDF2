package de.debuglevel.liberatepdf2.restservice

import java.nio.file.Path
import java.util.*

/**
 * A PDF file
 */
class Pdf(
    var id: UUID,
    /**
     * original filename of the PDF
     */
    var originalFilename: String
) {
    var isDone = false
    var failed: Boolean? = null

    var password: String? = null
    var restrictedPath: Path? = null
    var unrestrictedPath: Path? = null

}