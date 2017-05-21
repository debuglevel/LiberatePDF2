package rocks.huwi.liberatepdf2.restservice;

import java.nio.file.Path;

import lombok.ToString;

/**
 * A PDF file
 */
@ToString
public class Pdf {
	private boolean done;

	private Boolean failed;

	private String id;

	/**
	 * original filename of the PDF
	 */
	private String originalFilename;

	private String password;

	private Path restrictedPath;

	private Path unrectrictedPath;

	public Pdf(final String id, final String originalFilename) {
		this.id = id;
		this.originalFilename = originalFilename;
		this.done = false;
		this.failed = null;
	}

	public Boolean getFailed() {
		return this.failed;
	}

	public String getId() {
		return this.id;
	}

	public String getOriginalFilename() {
		return this.originalFilename;
	}

	public String getPassword() {
		return this.password;
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

	public void setId(final String id) {
		this.id = id;
	}

	public void setOriginalFilename(final String originalFilename) {
		this.originalFilename = originalFilename;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public void setRestrictedPath(final Path restrictedPath) {
		this.restrictedPath = restrictedPath;
	}

	public void setUnrectrictedPath(final Path unrectrictedPath) {
		this.unrectrictedPath = unrectrictedPath;
	}
}
