package de.huwi.liberatepdf2.restservice;

import java.nio.file.Path;

import lombok.ToString;

/**
 * A PDF file
 */
@ToString
public class Pdf {
	/**
	 * original filename of the PDF
	 */
	private String originalFilename;

	private Long id;
	
	private boolean done;
	
	private Boolean failed;
	
	private Path restrictedPath;
	
	private Path unrectrictedPath;
	
	public Pdf(Long id, String originalFilename)
	{
		this.id = id;
		this.originalFilename = originalFilename;
		this.done = false;
		this.failed = null;
	}
	
	public String getOriginalFilename() {
		return this.originalFilename;
	}

	public void setOriginalFilename(final String originalFilename) {
		this.originalFilename = originalFilename;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public Boolean getFailed() {
		return failed;
	}

	public void setFailed(Boolean failed) {
		this.failed = failed;
	}

	public Path getRestrictedPath() {
		return restrictedPath;
	}

	public void setRestrictedPath(Path restrictedPath) {
		this.restrictedPath = restrictedPath;
	}

	public Path getUnrectrictedPath() {
		return unrectrictedPath;
	}

	public void setUnrectrictedPath(Path unrectrictedPath) {
		this.unrectrictedPath = unrectrictedPath;
	}
}
