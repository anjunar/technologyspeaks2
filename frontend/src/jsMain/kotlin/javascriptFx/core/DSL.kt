@file:Suppress("unused")

package javascriptFx.core

import javascriptFx.controls.Button
import javascriptFx.state.Disposable
import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.Node

@DslMarker
annotation class FxDsl

interface HasNode<N : Node> {
    val node: N
}

open class Component<E : Element>(
    final override val node: E
) : HasNode<E> {

    open fun build() {}
    open fun bind() {}
    open fun hook() {}

    open fun onChildrenAttached(children: List<Component<*>>) {}
}

@FxDsl
class UiScope internal constructor(
    internal val parent: Component<*>
) {
    private val mountedChildren = mutableListOf<Component<*>>()

    fun <C : HasNode<out Node>> attach(child: C): C {
        parent.node.appendChild(child.node)

        if (child is Component<*>) {
            mountedChildren += child
        }
        return child
    }

    internal fun <C : Component<out Element>> mount(
        child: C,
        block: (UiScope.() -> Unit)?
    ): C {
        child.build()

        parent.node.appendChild(child.node)
        mountedChildren += child

        if (block != null) UiScope(child).block()

        child.bind()
        child.hook()
        return child
    }

    internal fun finalize() {
        parent.onChildrenAttached(mountedChildren)
    }}

fun <E : Component<*>> E.children(block: UiScope.() -> Unit): E {
    val scope = UiScope(this)
    scope.block()
    scope.finalize()
    return this
}

fun mountInto(parent: Component<*>, block: UiScope.() -> Unit): UiScope =
    UiScope(parent).apply(block)

inline fun <reified E : Element> create(tag: String): E = document.createElement(tag) as E

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