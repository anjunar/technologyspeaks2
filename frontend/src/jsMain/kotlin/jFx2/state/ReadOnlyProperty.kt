package jFx2.state

import jFx2.core.capabilities.Disposable

interface ReadOnlyProperty<T> {
    fun get(): T
    fun observe(listener: (T) -> Unit): Disposable
}

fun <T> ReadOnlyProperty<T>.subscribe(target: Property<T>): Disposable {
    return observe { v -> target.set(v) }
}