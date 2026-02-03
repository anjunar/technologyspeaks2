package com.anjunar.json.mapper.schema

import com.anjunar.kotlin.universe.introspector.AbstractProperty

interface VisibilityRule<E> {

    fun isVisible(instance : E?, property : AbstractProperty) : Boolean

    fun isWriteable(instance : E?, property : AbstractProperty) : Boolean

}