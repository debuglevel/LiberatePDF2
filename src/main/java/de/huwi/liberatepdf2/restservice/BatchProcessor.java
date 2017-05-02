package de.huwi.liberatepdf2.restservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchProcessor {

    private final Logger Logger = LoggerFactory.getLogger(BatchProcessor.class);

	private Path createZip(Path[] paths) throws IOException {
            Logger.info("Creating ZIP file");
		// String zipPath = "PDFs.zip";
                Path temporaryPath = Files.createTempDirectory("LiberatePDF_zip");
		File zipFile = new File(temporaryPath+"/PDFs.zip");

		// out put file
		ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream(zipFile));

		for (Path path : paths) {
                    Logger.info("Adding file \"{}\" to ZIP", path.toFile().getName());
                    
			// input file
			FileInputStream fileStream = new FileInputStream(path.toFile());

			// name the file inside the zip file
			String fileName = path.toFile().getName();
			zipStream.putNextEntry(new ZipEntry(fileName));

			// buffer size
			byte[] b = new byte[1024];
			int count;

			while ((count = fileStream.read(b)) > 0) {
//				System.out.println();
				zipStream.write(b, 0, count);
			}

			fileStream.close();
		}

                Logger.info("Closing ZIP file");
		zipStream.close();

		return zipFile.toPath();
	}

	public Path RemoveRestrictions(Iterable<Path> filesOriginal, String password) {
		LegacyPdftkRestrictionsRemover restrictionsRemover = new LegacyPdftkRestrictionsRemover();

		Path[] pathsNew = restrictionsRemover.RemoveRestrictions(filesOriginal, password);

                if (pathsNew.length == 0)
                {
                    Logger.error("RestrictionsRemover returned zero files.");
                }
                else if (pathsNew.length == 1)
                {
                    return pathsNew[0];
                }
                else if (pathsNew.length > 1)
                {
                	Path zipPath = null;
                    try {
                            zipPath = this.createZip(pathsNew);
                    } catch (IOException e) {
                        Logger.error("Exception occured during removing restrictions:", e);
                    }
                    
                    return zipPath;
                }
                
                Logger.error("This cannot happen.");
                return null;
	}

}
