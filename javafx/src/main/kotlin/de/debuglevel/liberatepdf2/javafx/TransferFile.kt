package de.debuglevel.liberatepdf2.javafx

import java.nio.file.Path

class TransferFile(
    var path: Path,
    var status: String,
    var password: String? = null,
    var done: Boolean = false,
    var id: String? = null,
) {
    override fun toString(): String {
        return "[$status] ${path.fileName}"
    }
}