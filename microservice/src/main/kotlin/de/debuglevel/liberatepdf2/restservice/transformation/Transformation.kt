package de.debuglevel.liberatepdf2.restservice.transformation

import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
data class Transformation(
    @Id
    @GeneratedValue
    var id: UUID?,
    var originalFilename: String,
    var password: String?,
    var finished: Boolean,
    var failed: Boolean? = null,
    var errorMessage: String? = null,
    var restrictedStoredFileId: UUID,
    var unrestrictedStoredFileId: UUID? = null,
)
