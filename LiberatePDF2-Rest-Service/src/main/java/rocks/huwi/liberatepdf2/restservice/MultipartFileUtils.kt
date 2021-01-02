package rocks.huwi.liberatepdf2.restservice

import org.slf4j.LoggerFactory
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

object MultipartFileUtils {
    private val log = LoggerFactory.getLogger(MultipartFileUtils::class.java)

    /**
     * Returns a Path for a MultipartFile by copying/moving its content.
     *
     * @param multipartFile
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun getPath(multipartFile: MultipartFile): Path {
        val tempDirectory = Files.createTempDirectory("LiberatePDF2")
        val tempPath = tempDirectory.resolve(multipartFile.originalFilename)
        log.debug("Moving MultipartFile {} to {}", multipartFile.name, tempPath)
        multipartFile.transferTo(tempPath.toFile())
        return tempPath
    } // /**
    // * Returns Paths for MultipartFiles by copying/moving their content.
    // *
    // * @param restrictedPdf
    // * @return
    // * @throws IOException
    // */
    // public static ArrayList<Path> getPaths(final MultipartFile[]
    // multipartFiles) throws IOException {
    // final ArrayList<Path> restrictedPdfsTempPaths = new ArrayList<>();
    //
    // for (final MultipartFile multipartFile : multipartFiles) {
    // final Path restrictedPdfTempPath =
    // MultipartFileUtils.getPath(multipartFile);
    // restrictedPdfsTempPaths.add(restrictedPdfTempPath);
    // }
    // return restrictedPdfsTempPaths;
    // }
}