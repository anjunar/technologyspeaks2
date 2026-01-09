package jFx.state

object Operators {
    fun <T> Property<T>.bind(source: ReadOnlyProperty<T>): Disposable =
        source.observe { set(it) }

    class MappedProperty<A, B>(
        private val source: ReadOnlyProperty<A>,
        private val mapper: (A) -> B
    ) : ReadOnlyProperty<B> {
        override fun get(): B = mapper(source.get()!!)
        override fun observe(listener: (B?) -> Unit): Disposable =
            source.observe { a -> listener(mapper(a!!)) }
    }

    fun <A, B> ReadOnlyProperty<A>.map(mapper: (A) -> B): ReadOnlyProperty<B> =
        MappedProperty(this, mapper)

    fun <A, R> computed(
        source: ReadOnlyProperty<A>,
        target: Property<R>,
        mapper: (A) -> R
    ): Disposable {
        return source.observe { a ->
            target.set(mapper(a!!))
        }
    }
}

