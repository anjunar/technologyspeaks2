package jFx2.virtual

import jFx2.state.Disposable
import jFx2.state.ListChange

/**
 * Optional capability for [RangeDataProvider]s which can signal when their backing data changes.
 *
 * This allows [VirtualListView] to re-render (and if needed reset measurements) without requiring
 * a scroll event.
 */
interface ObservableRangeDataProvider {
    fun observeChanges(listener: (ListChange<*>) -> Unit): Disposable
}

