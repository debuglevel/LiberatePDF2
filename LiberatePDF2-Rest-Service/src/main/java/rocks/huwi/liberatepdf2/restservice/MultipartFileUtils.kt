package rocks.huwi.liberatepdf2.restservice

import mu.KotlinLogging

object MultipartFileUtils {
    private val logger = KotlinLogging.logger {}

//    /**
//     * Returns a Path for a MultipartFile by copying/moving its content.
//     *
//     * @param multipartFile
//     * @return
//     * @throws IOException
//     */
//    @Throws(IOException::class)
//    fun getPath(multipartFile: MultipartFile): Path {
//        val tempDirectory = Files.createTempDirectory("LiberatePDF2")
//        val tempPath = tempDirectory.resolve(multipartFile.originalFilename)
//        logger.debug { ("Moving MultipartFile {} to {}" + multipartFile.name, tempPath) }
//        multipartFile.transferTo(tempPath.toFile())
//        return tempPath
//    }
}