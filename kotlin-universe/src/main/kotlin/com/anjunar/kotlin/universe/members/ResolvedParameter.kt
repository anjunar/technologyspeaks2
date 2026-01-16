package com.anjunar.kotlin.universe.members

import com.anjunar.kotlin.universe.ResolvedClass
import com.anjunar.kotlin.universe.TypeResolver
import com.anjunar.kotlin.universe.annotations.Annotated
import com.google.common.reflect.TypeToken
import java.lang.reflect.Parameter

class ResolvedParameter(val underlying: Parameter, private val owner: ResolvedExecutable) : Annotated {

    val name: String by lazy {
        underlying.name
    }

    val parameterType: ResolvedClass by lazy {
        val resolvedType = TypeToken.of(owner.owner.underlying)
            .resolveType(underlying.parameterizedType)
            .type
        TypeResolver.resolve(resolvedType)
    }

    override val declaredAnnotations: Array<Annotation> by lazy {
        underlying.declaredAnnotations
    }

    override val annotations: Array<Annotation> by lazy {
        when (owner) {
            is ResolvedMethod -> {
                val index = owner.parameters.indexOf(this)
                if (index == -1) {
                    declaredAnnotations
                } else {
                    declaredAnnotations + owner.overrides.flatMap { m ->
                        m.parameters[index].declaredAnnotations.asIterable()
                    }.toTypedArray()
                }
            }
            else -> emptyArray()
        }
    }

    override fun toString(): String =
        "ResolvedParameter($name, $parameterType)"
}