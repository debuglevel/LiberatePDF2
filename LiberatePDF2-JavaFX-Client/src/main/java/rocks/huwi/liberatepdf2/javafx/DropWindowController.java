package rocks.huwi.liberatepdf2.javafx;

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

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class DropWindowController {
	private static final String HOST = "http://localhost:8080";

	private static final Logger log = LoggerFactory.getLogger(DropWindowController.class);

	@FXML
	private ListView<TransferFile> filesListView;
	
	@FXML
	private TextField passwordTextField;

	private ObservableList<TransferFile> transferFiles;

	private ExecutorService uploadTaskExecutor;
	private ExecutorService checkTaskExecutor;

	public DropWindowController() {
		this.transferFiles = FXCollections.observableList(new ArrayList<TransferFile>());
		this.uploadTaskExecutor = Executors.newSingleThreadExecutor();
		this.checkTaskExecutor = Executors.newSingleThreadExecutor();
	}

	@FXML
	public void initialize() {
		this.filesListView.setItems(transferFiles);

		Timeline fiveSecondsWonder = new Timeline(new KeyFrame(Duration.seconds(5), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				checkFilesStatus();
			}
		}));
		fiveSecondsWonder.setCycleCount(Timeline.INDEFINITE);
		fiveSecondsWonder.play();
	}

	private void checkFilesStatus() {
		log.info("Checking status of files");

		transferFiles.stream().filter(tf -> tf.isDone() == false && tf.getId() != null).forEach(tf -> {
			checkTaskExecutor.submit(new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					// check again if pre-conditions for checking status are
					// still met. (although conditions were filtered, the queue
					// MIGHT contain duplicates if checking the whole queue
					// lasts longer than the checking interval?)
					if (tf.isDone() == false && tf.getId() != null) {
						checkFileStatus(tf);
					}
					
					return null;
				}
			});
		});
	}

	private void checkFileStatus(TransferFile transferFile) {
		try {
			String url = HOST + "/api/v1/documents/" + transferFile.getId();

			log.info("Checking status of file " + transferFile + " via " + url);

			HttpResponse response = Request.Get(url).execute().returnResponse();
			int statuscode = response.getStatusLine().getStatusCode();

			log.info("Got status code " + statuscode + " for " + transferFile);
			if (statuscode == 200) {
				// file was successfully processed
				transferFile.setStatus("done");
				transferFile.setDone(true);

				saveFile(transferFile, response.getEntity().getContent());
			} else if (statuscode == 260) {
				// file is still in progress
				transferFile.setStatus("in progress");
			} else if (statuscode == 560) {
				// processing failed
				transferFile.setStatus("processing failed");
				transferFile.setDone(true);
			} else {
				// unknown
				transferFile.setStatus("unknown statuscode");
			}

			this.filesListView.refresh(); // should be better be done via an
											// property. but works good enough.
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void saveFile(TransferFile transferFile, InputStream inputStream) {
		try {
			Path originalPath = transferFile.getPath();
			File destinationFile = originalPath.resolveSibling(originalPath.getFileName() + " (unrestricted).pdf")
					.toFile();

			log.info("Copying " + transferFile + " to " + destinationFile);

			FileUtils.copyInputStreamToFile(inputStream, destinationFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void onDragOver(DragEvent event) {
		if (event.getGestureSource() != filesListView && event.getDragboard().hasFiles()) {
			event.acceptTransferModes(TransferMode.ANY);
		}

		event.consume();
	}

	public void onDragDropped(DragEvent event) {
		Dragboard db = event.getDragboard();
		boolean success = false;
		if (db.hasFiles()) {
			List<File> files = db.getFiles();

			final Task<Void> processFilesTask = new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					processFiles(files.stream().map(f -> f.toPath()).collect(Collectors.toList()));
					return null;
				}
			};

			// processFilesTask.setOnFailed(e -> {
			// processFilesTask.exceptionProperty().get().printStackTrace();
			// });

			Thread thread = new Thread(processFilesTask);
			thread.setDaemon(true);
			thread.start();

			success = true;
		}

		event.setDropCompleted(success);

		event.consume();
	}

	private void processFiles(List<Path> paths) {
		log.info("Processing " + paths.size() + " dropped files");

		for (Path path : paths) {
			if (Files.isDirectory(path)) {
				log.info(path + " is a directory");

				// String[] extensions = null;
				String[] extensions = new String[] { "pdf", "PDF" };
				Collection<File> files = FileUtils.listFiles(path.toFile(), extensions, true);
				log.info("Found " + files.size() + " files in directory " + path);

				files.stream().forEach(f -> this.processFile(f.toPath()));
			} else if (Files.isRegularFile(path) && Files.isReadable(path)) {
				log.info(path + " is a regular and readable file");
				processFile(path);
			} else {
				// do nothing because strange
			}
		}
	}

	private void processFile(Path path) {
		log.info("Processing file " + path);

		TransferFile transferFile = new TransferFile(path, "uploading");
		this.transferFiles.add(transferFile);
		
		String password = this.passwordTextField.getText();

		final Task<String> uploadTask = new Task<String>() {
			@Override
			protected String call() throws Exception {
				log.info("Building POST request for " + path);
				HttpEntity entity = MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
						.setCharset(Charset.defaultCharset()).addBinaryBody("file", path.toFile())
						.addTextBody("password", password).build();

				log.info("Sending POST request for " + path);
				return Request.Post(HOST + "/api/v1/documents/").useExpectContinue().version(HttpVersion.HTTP_1_1)
						.body(entity).execute().returnContent().asString();
			}
		};

		uploadTask.setOnSucceeded(e -> {
			try {
				log.info("Upload Task for " + path + " succeeded; got ID=" + uploadTask.get());
				transferFile.setId(uploadTask.get());
			} catch (InterruptedException | ExecutionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			transferFile.setStatus("transfered");
			this.filesListView.refresh(); // should be better be done via an
											// property. but works good enough.
		});

		uploadTask.setOnFailed(e -> {
			log.info("Upload Task for " + path + " failed");
			transferFile.setStatus("upload failed");
			
			if (uploadTask.exceptionProperty().get() instanceof SocketException)
			{
				log.info("Exception is SocketException; the file MIGHT be too big.");
				transferFile.setStatus("upload failed (too big?)");
			}
			
			this.filesListView.refresh(); // should be better be done via an
											// property. but works good enough.
			uploadTask.exceptionProperty().get().printStackTrace();
		});

		log.info("Queueing upload task for " + path);
		uploadTaskExecutor.submit(uploadTask);
	}
}
