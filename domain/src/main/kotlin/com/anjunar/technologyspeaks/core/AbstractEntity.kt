package com.anjunar.technologyspeaks.core

import com.anjunar.json.mapper.provider.EntityProvider
import jakarta.json.bind.annotation.JsonbProperty
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@MappedSuperclass
abstract class AbstractEntity : EntityProvider {

    @Id
    @JsonbProperty
    override val id: UUID = UUID.randomUUID()

    @Version
    @JsonbProperty
    override var version: Long = -1

    lateinit var created: LocalDateTime

    lateinit var modified: LocalDateTime

    @PreUpdate
    fun preUpdate() {
        modified = LocalDateTime.now()
    }

    @PrePersist
    fun prePersist() {
        created = LocalDateTime.now()
    }

    fun isNew(): Boolean = version == -1L

}