package jFx2.virtual

import jFx2.state.ListProperty
import jFx2.state.Property
import kotlin.math.min

interface RangeDataProvider<T> {
    val items: ListProperty<T>

    val totalCount: Property<Int?>

    suspend fun ensureRange(from: Int, toInclusive: Int)
}

data class RangePage<T>(
    val rows: List<T>,
    val totalCount: Int? = null
)

class AppendRangeDataProvider<T>(
    private val pageSize: Int = 50,
    private val maxItems: Int? = null,
    private val fetch: suspend (index: Int, limit: Int) -> RangePage<T>
) : RangeDataProvider<T> {

    override val items: ListProperty<T> = ListProperty()
    override val totalCount: Property<Int?> = Property(null)

    private fun knownCountClampedOrNull(): Int? {
        val known = totalCount.get()
        val max = maxItems
        return when {
            known == null -> max
            max == null -> known
            else -> min(known, max)
        }
    }

    private fun endReached(): Boolean {
        val c = knownCountClampedOrNull() ?: return false
        return items.size >= c
    }

    override suspend fun ensureRange(from: Int, toInclusive: Int) {
        if (toInclusive < 0) return
        if (endReached()) return

        val target = run {
            val max = knownCountClampedOrNull()
            if (max == null) toInclusive else min(toInclusive, max - 1)
        }
        if (target < items.size) return

        while (items.size <= target && !endReached()) {
            val remaining = target - items.size + 1
            val limit = min(pageSize, remaining)
            if (limit <= 0) return

            val page = fetch(items.size, limit)

            page.totalCount?.let { serverCount ->
                val clamped = maxItems?.let { min(it, serverCount) } ?: serverCount
                totalCount.set(clamped)
            }

            if (page.rows.isEmpty()) {
                if (totalCount.get() == null) totalCount.set(items.size)
                return
            }

            val rowsToAdd = maxItems
                ?.let { max -> page.rows.take(max - items.size) }
                ?: page.rows

            if (rowsToAdd.isEmpty()) {
                if (totalCount.get() == null) totalCount.set(items.size)
                return
            }

            items.addAll(rowsToAdd)

            if (page.rows.size < limit && totalCount.get() == null) {
                totalCount.set(items.size)
                return
            }
        }
    }
}
