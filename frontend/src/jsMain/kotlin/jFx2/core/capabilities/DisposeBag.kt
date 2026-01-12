package jFx2.core.capabilities

typealias Disposable = () -> Unit

class DisposeBag {
    private val items = ArrayList<Disposable>()
    fun add(d: Disposable) { items += d }
    fun dispose() {
        // dispose in reverse order is often safer
        for (i in items.size - 1 downTo 0) items[i].invoke()
        items.clear()
    }
}

interface DisposeScope {
    fun register(disposable: Disposable)
}