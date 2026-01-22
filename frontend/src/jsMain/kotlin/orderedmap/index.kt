@file:JsModule("orderedmap")
@file:JsNonModule

package orderedmap

import kotlin.js.ReadonlyArray

external open class OrderedMap<T> private constructor(content: ReadonlyArray<dynamic>) {
    fun get(key: String): T?
    fun update(key: String, value: T, newKey: String? = definedExternally): OrderedMap<T>
    fun remove(key: String): OrderedMap<T>
    fun addToStart(key: String, value: T): OrderedMap<T>
    fun addToEnd(key: String, value: T): OrderedMap<T>
    fun addBefore(place: String, key: String, value: T): OrderedMap<T>
    fun forEach(fn: (key: String, value: T) -> Any?): Unit
    fun prepend(map: MapLike<T>): OrderedMap<T>
    fun append(map: MapLike<T>): OrderedMap<T>
    fun subtract(map: MapLike<T>): OrderedMap<T>
    fun toObject(): dynamic
    val size: Double

    companion object {
        fun <T> from(map: MapLike<T>): OrderedMap<T>
    }
}

typealias MapLike<T> = dynamic
