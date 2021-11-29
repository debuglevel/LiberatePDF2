package de.debuglevel.liberatepdf2.restservice.transformation

import org.hibernate.annotations.GenericGenerator
import java.util.*
import javax.persistence.*

@Entity
data class Transformation(
    /**
     * @implNote: Needs @GeneratedValue(generator = "uuid2"), @GenericGenerator and @Column to work with MariaDB/MySQL. See https://github.com/micronaut-projects/micronaut-data/issues/1210
     */
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    val id: UUID?,

    val originalFilename: String,
    val password: String?,
    var finished: Boolean,
    var failed: Boolean? = null,
    var errorMessage: String? = null,

    @Lob
    val restrictedFile: ByteArray,

    @Lob
    var unrestrictedFile: ByteArray? = null,
) {
    override fun toString(): String {
        return "Transformation(" +
                "id=$id, " +
                "originalFilename='$originalFilename', " +
                "finished=$finished, " +
                "failed=$failed" +
                ")"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Transformation

        if (id != other.id) return false
        if (originalFilename != other.originalFilename) return false
        if (finished != other.finished) return false
        if (failed != other.failed) return false
        if (!restrictedFile.contentEquals(other.restrictedFile)) return false
        if (!unrestrictedFile.contentEquals(other.unrestrictedFile)) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
