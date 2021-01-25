package io.github.pacdouglas.starwarssearch.model

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.IdClass
import javax.persistence.Table

data class RawDataKey(val name: String = "", val type: String = "") : Serializable

@Entity
@Table(name = "SW_API_RAW_DATA")
@IdClass(RawDataKey::class)
data class SwApiRawData(
        @Id
        val name: String,
        @Id
        val type: String,
        @Column(nullable = false, length = 4096)
        val data: String
) {
    override fun toString(): String {
        return "[${this.type}][${this.name}]"
    }
}