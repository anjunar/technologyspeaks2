package state

class ListProperty<T>(initial: List<T> = emptyList()) : ReadOnlyProperty<List<T>> {
    private val inner = Property(initial)

    override fun get(): List<T> = inner.get()
    fun set(items: List<T>) = inner.set(items)

    override fun observe(listener: (List<T>) -> Unit): Disposable =
        inner.observe(listener)
}
