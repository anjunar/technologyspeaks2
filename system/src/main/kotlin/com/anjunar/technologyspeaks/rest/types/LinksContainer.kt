package com.anjunar.technologyspeaks.rest.types

import com.anjunar.json.mapper.schema.Link
import jakarta.json.bind.annotation.JsonbProperty

object LinksContainer {

    interface Interface {

        @get:JsonbProperty(value = $$"$links")
        val links: MutableList<Link>

        fun addLinks(vararg value: Link?) {
            value.filterNotNull().forEach { link -> links.add(link)}
        }
    }

    class Trait : Interface {

        override val links: MutableList<Link> = mutableListOf()

    }

}