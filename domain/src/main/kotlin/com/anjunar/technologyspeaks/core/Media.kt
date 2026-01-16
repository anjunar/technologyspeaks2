package com.anjunar.technologyspeaks.core

import com.anjunar.json.mapper.schema.EntitySchema
import com.anjunar.json.mapper.schema.SchemaProvider
import jakarta.json.bind.annotation.JsonbProperty
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.NamedAttributeNode
import jakarta.persistence.NamedEntityGraph
import jakarta.persistence.NamedSubgraph
import jakarta.persistence.OneToOne

@Entity
@NamedEntityGraph(
    name = "Media.full",
    attributeNodes = [
        NamedAttributeNode("name"),
        NamedAttributeNode("contentType"),
        NamedAttributeNode("data"),
        NamedAttributeNode("thumbnail")
    ]
)
class Media(name: String, contentType: String, data: ByteArray) : Thumbnail(name, contentType, data) {

    @OneToOne(cascade = [CascadeType.ALL])
    @JsonbProperty
    lateinit var thumbnail: Thumbnail

    companion object : SchemaProvider {

        class Schema : EntitySchema<Media>() {
            @JsonbProperty val name = property(Media::name)
            @JsonbProperty val contentType = property(Media::contentType)
            @JsonbProperty val data = property(Media::data)
            @JsonbProperty val thumbnail = property(Media::thumbnail)
        }

    }

}