package com.anjunar.technologyspeaks.core

import com.anjunar.json.mapper.schema.EntitySchema
import com.anjunar.json.mapper.schema.SchemaProvider
import jakarta.json.bind.annotation.JsonbProperty
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Lob
import jakarta.persistence.NamedAttributeNode
import jakarta.persistence.NamedEntityGraph
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Entity
@Table(name = "Core#Media")
@NamedEntityGraph(
    name = "Thumbnail.full",
    attributeNodes = [
        NamedAttributeNode(value = "name"),
        NamedAttributeNode(value = "contentType"),
        NamedAttributeNode(value = "data")
    ]
)
class Thumbnail(

    @NotBlank
    @Size(min = 2, max = 80)
    @Column(unique = true)
    @JsonbProperty
    var name: String,

    @NotBlank
    @Size(min = 2, max = 80)
    @JsonbProperty
    var contentType: String,

    @Lob
    @JsonbProperty
    var data: ByteArray
) : AbstractEntity() {

    companion object : SchemaProvider {

        class Schema : EntitySchema<Thumbnail>() {
            val name = property(Thumbnail::name)
            val contentType = property(Thumbnail::contentType)
            val data = property(Thumbnail::data)
        }

    }

}