package jFx.core

import kotlinx.browser.window

class BuildContext internal constructor() {

    val stack: ArrayDeque<ElementBuilder<*>> = ArrayDeque()
    val afterTreeBuilt: MutableList<() -> Unit> = mutableListOf()
    val dirtyComponents: MutableSet<ElementBuilder<*>> = mutableSetOf()

    var scope: Scope = Scope()

    private var flushScheduled: Boolean = false
    private var pendingInvalidate: Boolean = false

    fun push(builder: ElementBuilder<*>) {
        stack.addLast(builder)
    }

    fun pop(builder: ElementBuilder<*>) {
        val last = stack.removeLastOrNull()
        check(last === builder) {
            "BuildContext stack corrupted: expected to pop $builder but was $last"
        }
    }

    fun addDirtyComponent(component: ElementBuilder<*>) {
        dirtyComponents.add(component)
        // Optional: wenn du beim Registrieren sofort initial flush willst:
        // invalidate()
    }

    fun flushDirty() {
        val comps = dirtyComponents.toList()
        comps.forEach { c ->
            c.dirtyValues.forEach { it() }
        }
    }

    fun invalidate() {
        if (!isIdle()) {
            pendingInvalidate = true
            return
        }
        scheduleFlushIfNeeded()
    }

    private fun scheduleFlushIfNeeded() {
        if (flushScheduled) return
        flushScheduled = true

        window.requestAnimationFrame {
            flushScheduled = false
            flushDirty()
        }
    }

    fun current(): ElementBuilder<*>? = stack.lastOrNull()

    fun parent(): ElementBuilder<*>? =
        if (stack.size >= 2) stack.elementAt(stack.size - 2) else null

    fun root(): ElementBuilder<*>? = stack.firstOrNull()

    internal fun isIdle(): Boolean = stack.isEmpty()

    fun afterTreeBuilt(action: () -> Unit) {
        afterTreeBuilt.add(action)
    }

    internal fun flushAfterTreeBuilt() {
        val actions = afterTreeBuilt.toList()
        afterTreeBuilt.clear()
        actions.forEach { it() }

        if (pendingInvalidate) {
            pendingInvalidate = false
            scheduleFlushIfNeeded()
        }
    }
}
