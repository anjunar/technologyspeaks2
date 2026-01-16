package com.anjunar.kotlin.universe.members

import com.anjunar.kotlin.universe.ResolvedClass
import java.lang.reflect.Executable

abstract class ResolvedExecutable(override val underlying: Executable, owner: ResolvedClass) : ResolvedMember(underlying, owner) {

    val parameters: Array<ResolvedParameter> by lazy {
        underlying.parameters
            .map { param -> ResolvedParameter(param, this) }
            .toTypedArray()
    }

}