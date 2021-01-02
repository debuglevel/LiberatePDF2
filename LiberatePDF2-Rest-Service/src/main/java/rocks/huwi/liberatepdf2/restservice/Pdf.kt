package rocks.huwi.liberatepdf2.restservice

import lombok.ToString
import java.nio.file.Path

/**
 * A PDF file
 */
@ToString
class Pdf(
    var id: String,
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