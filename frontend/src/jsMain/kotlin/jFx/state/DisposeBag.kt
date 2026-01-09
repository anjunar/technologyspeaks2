package jFx.state

typealias Disposable = () -> Unit

class DisposeBag {

    private var disposed = false
    private val items = mutableListOf<Disposable>()

    fun add(d: Disposable) {
        if (disposed) {
            d()
        } else {
            items += d
        }
    }

    fun dispose() {
        if (disposed) return
        disposed = true

        val snapshot = items.toList()
        items.clear()

        for (i in snapshot.indices.reversed()) {
            snapshot[i]()
        }
    }
}
