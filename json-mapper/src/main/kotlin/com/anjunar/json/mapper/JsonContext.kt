package com.anjunar.json.mapper

import com.anjunar.json.mapper.deserializer.DeserializerRegistry
import com.anjunar.kotlin.universe.ResolvedClass
import jakarta.persistence.EntityGraph

class JsonContext(val type : ResolvedClass,
                  val instance : Any?,
                  val graph : EntityGraph<*>?,
                  val loader : EntityLoader,
                  val parent : JsonContext?,
                  val name : String?) {

    var doNotUpdateFields = false

    fun path() : List<String> {
        val parentPath = emptyList<String>()

        var cursor: JsonContext? = this

        while (cursor != null) {
            parentPath + cursor.name
            cursor = cursor.parent
        }

        return parentPath.reversed()
    }
}