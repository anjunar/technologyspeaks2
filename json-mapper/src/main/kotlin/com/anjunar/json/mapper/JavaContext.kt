package com.anjunar.json.mapper

import com.anjunar.kotlin.universe.ResolvedClass
import jakarta.persistence.EntityGraph

class JavaContext(val type : ResolvedClass, val graph : EntityGraph<*>?, val parent : JavaContext?, val name : String?) {

    fun path() : List<String> {
        val parentPath = emptyList<String>()

        var cursor: JavaContext? = this

        while (cursor != null) {
            parentPath + cursor.name
            cursor = cursor.parent
        }

        return parentPath.reversed()
    }

}