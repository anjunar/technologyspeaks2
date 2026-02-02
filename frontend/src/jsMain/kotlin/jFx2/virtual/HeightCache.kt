package jFx2.virtual

import kotlin.math.max

class HeightCache(
    private val estimateHeightPx: Int
) {
    private var heights: IntArray = IntArray(0)
    private var measured: BooleanArray = BooleanArray(0)

    val size: Int get() = heights.size

    fun ensureCount(n: Int) {
        if (n <= heights.size) return
        val old = heights.size
        heights = heights.copyOf(n)
        measured = measured.copyOf(n)
        for (i in old until n) {
            heights[i] = estimateHeightPx
        }
    }

    fun trimTo(n: Int) {
        if (n >= heights.size) return
        heights = heights.copyOf(n)
        measured = measured.copyOf(n)
    }

    fun applyMeasuredHeight(index: Int, px: Int): Int {
        if (index < 0 || index >= heights.size) return 0
        val newH = max(1, px)
        val old = heights[index]
        if (old == newH && measured[index]) return 0
        heights[index] = newH
        measured[index] = true
        return newH - old
    }

    fun heightOf(index: Int): Int =
        if (index < 0 || index >= heights.size) estimateHeightPx else heights[index]

    fun heightsSnapshot(): IntArray = heights.copyOf()
}
