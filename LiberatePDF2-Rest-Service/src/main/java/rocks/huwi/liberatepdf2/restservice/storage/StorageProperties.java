package rocks.huwi.liberatepdf2.restservice.storage;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("storage")
public class StorageProperties {

	/**
	 * Whether the storage directory should be cleared during initialization
	 */
	private boolean clearOnInitialization = false;

	/**
	 * Folder location for storing files
	 */
	private String location = "storage-directory";

	private Path locationPath;

	/**
	 * Gets the storage location
	 */
	public String getLocation() {
		return this.location;
	}

	/**
	 * Gets the storage location as Path
	 */
	public Path getLocationPath() {
		return this.locationPath;
	}

	public boolean isClearOnInitialization() {
		return this.clearOnInitialization;
	}

	public void setClearOnInitialization(final boolean clearOnInitialization) {
		this.clearOnInitialization = clearOnInitialization;
	}

	public void setLocation(final String location) {
		this.location = location;
		this.locationPath = Paths.get(location);
	}
}