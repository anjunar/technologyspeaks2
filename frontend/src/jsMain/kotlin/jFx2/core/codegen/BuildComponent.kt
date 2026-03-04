package jFx2.core.codegen

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.DomInsertPoint
import jFx2.core.dom.ElementInsertPoint
import org.w3c.dom.Element
import org.w3c.dom.Node

context(scope: NodeScope)
inline fun <E : Element, C : Component<E>> buildComponent(
    tag: String,
    classes: Array<String> = emptyArray(),
    crossinline create: (E) -> C,
    crossinline block: context(NodeScope) C.() -> Unit = {},
    insertPointFactory: (Node) -> DomInsertPoint = { ElementInsertPoint(it) },
    afterBuild: AfterBuildMode = AfterBuildMode.NONE,
    crossinline afterBuildCall: context(NodeScope) C.() -> Unit = {}
): C {
    val el = scope.create<E>(tag)
    for (cls in classes) {
        val c = cls.trim()
        if (c.isNotEmpty()) el.classList.add(c)
    }

    val component = create(el)
    scope.attach(component)

    val childScope = scope.fork(
        parent = component.node,
        owner = component,
        ctx = scope.ctx,
        insertPoint = insertPointFactory(component.node)
    )

    block(childScope, component)

    when (afterBuild) {
        AfterBuildMode.NONE -> Unit
        AfterBuildMode.EAGER -> afterBuildCall(childScope, component)
        AfterBuildMode.SCHEDULED -> scope.ui.build.afterBuild { afterBuildCall(childScope, component) }
    }

    return component
}
