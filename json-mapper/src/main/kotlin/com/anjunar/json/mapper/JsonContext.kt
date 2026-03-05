package com.anjunar.json.mapper

import com.anjunar.kotlin.universe.ResolvedClass
import jakarta.persistence.EntityGraph
import jakarta.validation.ConstraintViolation
import jakarta.validation.Validator

class JsonContext(
    val type: ResolvedClass,
    val instance: Any?,
    val graph: EntityGraph<*>?,
    val loader: EntityLoader,
    val validator: Validator,
    val parent: JsonContext?,
    val name: String?,
    val index : Int = -1
) {

    init {
        parent?.children?.add(this)
    }

    val children : MutableList<JsonContext> = mutableListOf()

    val violations : MutableSet<ConstraintViolation<*>> = mutableSetOf()

    fun flatten() : List<JsonContext> {
        return arrayListOf(this) + children.flatMap { it.flatten() }
    }

    fun checkForViolations(clazz : Class<*>, name : String, value : Any?, callback : () -> Unit) {
        val v = validator.validateValue(clazz, name, value)
        if (v.isNotEmpty()) {
            violations.addAll(v)
        } else {
            callback()
        }
    }

    fun path(): List<String> {
        val parentPath = mutableListOf<String>()

        var cursor: JsonContext? = this

        while (cursor != null) {
            if (cursor.name != null) {
                parentPath.add(cursor.name)
            }
            cursor = cursor.parent
        }

        return parentPath.reversed()
    }

    fun pathWithIndexes(): List<Any> {
        val parentPath = mutableListOf<Any>()

        var cursor: JsonContext? = this

        while (cursor != null) {
            if (cursor.index > -1) {
                parentPath.add(cursor.index)
            } else {
                if (cursor.name != null) {
                    parentPath.add(cursor.name)
                }
            }
            cursor = cursor.parent
        }

        return parentPath.reversed()
    }

}