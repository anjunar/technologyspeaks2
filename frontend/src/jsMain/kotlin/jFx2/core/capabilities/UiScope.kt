package jFx2.core.capabilities

import jFx2.core.Component
import jFx2.core.JFxDsl
import jFx2.core.rendering.RenderScope
import jFx2.forms.FormRegistryScope
import jFx2.forms.FormScope
import org.w3c.dom.Element
import org.w3c.dom.Node

@JFxDsl
class UiScope internal constructor(
    val dom: DomScope,
    val build: BuildScope,
    val render: RenderScope,
    val dispose: DisposeScope,

    val formScope: FormScope? = null,
    val formRegistry: FormRegistryScope? = null
) {
    fun attach(parent: Node, child: Component<*>) = dom.attach(parent, child.node)
    fun <E: Element> create(tag: String): E = dom.create(tag)
}