package rocks.huwi.liberatepdf2.restservice.storage

open class StorageException : RuntimeException {
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

    companion object {
        private const val serialVersionUID = -4413651601072818152L
    }
}