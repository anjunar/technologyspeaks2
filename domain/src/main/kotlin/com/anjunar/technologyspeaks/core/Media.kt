package com.anjunar.technologyspeaks.core

import com.anjunar.json.mapper.schema.DefaultWritableRule
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

        class Schema : AbstractEntitySchema<Media>() {
            @JsonbProperty val name = property(Media::name, DefaultWritableRule())
            @JsonbProperty val contentType = property(Media::contentType, DefaultWritableRule())
            @JsonbProperty val data = property(Media::data, DefaultWritableRule())
            @JsonbProperty val thumbnail = property(Media::thumbnail, DefaultWritableRule())
        }

    }

}