package io.github.pacdouglas.starwarssearch.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FieldResult
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinColumns
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table(name = "SW_INFO_SEARCH_COUNT")
data class StarWarsInfoSearchCount(
        @Id
        val searchedName: String,

        @OneToOne
        @JoinColumns(value = [JoinColumn(name = "type"), JoinColumn(name = "name")])
        val found: SwApiRawData?,

        @Column
        val count: Long = 0,
)