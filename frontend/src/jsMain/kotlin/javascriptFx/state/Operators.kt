package javascriptFx.state

fun <T> Property<T>.bind(source: ReadOnlyProperty<T>): Disposable =
    source.observe { set(it) }

class MappedProperty<A, B>(
    private val source: ReadOnlyProperty<A>,
    private val mapper: (A) -> B
) : ReadOnlyProperty<B> {
    override fun get(): B = mapper(source.get())
    override fun observe(listener: (B) -> Unit): Disposable =
        source.observe { a -> listener(mapper(a)) }
}

fun <A, B> ReadOnlyProperty<A>.map(mapper: (A) -> B): ReadOnlyProperty<B> =
    MappedProperty(this, mapper)

class ComputedProperty<T>(
    private val sources: List<ReadOnlyProperty<*>>,
    private val compute: () -> T
) : ReadOnlyProperty<T> {
    override fun get(): T = compute()

    override fun observe(listener: (T) -> Unit): Disposable {
        // initial
        listener(compute())
        val disposables = sources.map { it.observe { listener(compute()) } }
        return { disposables.forEach { it() } }
    }
}

fun <A, B, R> computed(a: ReadOnlyProperty<A>, b: ReadOnlyProperty<B>, f: (A, B) -> R): ReadOnlyProperty<R> =
    ComputedProperty(listOf(a, b)) { f(a.get(), b.get()) }
