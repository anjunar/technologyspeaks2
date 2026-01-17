package jFx2.core

import jFx2.core.capabilities.NodeScope
import org.w3c.dom.Element

context(scope: NodeScope)
fun template(block: context(NodeScope) () -> Unit) {
    val owner = object : Component<Element>() {
        override val node: Element = scope.parent.unsafeCast<Element>()
    }
    val s = scope.fork(owner = owner)
    with(s) { block() }
}