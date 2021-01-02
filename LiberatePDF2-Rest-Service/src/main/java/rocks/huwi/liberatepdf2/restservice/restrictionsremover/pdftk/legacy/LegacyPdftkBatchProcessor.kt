package rocks.huwi.liberatepdf2.restservice.restrictionsremover.pdftk.legacy

// package de.huwi.liberatepdf2.restservice.restrictionsremover.pdftk.legacy;
//
// import java.io.File;
// import java.io.FileInputStream;
// import java.io.FileOutputStream;
// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.util.zip.ZipEntry;
// import java.util.zip.ZipOutputStream;
//
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
//
// public class LegacyPdftkBatchProcessor {
//
// private final Logger Logger =
// LoggerFactory.getLogger(LegacyPdftkBatchProcessor.class);
//
// private Path createZip(final Path[] paths) throws IOException {
// this.Logger.info("Creating ZIP file");
// // String zipPath = "PDFs.zip";
// final Path temporaryPath = Files.createTempDirectory("LiberatePDF_zip");
// final File zipFile = new File(temporaryPath + "/PDFs.zip");
//
// // out put file
// final ZipOutputStream zipStream = new ZipOutputStream(new
// FileOutputStream(zipFile));
//
// for (final Path path : paths) {
// this.Logger.info("Adding file \"{}\" to ZIP", path.toFile().getName());
//
// // input file
// final FileInputStream fileStream = new FileInputStream(path.toFile());
//
// // name the file inside the zip file
// final String fileName = path.toFile().getName();
// zipStream.putNextEntry(new ZipEntry(fileName));
//
// // buffer size
// final byte[] b = new byte[1024];
// int count;
//
// while ((count = fileStream.read(b)) > 0) {
// // System.out.println();
// zipStream.write(b, 0, count);
// }
//
// fileStream.close();
// }
//
// this.Logger.info("Closing ZIP file");
// zipStream.close();
//
// return zipFile.toPath();
// }
//
//// public Path RemoveRestrictions(final Iterable<Path> filesOriginal, final
// String password) {
//// final LegacyPdftkRestrictionsRemover restrictionsRemover = new
// LegacyPdftkRestrictionsRemover();
////
//// final Path[] pathsNew =
// restrictionsRemover.RemoveRestrictions(filesOriginal, password);
////
//// if (pathsNew.length == 0) {
//// this.Logger.error("RestrictionsRemover returned zero files.");
//// } else if (pathsNew.length == 1) {
//// return pathsNew[0];
//// } else if (pathsNew.length > 1) {
//// Path zipPath = null;
//// try {
//// zipPath = this.createZip(pathsNew);
//// } catch (final IOException e) {
//// this.Logger.error("Exception occured during removing restrictions:", e);
//// }
////
//// return zipPath;
//// }
////
//// this.Logger.error("This cannot happen.");
//// return null;
//// }
//
// }
