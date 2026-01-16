package com.anjunar.json.mapper.schema

import kotlin.reflect.full.isSubclassOf

@Suppress("UNCHECKED_CAST")
interface SchemaProvider {

    fun schema(): EntitySchema<*> {
        val clazz = this::class.nestedClasses.find { it.isSubclassOf(EntitySchema::class) }
            ?: throw IllegalStateException("Schema class not found")

        return clazz.constructors.first().call() as EntitySchema<*>
    }

}