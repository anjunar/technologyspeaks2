package com.anjunar.json.mapper.schema

import jakarta.json.bind.annotation.JsonbProperty

class Link(
    @JsonbProperty val rel : String,
    @JsonbProperty val url : String,
    @JsonbProperty val method : String,
    @JsonbProperty val id : String
) {

    companion object : SchemaProvider {

        class Schema : EntitySchema<Link>() {
            @JsonbProperty val id = property(Link::id)
            @JsonbProperty val rel = property(Link::rel)
            @JsonbProperty val url = property(Link::url)
            @JsonbProperty val method = property(Link::method)
        }

    }

}