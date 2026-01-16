package com.anjunar.json.mapper.schema

import jakarta.json.bind.annotation.JsonbProperty

class Link(
    @JsonbProperty val rel : String,
    @JsonbProperty val url : String,
    @JsonbProperty val method : String,
    @JsonbProperty val property : String? = null
) {

    companion object : SchemaProvider {

        class Schema : EntitySchema<Link>() {
            val rel = property(Link::rel)
            val url = property(Link::url)
            val method = property(Link::method)
            val property = property(Link::property)
        }

    }

}