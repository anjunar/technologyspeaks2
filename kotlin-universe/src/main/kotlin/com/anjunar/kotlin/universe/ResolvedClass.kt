package com.anjunar.kotlin.universe

import com.anjunar.kotlin.universe.annotations.Annotated
import com.anjunar.kotlin.universe.members.ResolvedConstructor
import com.anjunar.kotlin.universe.members.ResolvedField
import com.anjunar.kotlin.universe.members.ResolvedMethod
import com.google.common.reflect.TypeToken
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.KClass

class ResolvedClass(val underlying : Type) : Annotated {

    val name: String by lazy { raw.simpleName }

    val fullName: String by lazy { raw.name }

    val raw: Class<*> by lazy { TypeResolver.rawType(underlying) }

    val kotlin : KClass<*> = raw.kotlin

    val hierarchy: Array<ResolvedClass> by lazy {
        val result = ArrayList<ResolvedClass>()
        var cursor: Type? = underlying
        while (cursor != null) {
            val resolvedType = TypeToken.of(underlying).resolveType(cursor).type
            result.add(TypeResolver.resolve(resolvedType))
            val rawClass = TypeResolver.rawType(resolvedType)
            rawClass.genericInterfaces.forEach { intf ->
                val ifaceType = TypeToken.of(underlying).resolveType(intf).type
                result.add(TypeResolver.resolve(ifaceType))
            }
            cursor = rawClass.genericSuperclass
            if (cursor == Object::class.java) {
                cursor = null
            }
        }
        result.toTypedArray()
    }

    val declaredFields: Array<ResolvedField> by lazy {
        raw.declaredFields.map { field -> ResolvedField(field, this) }.toTypedArray()
    }

    val declaredConstructors: Array<ResolvedConstructor> by lazy {
        raw.declaredConstructors.map { ctor -> ResolvedConstructor(ctor, this) }.toTypedArray()
    }

    val declaredMethods: Array<ResolvedMethod> by lazy {
        raw.declaredMethods.map { method -> ResolvedMethod(method, this) }.toTypedArray()
    }

    val fields: Array<ResolvedField> by lazy {
        val allFields = hierarchy.flatMap { it.declaredFields.asIterable() }
        val hidden = allFields.flatMap { it.hidden.asIterable() }
        allFields.filter { it !in hidden }.toTypedArray()
    }

    val constructors: Array<ResolvedConstructor> by lazy {
        hierarchy.flatMap { it.declaredConstructors.asIterable() }.toTypedArray()
    }

    val methods: Array<ResolvedMethod> by lazy {
        val allMethods = hierarchy.flatMap { it.declaredMethods.asIterable() }
        val overridden = allMethods.flatMap { it.overrides.asIterable() }
        allMethods.filter { it !in overridden }.toTypedArray()
    }

    val typeArguments: Array<ResolvedClass> by lazy {
        when (underlying) {
            is ParameterizedType ->
                underlying.actualTypeArguments
                    .map { aType -> TypeResolver.resolve(aType) }
                    .toTypedArray()
            else -> emptyArray()
        }
    }

    infix fun isSubtypeOf(aClass: ResolvedClass): Boolean =
        TypeToken.of(TypeToken.of(underlying).wrap().type).isSubtypeOf(aClass.underlying)

    fun findDeclaredField(name: String): ResolvedField? =
        try {
            val field = raw.getDeclaredField(name)
            declaredFields.firstOrNull { resolvedField -> resolvedField == field }
        } catch (e: NoSuchFieldException) {
            null
        }

    fun findDeclaredConstructor(vararg args: Class<*>): ResolvedConstructor? =
        try {
            val constructor = raw.getDeclaredConstructor(*args)
            declaredConstructors.firstOrNull { resolvedConstructor ->
                resolvedConstructor.underlying == constructor
            }
        } catch (e: NoSuchMethodException) {
            null
        }

    fun findDeclaredMethod(name: String, vararg args: Class<*>): ResolvedMethod? =
        try {
            val method = raw.getDeclaredMethod(name, *args)
            declaredMethods.firstOrNull { resolvedMethod ->
                resolvedMethod.underlying == method
            }
        } catch (e: NoSuchMethodException) {
            null
        }

    fun findField(name: String): ResolvedField? =
        try {
            fields.firstOrNull { resolvedField -> resolvedField.name == name }
        } catch (e: NoSuchFieldException) {
            null
        }

    fun findConstructor(vararg args: Class<*>): ResolvedConstructor? =
        try {
            val constructor = raw.getConstructor(*args)
            constructors.firstOrNull { resolvedConstructor ->
                resolvedConstructor.underlying == constructor
            }
        } catch (e: NoSuchMethodException) {
            null
        }

    fun findMethod(name: String, vararg args: Class<*>): ResolvedMethod? =
        try {
            val method = raw.getMethod(name, *args)
            methods.firstOrNull { resolvedMethod ->
                resolvedMethod.underlying == method
            }
        } catch (e: NoSuchMethodException) {
            null
        }

    override val declaredAnnotations: Array<Annotation> by lazy {
        raw.declaredAnnotations
    }

    override val annotations: Array<Annotation> by lazy {
        hierarchy.flatMap { it.declaredAnnotations.asIterable() }.toTypedArray()
    }

    private fun canEqual(other: Any?): Boolean = other is ResolvedClass

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ResolvedClass) return false
        if (!other.canEqual(this)) return false
        return underlying == other.underlying
    }

    override fun hashCode(): Int {
        val state = listOf(underlying)
        return state.map { it.hashCode() }.fold(0) { a, b -> 31 * a + b }
    }

    override fun toString(): String = "ResolvedClass($name)"

}