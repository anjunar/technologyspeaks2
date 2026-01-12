package jFx2.state

class Property<T>(initial: T) : ReadOnlyProperty<T> {
    private var v: T = initial
    private val listeners = LinkedHashMap<Int, (T) -> Unit>()
    private var id = 1
    fun set(n: T) { if (n == v) return; v = n; listeners.values.toList().forEach { it(n) } }
    override fun get(): T = v
    override fun observe(listener: (T) -> Unit): () -> Unit {
        val my = id++
        listeners[my] = listener
        listener(v)
        return { listeners.remove(my) }
    }
}