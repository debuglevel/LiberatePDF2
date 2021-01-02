package rocks.huwi.liberatepdf2.restservice

import lombok.ToString
import org.springframework.web.multipart.MultipartFile

/**
 * A PDF file
 */
@ToString
class PdfDTO {
    /**
     * Byte content of the PDF
     */
    var file: MultipartFile? = null

    /**
     * File name of the PDF
     */
    var password: String? = null
}