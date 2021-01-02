package rocks.huwi.liberatepdf2.restservice.storage;

public class StorageException extends RuntimeException {

	private static final long serialVersionUID = -4413651601072818152L;

	public StorageException(final String message) {
		super(message);
	}

	public StorageException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
