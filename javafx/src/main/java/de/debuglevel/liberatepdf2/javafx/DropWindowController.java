package de.debuglevel.liberatepdf2.javafx;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.util.Duration;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.fluent.Request;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class DropWindowController {
	private static final Logger logger = LoggerFactory.getLogger(DropWindowController.class);

	private static final String HOST = "http://localhost:8080";

	private final ExecutorService checkTaskExecutor;

	@FXML
	private ListView<TransferFile> filesListView;

	private Long maximumUploadSize;

	@FXML
	private TextField passwordTextField;
	private final ObservableList<TransferFile> transferFiles;

	private final ExecutorService uploadTaskExecutor;

	public DropWindowController() {
		this.transferFiles = FXCollections.observableList(new ArrayList<TransferFile>());
		this.uploadTaskExecutor = Executors.newSingleThreadExecutor();
		this.checkTaskExecutor = Executors.newSingleThreadExecutor();
	}

	private void checkFilesStatus() {
		logger.info("Checking status of files...");

		this.transferFiles.stream().filter(tf -> (!tf.isDone()) && (tf.getId() != null)).forEach(tf -> {
			this.checkTaskExecutor.submit(new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					// check again if pre-conditions for checking status are
					// still met. (although conditions were filtered, the queue
					// MIGHT contain duplicates if checking the whole queue
					// lasts longer than the checking interval?)
					if ((!tf.isDone()) && (tf.getId() != null)) {
						DropWindowController.this.checkFileStatus(tf);
					}

					return null;
				}
			});
		});
	}

	private void checkFileStatus(final TransferFile transferFile) {
		try {
			final String url = HOST + "/api/v1/documents/" + transferFile.getId();

			logger.info("Checking status of file " + transferFile + " via " + url);

			final HttpResponse response = Request.Get(url).execute().returnResponse();
			final int statusCode = response.getStatusLine().getStatusCode();

			logger.info("Got status code " + statusCode + " for " + transferFile);
			if (statusCode == 200) {
				// file was successfully processed
				transferFile.setStatus("done");
				transferFile.setDone(true);

				this.saveFile(transferFile, response.getEntity().getContent());
			} else if (statusCode == 260) {
				// file is still in progress
				transferFile.setStatus("in progress");
			} else if (statusCode == 560) {
				// processing failed
				transferFile.setStatus("processing failed");
				transferFile.setDone(true);
			} else {
				// unknown
				transferFile.setStatus("unknown status code");
			}

			this.filesListView.refresh(); // should be better be done via an
			// property. but works good enough.
		} catch (final HttpHostConnectException e) {
			logger.error("Connection to host failed: " + e.getMessage());
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void fetchMaximumUploadSize() {
		try {
			logger.info("Querying service for maximum upload size...");

			final String maximumUploadSizeString = Request.Get(HOST + "/api/v1/status/maximum-upload-size").execute()
					.returnContent().asString();
			logger.info("Maximum upload size is '" + maximumUploadSizeString + "'");

			this.maximumUploadSize = Long.valueOf(maximumUploadSizeString);
		} catch (final HttpHostConnectException e) {
			logger.error("Connection to host failed: " + e.getMessage());
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@FXML
	public void initialize() {
		this.filesListView.setItems(this.transferFiles);

		final Timeline fiveSecondsWonder = new Timeline(
				new KeyFrame(Duration.seconds(5), new EventHandler<ActionEvent>() {
					@Override
					public void handle(final ActionEvent event) {
						DropWindowController.this.checkFilesStatus();
					}
				}));
		fiveSecondsWonder.setCycleCount(Animation.INDEFINITE);
		fiveSecondsWonder.play();

		this.fetchMaximumUploadSize();
	}

	/**
	 * Checks if the size of the given file meets the restrictions of the server
	 *
	 * @return true if file size is okay (or unknown), false if file is too big
	 */
	private boolean isFileSizeAccepted(final Path path) {
		if (this.maximumUploadSize == null) {
			return true;
		} else {
			try {
				if (Files.size(path) < this.maximumUploadSize) {
					return true;
				}
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return false;
	}

	public void onDragDropped(final DragEvent event) {
		final Dragboard db = event.getDragboard();
		boolean success = false;
		if (db.hasFiles()) {
			final List<File> files = db.getFiles();

			final Task<Void> processFilesTask = new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					DropWindowController.this
							.processFiles(files.stream().map(File::toPath).collect(Collectors.toList()));
					return null;
				}
			};

			// processFilesTask.setOnFailed(e -> {
			// processFilesTask.exceptionProperty().get().printStackTrace();
			// });

			final Thread thread = new Thread(processFilesTask);
			thread.setDaemon(true);
			thread.start();

			success = true;
		}

		event.setDropCompleted(success);

		event.consume();
	}

	public void onDragOver(final DragEvent event) {
		if ((event.getGestureSource() != this.filesListView) && event.getDragboard().hasFiles()) {
			event.acceptTransferModes(TransferMode.ANY);
		}

		event.consume();
	}

	private void processFile(final Path path) {
		logger.info("Processing file " + path);

		final TransferFile transferFile = new TransferFile(path, "uploading");
		this.transferFiles.add(transferFile);

		final String password = this.passwordTextField.getText();

		if (this.isFileSizeAccepted(path) == false) {
			transferFile.setStatus("file too big");
		} else {
			final Task<String> uploadTask = new Task<String>() {
				@Override
				protected String call() throws Exception {
					logger.info("Building POST request for " + path + "...");
					final HttpEntity entity = MultipartEntityBuilder.create()
							.setMode(HttpMultipartMode.BROWSER_COMPATIBLE).setCharset(Charset.defaultCharset())
							.addBinaryBody("file", path.toFile()).addTextBody("password", password).build();

					logger.info("Sending POST request for " + path + "...");
					return Request.Post(HOST + "/api/v1/documents/").useExpectContinue().version(HttpVersion.HTTP_1_1)
							.body(entity).execute().returnContent().asString();
				}
			};

			uploadTask.setOnSucceeded(e -> {
				try {
					logger.info("Upload Task for " + path + " succeeded; got ID=" + uploadTask.get());
					transferFile.setId(uploadTask.get());
				} catch (InterruptedException | ExecutionException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				transferFile.setStatus("transferred");
				this.filesListView.refresh(); // should be better be done via an
				// property. but works good
				// enough.
			});

			uploadTask.setOnFailed(e -> {
				logger.info("Upload Task for " + path + " failed");
				transferFile.setStatus("upload failed");

				if (uploadTask.exceptionProperty().get() instanceof SocketException) {
					logger.info("Exception is SocketException; the file MIGHT be too big.");
					transferFile.setStatus("upload failed (too big?)");
				}

				this.filesListView.refresh(); // should be better be done via an
				// property. but works good
				// enough.
				uploadTask.exceptionProperty().get().printStackTrace();
			});

			logger.info("Queueing upload task for " + path + "...");
			this.uploadTaskExecutor.submit(uploadTask);
		}
	}

	private void processFiles(final List<Path> paths) {
		logger.info("Processing " + paths.size() + " files...");

		for (final Path path : paths) {
			if (Files.isDirectory(path)) {
				logger.info(path + " is a directory");

				// String[] extensions = null;
				final String[] extensions = new String[]{"pdf", "PDF"};
				final Collection<File> files = FileUtils.listFiles(path.toFile(), extensions, true);
				logger.info("Found " + files.size() + " files in directory " + path);

				files.stream().forEach(f -> this.processFile(f.toPath()));
			} else if (Files.isRegularFile(path) && Files.isReadable(path)) {
				logger.info(path + " is a regular and readable file");
				this.processFile(path);
			} else {
				// do nothing because strange
			}
		}
	}

	private void saveFile(final TransferFile transferFile, final InputStream inputStream) {
		try {
			final Path originalPath = transferFile.getPath();
			final File destinationFile = originalPath.resolveSibling(originalPath.getFileName() + " (unrestricted).pdf")
					.toFile();

			logger.info("Copying " + transferFile + " to " + destinationFile + "...");

			FileUtils.copyInputStreamToFile(inputStream, destinationFile);
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
