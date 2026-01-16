package com.anjunar.kotlin.universe.members

import com.anjunar.kotlin.universe.ResolvedClass
import com.anjunar.kotlin.universe.annotations.Annotated
import java.lang.reflect.Member

abstract class ResolvedMember(open val underlying : Member, val owner : ResolvedClass) : Annotated