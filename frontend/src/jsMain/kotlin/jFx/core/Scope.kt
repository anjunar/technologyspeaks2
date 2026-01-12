package jFx.core

class Scope private constructor(
    private val parent: Scope?,
    private val values: Map<ScopeKey<*>, Any>
) {
    constructor() : this(parent = null, values = emptyMap())

    fun <T : Any> get(key: ScopeKey<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return (values[key] as? T) ?: parent?.get(key)
    }

    fun <T : Any> with(key: ScopeKey<T>, value: T): Scope =
        Scope(parent = this, values = mapOf(key to value))

    inline fun <R> use(temp: Scope, block: () -> R): R = block()
}

