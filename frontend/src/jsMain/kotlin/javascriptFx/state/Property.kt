package javascriptFx.state

typealias Disposable = () -> Unit

interface ReadOnlyProperty<T> {
    fun get(): T
    fun observe(listener: (T) -> Unit): Disposable
}

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
}
