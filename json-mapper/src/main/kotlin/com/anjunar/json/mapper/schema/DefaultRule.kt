package com.anjunar.json.mapper.schema

import com.anjunar.kotlin.universe.introspector.AbstractProperty

class DefaultRule : VisibilityRule<Any> {
    override fun isVisible(instance: Any?, property : AbstractProperty): Boolean {
        return true
    }

    override fun isWriteable(instance: Any?, property : AbstractProperty): Boolean {
        return false
    }
}