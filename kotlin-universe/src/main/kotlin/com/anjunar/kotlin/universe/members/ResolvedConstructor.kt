package com.anjunar.kotlin.universe.members

import com.anjunar.kotlin.universe.ResolvedClass
import java.lang.reflect.Constructor

class ResolvedConstructor(override val underlying: Constructor<*>, owner: ResolvedClass) : ResolvedExecutable(underlying, owner) {

    override val declaredAnnotations: Array<Annotation> by lazy {
        underlying.declaredAnnotations
    }

    override val annotations: Array<Annotation> by lazy {
        underlying.declaredAnnotations
    }

    fun newInstance(vararg args: Any?): Any? {
        return underlying.newInstance(*args)
    }

    override fun toString(): String =
        "ResolvedConstructor(${parameters.joinToString(", ")})"

}