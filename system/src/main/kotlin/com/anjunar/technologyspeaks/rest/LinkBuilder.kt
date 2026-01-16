package com.anjunar.technologyspeaks.rest

import com.anjunar.json.mapper.schema.Link
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import kotlin.reflect.KFunction

class LinkBuilder(val href : String, var rel : String, val method : String = "GET") {

    val variables = mutableMapOf<String, Any>()

    var property : String = ""

    fun withVariable(name : String, value : Any) : LinkBuilder {
        variables[name] = value
        return this
    }

    fun withRel(rel : String) : LinkBuilder {
        this.rel = rel
        return this
    }

    fun withProperty(property : String) : LinkBuilder {
        this.property = property
        return this
    }

    fun build() : Link {
        val uriString = ServletUriComponentsBuilder
            .fromPath("/")
            .path(href)
            .buildAndExpand(variables)
            .toUriString()

        return Link(rel, uriString, method, property)
    }

    companion object {

        fun create(function : KFunction<*>) : LinkBuilder {
            for (annotation in function.annotations) {
                when (annotation) {
                    is GetMapping -> return LinkBuilder(annotation.value.first(), function.name)
                    is PostMapping -> return LinkBuilder(annotation.value.first(), function.name, "POST")
                    is PutMapping -> return LinkBuilder(annotation.value.first(), function.name, "PUT")
                    else -> null
                }
            }

            throw IllegalArgumentException("No Mapping found for function $function")
        }

    }

}