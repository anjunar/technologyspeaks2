package jFx2.core.capabilities

import jFx2.core.Component
import jFx2.core.JFxDsl
import org.w3c.dom.Element
import org.w3c.dom.Node

@JFxDsl
class NodeScope internal constructor(
    val ui: UiScope,
    val parent: Node
) {
    fun attach(child: Component<*>): Component<*> {
        ui.dom.attach(parent, child.node)

        if (child is HasUi) {
            child.ui = ui
        }

        return child
    }

    fun <E : Element> create(tag: String): E = ui.dom.create(tag)

    val build get() = ui.build
    val dispose get() = ui.dispose
    val render get() = ui.render
    val dom get() = ui.dom
    val formScope get() = ui.formScope
    val formRegistry get() = ui.formRegistry
}
