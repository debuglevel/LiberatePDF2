package rocks.huwi.liberatepdf2.javafx;

import java.nio.file.Path;

public class TransferFile {
	private boolean done;
	private String id;
	private Path path;
	private String status;

	public TransferFile(final Path path, final String status) {
		super();
		this.path = path;
		this.done = false;
		this.status = status;
	}

	public String getId() {
		return this.id;
	}

	public Path getPath() {
		return this.path;
	}

	public String getStatus() {
		return this.status;
	}

	public boolean isDone() {
		return this.done;
	}

	public void setDone(final boolean done) {
		this.done = done;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public void setPath(final Path path) {
		this.path = path;
	}

	public void setStatus(final String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return String.format("[%s] %s", this.status, this.path.getFileName());
	}
}
