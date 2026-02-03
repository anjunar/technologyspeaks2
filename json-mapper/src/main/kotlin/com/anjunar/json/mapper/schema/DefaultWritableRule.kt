package com.anjunar.json.mapper.schema

import com.anjunar.kotlin.universe.introspector.AbstractProperty

class DefaultWritableRule<E> : VisibilityRule<E> {
    override fun isVisible(instance: E?, property : AbstractProperty): Boolean {
        return true
    }

    override fun isWriteable(instance: E?, property : AbstractProperty): Boolean {
        return true
    }
}