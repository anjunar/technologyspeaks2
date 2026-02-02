package jFx2.virtual

interface RangeDataProvider<T> {
    val hasKnownCount: Boolean
    val knownCount: Int

    suspend fun ensureRange(from: Int, toInclusive: Int)
    fun getOrNull(index: Int): T?

    val endReached: Boolean
    val loadedCount: Int
}
