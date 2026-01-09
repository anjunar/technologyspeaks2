package jFx.state

class Property<T>(initial: T) : ReadOnlyProperty<T> {
    private var value: T = initial
    private val listeners = LinkedHashMap<Int, (T) -> Unit>()
    private var nextId = 1

    override fun get(): T = value

    fun set(newValue: T) {
        if (newValue == value) return
        value = newValue
        val snapshot = listeners.values.toList()
        snapshot.forEach { it(newValue) }
    }

    override fun observe(listener: (T) -> Unit): Disposable {
        val id = nextId++
        listeners[id] = listener
        listener(value)
        return { listeners.remove(id) }
    }

    fun subscribe(other: Property<T>): Disposable {
        var updating = false

        val d1 = this.observe { v ->
            if (updating) return@observe
            updating = true
            other.set(v)
            updating = false
        }

        val d2 = other.observe { v ->
            if (updating) return@observe
            updating = true
            this.set(v)
            updating = false
        }

        return {
            d1()
            d2()
        }
    }
}
