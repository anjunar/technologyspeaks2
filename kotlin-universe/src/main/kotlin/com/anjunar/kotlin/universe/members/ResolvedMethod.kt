package com.anjunar.kotlin.universe.members

import com.anjunar.kotlin.universe.ResolvedClass
import com.anjunar.kotlin.universe.TypeResolver
import com.google.common.reflect.TypeToken
import java.lang.reflect.Method

class ResolvedMethod(override val underlying: Method, owner: ResolvedClass) : ResolvedExecutable(underlying, owner) {

    val name: String by lazy {
        underlying.name
    }

    val overrides: Array<ResolvedMethod> by lazy {
        owner.hierarchy
            .drop(1)
            .mapNotNull { cls ->
                try {
                    cls.findMethod(name, *underlying.parameterTypes)
                } catch (e: NoSuchMethodException) {
                    null
                }
            }
            .toTypedArray()
    }

    val returnType: ResolvedClass by lazy {
        val resolvedType = TypeToken.of(owner.underlying)
            .resolveType(underlying.genericReturnType)
            .type
        TypeResolver.resolve(resolvedType)
    }

    fun invoke(instance: Any, vararg args: Any?): Any? {
        underlying.isAccessible = true
        return underlying.invoke(instance, *args)
    }

    override val declaredAnnotations: Array<Annotation> by lazy {
        underlying.declaredAnnotations
    }

    override val annotations: Array<Annotation> by lazy {
        declaredAnnotations +
                overrides.flatMap { it.declaredAnnotations.asIterable() }
                    .toTypedArray()
    }

    override fun toString(): String =
        "ResolvedMethod($name, $returnType, ${parameters.joinToString(", ")})"
}