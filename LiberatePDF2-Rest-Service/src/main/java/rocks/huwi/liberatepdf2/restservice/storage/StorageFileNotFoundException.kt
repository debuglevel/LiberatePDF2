package rocks.huwi.liberatepdf2.restservice.storage

class StorageFileNotFoundException : StorageException {
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

    companion object {
        private const val serialVersionUID = 8134337840028689194L
    }
}