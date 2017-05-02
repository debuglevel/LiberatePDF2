package de.huwi.liberatepdf2.restservice.storage;

public class StorageException extends RuntimeException {

	public StorageException(final String message) {
		super(message);
	}

	public StorageException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
