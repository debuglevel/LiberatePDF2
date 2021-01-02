package rocks.huwi.liberatepdf2.restservice.storage

import org.springframework.boot.context.properties.ConfigurationProperties
import java.nio.file.Path
import java.nio.file.Paths

@ConfigurationProperties("storage")
class StorageProperties {
    /**
     * Whether the storage directory should be cleared during initialization
     */
    var isClearOnInitialization = false
    /**
     * Gets the storage location
     */
    /**
     * Folder location for storing files
     */
    var location = "storage-directory"
        set(location) {
            field = location
            locationPath = Paths.get(location)
        }

    /**
     * Gets the storage location as Path
     */
    var locationPath: Path? = null
        private set
}