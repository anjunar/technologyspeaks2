package com.anjunar.json.mapper.schema

import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

@Suppress("UNCHECKED_CAST")
abstract class EntitySchema<T> {

    val properties = LinkedHashMap<String, Property<T, Any>>()

    fun <V>property(property : KProperty1<T, V>, visibilityRule: VisibilityRule<T> = DefaultRule() as VisibilityRule<T>) : Property<T,V> {
        val prop = Property(property, visibilityRule)
        properties[property.name] = prop as Property<T, Any>
        return prop
    }

}