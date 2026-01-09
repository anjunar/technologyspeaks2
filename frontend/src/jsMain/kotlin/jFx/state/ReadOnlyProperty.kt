package jFx.state

interface ReadOnlyProperty<T> {
    fun get(): T
    fun observe(listener: (T) -> Unit): Disposable
}

