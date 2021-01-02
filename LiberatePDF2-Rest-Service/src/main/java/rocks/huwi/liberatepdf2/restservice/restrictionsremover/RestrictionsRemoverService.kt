package rocks.huwi.liberatepdf2.restservice.restrictionsremover

import org.springframework.scheduling.annotation.Async
import rocks.huwi.liberatepdf2.restservice.Pdf

/**
 * Removes restrictions from files.
 */
interface RestrictionsRemoverService {
    /**
     * Gets the count of unsuccessfully processed items.
     *
     * @return
     */
    val failedItemsCount: Long

    /**
     * Gets the count of processed items.
     *
     * @return
     */
    val itemsCount: Long

    /**
     * Remove restrictions from file.
     *
     * @param original
     * file to remove restrictions from
     * @return
     */
    fun removeRestrictions(pdf: Pdf)

    /**
     * Enqueue a task to remove restrictions
     *
     * @param original
     * file to remove restrictions from
     * @return
     */
    @Async
    fun removeRestrictionsAsync(pdf: Pdf)
}