package de.debuglevel.liberatepdf2.restservice.transformation

import de.debuglevel.liberatepdf2.restservice.restrictionsremover.RestrictionsRemoverService
import de.debuglevel.liberatepdf2.restservice.storage.StorageService
import io.micronaut.context.annotation.Property
import mu.KotlinLogging
import java.io.InputStream
import java.util.*
import java.util.concurrent.Executors
import javax.inject.Singleton

@Singleton
class TransformationService(
    private val storageService: StorageService,
    private val restrictionsRemoverService: RestrictionsRemoverService,
    @Property(name = "app.liberatepdf2.transformation.worker-threads") workerThreadsCount: Int,
) {
    private val logger = KotlinLogging.logger {}

    private val executor = Executors.newFixedThreadPool(workerThreadsCount)

    private val transformations = HashMap<UUID, Transformation>()

    fun get(transformationId: UUID): Transformation {
        logger.debug { "Getting transformation with id=$transformationId..." }

        val transformation = transformations.getOrElse(transformationId) { throw NotFoundException(transformationId) }

        logger.debug { "Got transformation with id=$transformationId: $transformation" }
        return transformation
    }

    fun add(filename: String, inputStream: InputStream, password: String?): Transformation {
        logger.debug { "Adding transformation..." }

        val pdf = storageService.store(filename, inputStream, password ?: "")
        pdf.password = password
        val transformation = Transformation(
            id = pdf.id,
            originalFilename = filename,
            password = password,
            finished = false,
            restrictedPath = pdf.restrictedPath!!
        )

        transformations[transformation.id] = transformation

        logger.debug { "Submitting restriction removing task to executor..." }
        executor.submit { restrictionsRemoverService.removeRestrictions(transformation) }

        logger.debug { "Added transformation: $transformation" }
        return transformation
    }

    data class NotFoundException(val transformationId: UUID) :
        Exception("No transformation found with id=$transformationId")
}