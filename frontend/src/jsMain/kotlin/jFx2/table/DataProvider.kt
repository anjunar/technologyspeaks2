package jFx2.table

import jFx2.state.Property
import kotlinx.coroutines.*
import kotlin.math.floor

interface DataProvider<T> {
    /** null => unknown/infinite */
    val totalCount: Property<Int?>
    suspend fun loadRange(offset: Int, limit: Int): List<T>
}

class LazyTableModel<T>(
    private val scope: CoroutineScope,
    private val provider: DataProvider<T>,
    private val pageSize: Int = 200,
    private val prefetchPages: Int = 2
) {
    val totalCount: Property<Int?> get() = provider.totalCount

    /** bump => UI should re-render visible rows */
    val invalidateTick = Property(0L)

    private val cache = HashMap<Int, List<T>>()                  // pageIndex -> items
    private val inflight = HashMap<Int, Deferred<List<T>>>()     // pageIndex -> request

    fun clearCache() {
        cache.clear()
        invalidateTick.set(invalidateTick.get() + 1)
    }

    fun get(index: Int): T? {
        val pageIndex = floorDiv(index, pageSize)
        cache[pageIndex]?.let { page ->
            val local = index - pageIndex * pageSize
            return if (local in page.indices) page[local] else null
        }

        // load target page and neighbors
        requestPage(pageIndex)
        for (i in 1..prefetchPages) {
            requestPage(pageIndex - i)
            requestPage(pageIndex + i)
        }
        return null
    }

    private fun requestPage(pageIndex: Int) {
        if (pageIndex < 0) return
        if (cache.containsKey(pageIndex)) return
        if (inflight.containsKey(pageIndex)) return

        val total = totalCount.get()
        if (total != null) {
            val start = pageIndex * pageSize
            if (start >= total) return
        }

        val d = scope.async(Dispatchers.Default) {
            provider.loadRange(pageIndex * pageSize, pageSize)
        }
        inflight[pageIndex] = d

        d.invokeOnCompletion { cause ->
            inflight.remove(pageIndex)
            if (cause == null) {
                val list = runCatching { d.getCompleted() }.getOrNull()
                if (list != null) {
                    cache[pageIndex] = list
                    invalidateTick.set(invalidateTick.get() + 1)
                }
            }
        }
    }

    private fun floorDiv(a: Int, b: Int): Int = floor(a.toDouble() / b.toDouble()).toInt()
}
