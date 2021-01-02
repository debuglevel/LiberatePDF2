package rocks.huwi.liberatepdf2.restservice.storage;

public class StorageFileNotFoundException extends StorageException {
	private static final long serialVersionUID = 8134337840028689194L;

	public StorageFileNotFoundException(final String message) {
		super(message);
	}

	public StorageFileNotFoundException(final String message, final Throwable cause) {
		super(message, cause);
	}
}