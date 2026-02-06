package jFx2.virtual

interface RangeDataProvider<T> {
    val hasKnownCount: Boolean
    val knownCount: Int
    val endReached: Boolean
    val loadedCount: Int

    suspend fun ensureRange(from: Int, toInclusive: Int)

    fun getOrNull(index: Int): T

    fun observeChanges(listener: (jFx2.state.ListChange<*>) -> Unit): jFx2.state.Disposable
}
