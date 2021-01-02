package rocks.huwi.liberatepdf2.restservice.storage

import io.micronaut.context.annotation.ConfigurationProperties
import java.nio.file.Paths

@ConfigurationProperties("app.liberatepdf2.storage")
class StorageProperties {
    /**
     * Whether the storage directory should be cleared during initialization
     */
    var isClearOnInitialization = false

    /**
     * Folder location for storing files
     */
    var location = "storage-directory"

    /**
     * Gets the storage location as Path
     */
    var locationPath = Paths.get(location)
}