package rocks.huwi.liberatepdf2.restservice.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("storage")
public class StorageProperties {

	/**
	 * Folder location for storing files
	 */
	private String location = "temporary-storage-directory";

	public String getLocation() {
		return this.location;
	}

	public void setLocation(final String location) {
		this.location = location;
	}
}