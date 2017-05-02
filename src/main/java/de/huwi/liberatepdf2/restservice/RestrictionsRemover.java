package de.huwi.liberatepdf2.restservice;

import java.nio.file.Path;

/**
 * Removes restrictions from files.
 */
public interface RestrictionsRemover {
	/**
	 * Remove restrictions from files.
	 * @param original file to remove restrictions from
	 * @return PDF if single file given, ZIP if multiple files
	 */
	public Path removeRestrictions(Iterable<Path> originals);
	
	/**
	 * Remove restrictions from encrypted files.
	 * @param original files to remove restrictions from
	 * @param password read/user password to decrypt the files (null if no password is needed)
	 * @return PDF if single file given, ZIP if multiple files
	 */
	public Path removeRestrictions(Iterable<Path> originals, String password);
}
