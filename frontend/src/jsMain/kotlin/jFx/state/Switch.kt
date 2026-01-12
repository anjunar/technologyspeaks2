package jFx.state

import jFx.core.ParentScope


class SwitchScope<T>(
    private val parent: ParentScope,
    private val value: T
) {
    private var matched: Boolean = false

    fun case(expected: T, body: ParentScope.() -> Unit) {
        if (!matched && value == expected) {
            matched = true
            parent.body()
        }
    }

    fun caseIf(predicate: (T) -> Boolean, body: ParentScope.() -> Unit) {
        if (!matched && predicate(value)) {
            matched = true
            parent.body()
        }
    }

    fun otherwise(body: ParentScope.() -> Unit) {
        if (!matched) {
            matched = true
            parent.body()
        }
    }
}

inline fun <T> ParentScope.switch(value: T, block: SwitchScope<T>.() -> Unit) {
    SwitchScope(this, value).block()
}
