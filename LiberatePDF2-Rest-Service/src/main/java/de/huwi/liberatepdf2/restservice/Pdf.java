package de.huwi.liberatepdf2.restservice;

import java.nio.file.Path;

import lombok.ToString;

/**
 * A PDF file
 */
@ToString
public class Pdf {
	private boolean done;

	private Boolean failed;

	private Long id;

	/**
	 * original filename of the PDF
	 */
	private String originalFilename;

	private Path restrictedPath;

	private Path unrectrictedPath;

	public Pdf(final Long id, final String originalFilename) {
		this.id = id;
		this.originalFilename = originalFilename;
		this.done = false;
		this.failed = null;
	}

	public Boolean getFailed() {
		return this.failed;
	}

	public Long getId() {
		return this.id;
	}

	public String getOriginalFilename() {
		return this.originalFilename;
	}

	public Path getRestrictedPath() {
		return this.restrictedPath;
	}

	public Path getUnrectrictedPath() {
		return this.unrectrictedPath;
	}

	public boolean isDone() {
		return this.done;
	}

	public void setDone(final boolean done) {
		this.done = done;
	}

	public void setFailed(final Boolean failed) {
		this.failed = failed;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public void setOriginalFilename(final String originalFilename) {
		this.originalFilename = originalFilename;
	}

	public void setRestrictedPath(final Path restrictedPath) {
		this.restrictedPath = restrictedPath;
	}

	public void setUnrectrictedPath(final Path unrectrictedPath) {
		this.unrectrictedPath = unrectrictedPath;
	}
}
