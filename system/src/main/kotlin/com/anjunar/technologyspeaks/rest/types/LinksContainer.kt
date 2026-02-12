package com.anjunar.technologyspeaks.rest.types

import com.anjunar.json.mapper.schema.Link
import jakarta.json.bind.annotation.JsonbProperty
import jakarta.persistence.Transient

object LinksContainer {

    interface Interface {

        @get:JsonbProperty(value = $$"$links")
        @get:Transient
        val links: MutableList<Link>

        fun addLinks(vararg value: Link?) {
            value.filterNotNull().forEach { link -> links.add(link)}
        }
    }

    open class Trait : Interface {

        override val links: MutableList<Link> = mutableListOf()

    }

}