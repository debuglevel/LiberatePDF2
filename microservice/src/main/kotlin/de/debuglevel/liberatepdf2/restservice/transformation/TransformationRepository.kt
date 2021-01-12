package de.debuglevel.liberatepdf2.restservice.transformation

import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository
import java.util.*

@Repository
interface TransformationRepository : CrudRepository<Transformation, UUID>