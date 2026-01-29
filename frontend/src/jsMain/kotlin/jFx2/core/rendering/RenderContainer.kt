package jFx2.core.rendering

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dsl.renderFields
import org.w3c.dom.HTMLDivElement

class RenderContainer(override val node: HTMLDivElement) : Component<HTMLDivElement>() {

    context(scope: NodeScope)
    fun afterBuild() {
        renderFields(*this@RenderContainer.children.toTypedArray())
    }
}

internal fun attachRenderContainer(scope: NodeScope): RenderContainer {
    val el = scope.create<HTMLDivElement>("div")
    val container = RenderContainer(el)
    scope.attach(container)

    if (container.node.parentNode !== scope.insertPoint.parent) {
        scope.insertPoint.insert(container.node)
    }

    scope.dispose.register {
        container.node.parentNode?.removeChild(container.node)
    }

    return container
}
