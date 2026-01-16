package com.anjunar.json.mapper.schema

import jakarta.json.bind.annotation.JsonbProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure

@Suppress("UNCHECKED_CAST")
class Property<T,V>(property : KProperty1<T, V>, var rule : VisibilityRule<T>) {

    @JsonbProperty
    val type = property.returnType.jvmErasure.simpleName

    @JsonbProperty
    val schema = run {

        val jvmErasure = property.returnType.jvmErasure

        if (jvmErasure.isSubclassOf(Collection::class)) {
            val collectionType = property.returnType.arguments.first().type?.jvmErasure
            fetchCompanionSchema(collectionType!!)
        } else {
            fetchCompanionSchema(jvmErasure)
        }

    }

    private fun fetchCompanionSchema(jvmErasure: KClass<*>): EntitySchema<*>? {
        val companionObject = jvmErasure.companionObject

        return if (companionObject != null) {
            if (companionObject.isSubclassOf(SchemaProvider::class)) {
                val companionInstance = jvmErasure.companionObjectInstance as SchemaProvider
                companionInstance.schema()
            } else {
                null
            }
        } else {
            null
        }
    }

}

