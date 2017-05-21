package rocks.huwi.liberatepdf2.javafx;

import java.nio.file.Path;

public class TransferFile {
	private Path path;
	private String id;
	private String status;
	private boolean done;
	
	
	
	public TransferFile(Path path, String status) {
		super();
		this.path = path;
		this.done = false;
		this.status = status;
	}

	@Override
	public String toString() {
		return String.format("(%s) [%s] %s", id, status, path.getFileName());
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}
}
