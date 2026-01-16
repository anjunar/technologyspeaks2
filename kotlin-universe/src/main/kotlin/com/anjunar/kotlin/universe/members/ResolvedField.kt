package com.anjunar.kotlin.universe.members

import com.anjunar.kotlin.universe.ResolvedClass
import com.anjunar.kotlin.universe.TypeResolver
import com.google.common.reflect.TypeToken
import java.lang.reflect.Field

class ResolvedField(override val underlying: Field, owner: ResolvedClass) : ResolvedMember(underlying, owner) {

    val name: String by lazy {
        underlying.name
    }

    val fieldType: ResolvedClass by lazy {
        val resolvedType = TypeToken.of(owner.underlying)
            .resolveType(underlying.genericType)
            .type
        TypeResolver.resolve(resolvedType)
    }

    val hidden: Array<ResolvedField> by lazy {
        owner.hierarchy
            .drop(1)
            .flatMap { cls -> cls.fields.filter { it.name == name } }
            .toTypedArray()
    }

    fun get(instance: Any): Any? {
        underlying.isAccessible = true
        return underlying.get(instance)
    }

    fun set(instance: Any, value: Any?) {
        underlying.isAccessible = true
        underlying.set(instance, value)
    }

    override val declaredAnnotations: Array<Annotation> by lazy {
        underlying.declaredAnnotations
    }

    override val annotations: Array<Annotation> by lazy {
        declaredAnnotations + hidden.flatMap { it.declaredAnnotations.asIterable() }
    }

    override fun toString(): String = "ResolvedField($name, $fieldType)"
}