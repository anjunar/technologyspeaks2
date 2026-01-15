package jFx2.core.capabilities

import jFx2.core.Component
import jFx2.core.Ctx
import jFx2.state.Disposable
import org.w3c.dom.Element
import org.w3c.dom.Node
import kotlinx.browser.document

class DomScope {
    fun <E : Element> create(tag: String): E = document.createElement(tag).unsafeCast<E>()
    fun attach(parent: Node, child: Node) { parent.appendChild(child) }
    fun detach(node: Node) { node.parentNode?.removeChild(node) }
    fun clear(node: Node) { while (node.firstChild != null) node.removeChild(node.firstChild!!) }
}

class BuildScope {
    private val after = ArrayList<() -> Unit>()
    fun afterBuild(fn: () -> Unit) { after.add(fn) }
    fun flush() {
        if (after.isEmpty()) return
        val copy = after.toList()
        after.clear()
        for (f in copy) f()
    }
}

class UiScope(
    val dom: DomScope = DomScope(),
    val build: BuildScope = BuildScope(),
    val dispose: DisposeScope = DisposeScope()
)

class NodeScope(
    val ui: UiScope,
    val parent: Node,
    val owner: Component<*>,
    val ctx: Ctx,
    val dispose: DisposeScope
) {
    fun <E : Element> create(tag: String): E = ui.dom.create(tag)
    fun attach(child: Component<*>) {
        owner.addChild(child)
        ui.dom.attach(parent, child.node)

        dispose.register {
            runCatching { child.dispose() }

            ui.dom.detach(child.node)

            owner.removeChild(child)
        }
    }}

