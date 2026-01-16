package com.anjunar.technologyspeaks.security

import com.anjunar.json.mapper.schema.Link
import com.anjunar.technologyspeaks.SpringContext
import jakarta.annotation.security.RolesAllowed
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation

class LinkBuilder(val href : String?, var rel : String?, val method : String = "GET") {

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

    fun build() : Link? {

        if (rel == null || href == null) return null

        val uriString = ServletUriComponentsBuilder
            .fromPath("/")
            .path(href)
            .buildAndExpand(variables)
            .toUriString()

        return Link(rel!!, uriString, method, property)
    }

    companion object {

        fun create(function : KFunction<*>) : LinkBuilder {
            val rolesAllowed = function.findAnnotation<RolesAllowed>()

            if (rolesAllowed == null) {
                return generateLinkBuilder(function)
            } else {
                val identityHolder = SpringContext.getBean(IdentityHolder::class)

                if (rolesAllowed.value.any { identityHolder.hasRole(it) }) {
                    return generateLinkBuilder(function)
                } else {
                    return generateLinkBuilder(null)
                }
            }
        }

        private fun generateLinkBuilder(function: KFunction<*>?): LinkBuilder {
            if (function == null) {
                return LinkBuilder(null, null)
            }

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