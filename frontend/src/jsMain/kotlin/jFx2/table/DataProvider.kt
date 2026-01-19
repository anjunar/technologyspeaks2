package jFx2.table

import jFx2.state.Property
import kotlinx.coroutines.*
import kotlin.math.max
import kotlin.math.min

/**
 * Range-based data source (Lazy loading).
 *
 * totalCount:
 *  - null => unknown / infinite (grow-on-demand)
 *  - n => finite size
 */
interface DataProvider<T> {
    val totalCount: Property<Int?>
    suspend fun loadRange(offset: Int, limit: Int): List<T>
}

/**
 * Lazy model with cache + inflight de-duplication.
 *
 * Exposes:
 *  - get(index): T?  (null => not loaded yet)
 *  - invalidateTick: Property<Long>  (bump to trigger UI refresh)
 */
class LazyTableModel<T>(
    private val scope: CoroutineScope,
    private val provider: DataProvider<T>,
    private val pageSize: Int = 200,
    private val prefetchPages: Int = 2
) {
    val totalCount: Property<Int?> get() = provider.totalCount

    /** Bump to tell the virtual flow: "some rows may have changed now" */
    val invalidateTick = Property(0L)

    // Simple page cache: pageIndex -> list of items in that page
    private val cache = HashMap<Int, List<T>>()

    // inflight: pageIndex -> deferred
    private val inflight = HashMap<Int, Deferred<List<T>>>()

    fun clearCache() {
        cache.clear()
        // don't cancel inflight automatically; depends on your preference
        invalidateTick.value = invalidateTick.value + 1
    }

    fun get(index: Int): T? {
        val pageIndex = floorDiv(index, pageSize)
        val page = cache[pageIndex]
        if (page != null) {
            val local = index - pageIndex * pageSize
            return if (local in page.indices) page[local] else null
        }

        // schedule load for this and some neighbors
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

        val total = totalCount.value
        if (total != null) {
            val start = pageIndex * pageSize
            if (start >= total) return
        }

        val deferred = scope.async(Dispatchers.Default) {
            provider.loadRange(pageIndex * pageSize, pageSize)
        }
        inflight[pageIndex] = deferred

        deferred.invokeOnCompletion { cause ->
            inflight.remove(pageIndex)
            if (cause == null) {
                val list = runCatching { deferred.getCompleted() }.getOrNull()
                if (list != null) {
                    cache[pageIndex] = list
                    invalidateTick.value = invalidateTick.value + 1
                }
            }
        }
    }

    private fun floorDiv(a: Int, b: Int): Int = kotlin.math.floor(a.toDouble() / b.toDouble()).toInt()
}
