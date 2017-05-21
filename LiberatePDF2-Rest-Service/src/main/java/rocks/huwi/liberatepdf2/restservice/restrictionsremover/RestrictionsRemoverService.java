package rocks.huwi.liberatepdf2.restservice.restrictionsremover;

import org.springframework.scheduling.annotation.Async;

import rocks.huwi.liberatepdf2.restservice.Pdf;

/**
 * Removes restrictions from files.
 */
public interface RestrictionsRemoverService {
	/**
	 * Remove restrictions from file.
	 *
	 * @param original
	 *            file to remove restrictions from
	 * @return
	 */
	public void removeRestrictions(Pdf pdf);

	/**
	 * Enqueue a task to remove removes restrictions
	 *
	 * @param original
	 *            file to remove restrictions from
	 * @return
	 */
	@Async
	public void removeRestrictionsAsync(Pdf pdf);
	
	/**
	 * Gets the count of processed items.
	 * @return
	 */
	public Long getItemsCount();
	
	/**
	 * Gets the count of unsuccessfully processed items.
	 * @return
	 */
	public Long getFailedItemsCount();
}
