package jFx.state

class ListProperty<T>(
    initial: List<T> = emptyList()
) {

    sealed class Change<T> {
        data class Add<T>(val index: Int, val items: List<T>) : Change<T>()
        data class Remove<T>(val index: Int, val items: List<T>) : Change<T>()
        data class Replace<T>(val index: Int, val removed: List<T>, val added: List<T>) : Change<T>()
        data class Move<T>(val fromIndex: Int, val toIndex: Int, val item: T) : Change<T>()
        data class Reset<T>(val newValue: List<T>, val oldValue: List<T>) : Change<T>()
    }

    private var value: MutableList<T> = initial.toMutableList()

    private val changeListeners = LinkedHashMap<Int, (Change<T>) -> Unit>()
    private val valueListeners = LinkedHashMap<Int, (List<T>) -> Unit>()
    private var nextId = 1

    fun get(): List<T> = value

    operator fun get(index: Int): T = value[index]

    val size: Int get() = value.size
    fun isEmpty(): Boolean = value.isEmpty()

    fun observeChanges(
        fireInitial: Boolean = true,
        listener: (Change<T>) -> Unit
    ): Disposable {
        val id = nextId++
        changeListeners[id] = listener
        if (fireInitial) {
            listener(Change.Reset(newValue = value.toList(), oldValue = emptyList()))
        }
        return { changeListeners.remove(id) }
    }

    fun observe(
        fireInitial: Boolean = true,
        listener: (List<T>) -> Unit
    ): Disposable {
        val id = nextId++
        valueListeners[id] = listener
        if (fireInitial) listener(value.toList())
        return { valueListeners.remove(id) }
    }

    fun add(item: T) {
        val index = value.size
        value.add(item)
        emit(Change.Add(index, listOf(item)))
    }

    fun addAll(items: List<T>) {
        if (items.isEmpty()) return
        val index = value.size
        value.addAll(items)
        emit(Change.Add(index, items.toList()))
    }

    fun insert(index: Int, item: T) {
        value.add(index, item)
        emit(Change.Add(index, listOf(item)))
    }

    fun remove(item: T): Boolean {
        val index = value.indexOf(item)
        if (index < 0) return false
        val removed = value.removeAt(index)
        emit(Change.Remove(index, listOf(removed)))
        return true
    }

    fun removeAt(index: Int): T {
        val removed = value.removeAt(index)
        emit(Change.Remove(index, listOf(removed)))
        return removed
    }

    fun clear() {
        if (value.isEmpty()) return
        val old = value.toList()
        value.clear()
        emit(Change.Reset(newValue = emptyList(), oldValue = old))
    }

    fun set(index: Int, item: T) {
        val old = value[index]
        if (old == item) return
        value[index] = item
        emit(Change.Replace(index, removed = listOf(old), added = listOf(item)))
    }

    fun setAll(items: List<T>) {
        val old = value.toList()
        if (old == items) return
        value = items.toMutableList()
        emit(Change.Reset(newValue = value.toList(), oldValue = old))
    }

    fun move(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        val item = value.removeAt(fromIndex)
        value.add(toIndex, item)
        emit(Change.Move(fromIndex, toIndex, item))
    }

    fun mutate(block: MutableList<T>.() -> Unit) {
        val old = value.toList()
        value.block()
        val new = value.toList()
        if (old != new) emit(Change.Reset(newValue = new, oldValue = old))
    }

    fun subscribe(source: ListProperty<T>): Disposable {
        setAll(source.get())
        return source.observeChanges(fireInitial = false) { change ->
            when (change) {
                is Change.Add -> {
                    value.addAll(change.index, change.items)
                    emit(change)
                }
                is Change.Remove -> {
                    repeat(change.items.size) { value.removeAt(change.index) }
                    emit(change)
                }
                is Change.Replace -> {
                    for (i in change.removed.indices) {
                        value[change.index + i] = change.added[i]
                    }
                    emit(change)
                }
                is Change.Move -> {
                    move(change.fromIndex, change.toIndex)
                }
                is Change.Reset -> {
                    setAll(change.newValue)
                }
            }
        }
    }

    private fun emit(change: Change<T>) {
        val changeSnapshot = changeListeners.values.toList()
        val valueSnapshot = valueListeners.values.toList()

        changeSnapshot.forEach { it(change) }
        if (valueSnapshot.isNotEmpty()) {
            val snapshotValue = value.toList()
            valueSnapshot.forEach { it(snapshotValue) }
        }
    }
}
