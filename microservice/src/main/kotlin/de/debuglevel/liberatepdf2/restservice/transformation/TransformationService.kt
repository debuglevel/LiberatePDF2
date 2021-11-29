package de.debuglevel.liberatepdf2.restservice.transformation

import de.debuglevel.liberatepdf2.restservice.restrictionsremover.RestrictionsRemoverService
import io.micronaut.context.annotation.Property
import mu.KotlinLogging
import java.io.InputStream
import java.util.*
import java.util.concurrent.Executors
import javax.inject.Singleton

@Singleton
class TransformationService(
    private val restrictionsRemoverService: RestrictionsRemoverService,
    private val transformationRepository: TransformationRepository,
    @Property(name = "app.liberatepdf2.transformation.worker-threads") workerThreadsCount: Int,
) {
    private val logger = KotlinLogging.logger {}

    private val executor = Executors.newFixedThreadPool(workerThreadsCount)

    val count: Long
        get() {
            logger.debug { "Getting transformations count..." }

            val count = transformationRepository.count()

            logger.debug { "Got transformations count: $count" }
            return count
        }

    fun exists(id: UUID): Boolean {
        logger.debug { "Checking if transformation $id exists..." }

        val isExisting = transformationRepository.existsById(id)

        logger.debug { "Checked if transformation $id exists: $isExisting" }
        return isExisting
    }

    fun get(id: UUID): Transformation {
        logger.debug { "Getting transformation with ID '$id'..." }

        val transformation: Transformation = transformationRepository.findById(id).orElseThrow {
            logger.debug { "Getting transformation with ID '$id' failed" }
            ItemNotFoundException(id)
        }

        logger.debug { "Got transformation with ID '$id': $transformation" }
        return transformation
    }

    fun getAll(): Set<Transformation> {
        logger.debug { "Getting all transformations..." }

        val transformations = transformationRepository.findAll().toSet()

        logger.debug { "Got ${transformations.size} transformations" }
        return transformations
    }

    fun add(filename: String, inputStream: InputStream, password: String?): Transformation {
        val transformation = Transformation(
            id = null,
            originalFilename = filename,
            password = password,
            finished = false,
            restrictedFile = inputStream.readBytes(),
        )

        return add(transformation)
    }

    fun add(transformation: Transformation): Transformation {
        logger.debug { "Adding transformation '$transformation'..." }

        val savedTransformation = transformationRepository.save(transformation)

        logger.debug { "Submitting restriction removing task to executor..." }
        executor.submit {
            restrictionsRemoverService.removeRestrictions(savedTransformation)
            update(savedTransformation.id!!, savedTransformation)
        }

        logger.debug { "Added transformation: $savedTransformation" }
        return savedTransformation
    }

    fun update(id: UUID, transformation: Transformation): Transformation {
        logger.debug { "Updating transformation '$transformation' with ID '$id'..." }

        // an object must be known to Hibernate (i.e. retrieved first) to get updated;
        // it would be a "detached entity" otherwise.
        val updateTransformation = this.get(id).apply {
            finished = transformation.finished
            failed = transformation.failed
            errorMessage = transformation.errorMessage
            unrestrictedFile = transformation.unrestrictedFile
        }

        val updatedTransformation = transformationRepository.update(updateTransformation)

        logger.debug { "Updated transformation: $updatedTransformation with ID '$id'" }
        return updatedTransformation
    }

    fun delete(id: UUID) {
        logger.debug { "Deleting transformation with ID '$id'..." }

        if (transformationRepository.existsById(id)) {
            transformationRepository.deleteById(id)
        } else {
            throw ItemNotFoundException(id)
        }

        logger.debug { "Deleted transformation with ID '$id'" }
    }

    fun deleteAll() {
        logger.debug { "Deleting all transformations..." }

        val countBefore = transformationRepository.count()
        transformationRepository.deleteAll() // CAVEAT: does not delete dependent entities; use this instead: transformationRepository.findAll().forEach { transformationRepository.delete(it) }
        val countAfter = transformationRepository.count()
        val countDeleted = countBefore - countAfter

        logger.debug { "Deleted $countDeleted of $countBefore transformations, $countAfter remaining" }
    }

    class ItemNotFoundException(criteria: Any) : Exception("Item '$criteria' does not exist.")
}