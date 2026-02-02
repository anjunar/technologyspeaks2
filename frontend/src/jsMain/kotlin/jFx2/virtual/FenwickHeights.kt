package jFx2.virtual

import kotlin.math.max

class FenwickHeights {
    private var n: Int = 0
    private var tree: IntArray = IntArray(1)

    val size: Int get() = n

    fun ensureSize(newSize: Int) {
        if (newSize <= n) return
        val newTree = IntArray(newSize + 1)
        for (i in 1..n) {
            newTree[i] = tree[i]
        }
        tree = newTree
        n = newSize
    }

    fun rebuildFromHeights(heights: IntArray) {
        n = heights.size
        tree = IntArray(n + 1)
        for (i in heights.indices) {
            add(i, heights[i])
        }
    }

    fun prefixSum(indexExclusive: Int): Int {
        val idx = indexExclusive.coerceIn(0, n)
        var sum = 0
        var i = idx
        while (i > 0) {
            sum += tree[i]
            i -= i and -i
        }
        return sum
    }

    fun add(index: Int, delta: Int) {
        if (index < 0 || index >= n || delta == 0) return
        var i = index + 1
        while (i <= n) {
            tree[i] += delta
            i += i and -i
        }
    }

    fun totalSum(): Int = prefixSum(n)

    fun indexAtY(y: Double): Int {
        if (n == 0) return 0
        val target = max(0, y.toInt())
        var idx = 0
        var bit = highestPowerOfTwo(n)
        var sum = 0
        while (bit != 0) {
            val next = idx + bit
            if (next <= n && sum + tree[next] <= target) {
                idx = next
                sum += tree[next]
            }
            bit = bit shr 1
        }
        return idx.coerceIn(0, n - 1)
    }

    private fun highestPowerOfTwo(x: Int): Int {
        var v = 1
        while (v < x) v = v shl 1
        return v
    }
}
