@file:Suppress("unused")

package core

import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.Node
import state.Disposable

@DslMarker
annotation class FxDsl

interface HasNode<N : Node> {
    val node: N
}

open class Component<E : Element>(
    final override val node: E
) : HasNode<E> {

    open fun build() {}
    open fun apply() {}
    open fun hook() {}
}

@FxDsl
class UiScope internal constructor(
    internal val parent: Element
) {
    fun <C : HasNode<out Node>> attach(child: C): C {
        parent.appendChild(child.node)
        return child
    }

    internal fun <C : Component<out Element>> mount(
        child: C,
        block: (UiScope.() -> Unit)?
    ): C {
        child.build()
        parent.appendChild(child.node)
        if (block != null) UiScope(child.node).block()
        child.apply()
        child.hook()
        return child
    }
}


fun mountInto(parent: Element, block: UiScope.() -> Unit): UiScope =
    UiScope(parent).apply(block)

inline fun <reified E : Element> create(tag: String): E =
    document.createElement(tag) as E

private val bindingsKey = "__fx_bindings__"

private fun Element.ensureBindings(): MutableList<Disposable> {
    val dyn = this.asDynamic()
    val existing = dyn[bindingsKey]
    if (existing != null) return existing.unsafeCast<MutableList<Disposable>>()
    val list = mutableListOf<Disposable>()
    dyn[bindingsKey] = list
    return list
}

private fun Element.getBindingsOrNull(): MutableList<Disposable>? {
    val dyn = this.asDynamic()
    val existing = dyn[bindingsKey]
    return existing?.unsafeCast<MutableList<Disposable>>()
}

fun Element.disposeBindings() {
    val list = getBindingsOrNull() ?: return
    val snapshot = list.toList()
    list.clear()
    snapshot.forEach { it() }
}

fun disposeSubtree(node: Node) {
    val children = node.childNodes
    for (i in 0 until children.length) {
        val child = children.item(i) ?: continue
        disposeSubtree(child)
    }

    if (node.nodeType == Node.ELEMENT_NODE.toShort()) {
        val el = node.unsafeCast<Element>()
        el.disposeBindings()
    }
}

fun Component<out Element>.track(disposable: Disposable) {
    node.ensureBindings().add(disposable)
}