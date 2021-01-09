package de.debuglevel.liberatepdf2.restservice.restrictionsremover

import de.debuglevel.liberatepdf2.restservice.transformation.Transformation

/**
 * Removes restrictions from files.
 */
interface RestrictionsRemoverService {
    /**
     * Count of unsuccessfully processed items.
     */
    val failedItemsCount: Long

    /**
     * Count of successfully processed items.
     */
    val successfulItemsCount: Long

    /**
     * Count of all items.
     */
    val itemsCount: Long
        get() = failedItemsCount + successfulItemsCount

    /**
     * Remove restrictions from file.
     */
    fun removeRestrictions(transformation: Transformation)
}