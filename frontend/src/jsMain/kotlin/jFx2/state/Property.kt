package jFx2.state

import jFx2.core.capabilities.Disposable

class Property<T>(initial: T) : ReadOnlyProperty<T> {
    private var value: T = initial
    private val listeners = LinkedHashMap<Int, (T) -> Unit>()
    private var nextId = 1

    override fun get(): T = value

    fun set(newValue: T) {
        if (newValue == value) return
        value = newValue
        listeners.values.toList().forEach { it(newValue) }
    }

    override fun observe(listener: (T) -> Unit): Disposable {
        val id = nextId++
        listeners[id] = listener
        listener(value)
        return { listeners.remove(id) }
    }
}
