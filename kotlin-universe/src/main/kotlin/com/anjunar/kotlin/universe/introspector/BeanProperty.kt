package com.anjunar.kotlin.universe.introspector

import com.anjunar.kotlin.universe.members.ResolvedField
import com.anjunar.kotlin.universe.members.ResolvedMethod

class BeanProperty(val owner: BeanModel, name: String, field: ResolvedField?, getter: ResolvedMethod?, setter: ResolvedMethod?)
    : AbstractProperty(name, field, getter, setter)