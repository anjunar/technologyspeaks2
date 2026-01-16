package com.anjunar.kotlin.universe

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType
import java.util.HashMap

object TypeResolver {

    val cache: MutableMap<Type, ResolvedClass> = HashMap()

    fun companionClass(clazz: Class<*>): Class<*>? {



        val companionClassName = clazz.name + "$"
        return try {
            Class.forName(companionClassName)
        } catch (e: ClassNotFoundException) {
            null
        }
    }

    fun companionInstance(clazz: Class<*>): Any? {
        return try {
            val value = companionClass(clazz) ?: return null
            value.getField("MODULE$").get(null)
        } catch (e: NoSuchFieldException) {
            null
        }
    }

    fun resolve(aType: Type): ResolvedClass {
        val existing = cache[aType]
        if (existing != null) {
            return existing
        }
        val resolvedClass = ResolvedClass(aType)
        cache.put(aType, resolvedClass)
        return resolvedClass
    }

    fun rawType(aType: Type): Class<*> = when (aType) {
        is Class<*> -> aType
        is ParameterizedType -> rawType(aType.rawType)
        is TypeVariable<*> -> rawType(generateParameterizedType(aType))
        is WildcardType -> rawType(aType.upperBounds.get(0))
        else -> throw IllegalStateException("Unexpected value: $aType")
    }

    private fun generateParameterizedType(typeVariable: TypeVariable<*>): ParameterizedType {
        return object : ParameterizedType {
            override fun getActualTypeArguments(): Array<Type> = typeVariable.bounds

            override fun getRawType(): Type = typeVariable.genericDeclaration as Type

            override fun getOwnerType(): Type? = null
        }
    }


}