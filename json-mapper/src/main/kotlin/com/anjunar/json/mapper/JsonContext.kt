package com.anjunar.json.mapper

import com.anjunar.kotlin.universe.ResolvedClass
import jakarta.persistence.EntityGraph
import jakarta.validation.Validator

class JsonContext(
    val type: ResolvedClass,
    val instance: Any?,
    val graph: EntityGraph<*>?,
    val loader: EntityLoader,
    val validator: Validator,
    val parent: JsonContext?,
    val name: String?
) {

    fun path(): List<String> {
        val parentPath = emptyList<String>()

        var cursor: JsonContext? = this

        while (cursor != null) {
            parentPath + cursor.name
            cursor = cursor.parent
        }

        return parentPath.reversed()
    }
}