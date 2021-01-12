package de.debuglevel.liberatepdf2.restservice.transformation

import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
data class Transformation(
    @Id
    @GeneratedValue
    val id: UUID?,
    val originalFilename: String,
    val password: String?,
    var finished: Boolean,
    var failed: Boolean? = null,
    var errorMessage: String? = null,
    val restrictedStoredFileId: UUID,
    var unrestrictedStoredFileId: UUID? = null,
)
