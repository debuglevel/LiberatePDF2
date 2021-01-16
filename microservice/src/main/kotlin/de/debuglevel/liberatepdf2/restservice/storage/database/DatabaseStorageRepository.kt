package de.debuglevel.liberatepdf2.restservice.storage.database

import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository
import java.util.*

@Repository
interface DatabaseStorageRepository : CrudRepository<Transformation, UUID>