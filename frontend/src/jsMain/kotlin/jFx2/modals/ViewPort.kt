package jFx2.modals

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.renderComponent
import jFx2.core.dsl.renderFields
import jFx2.core.dsl.style
import jFx2.core.rendering.foreach
import jFx2.core.template
import jFx2.layout.div
import jFx2.state.ListProperty
import org.w3c.dom.HTMLDivElement

class WindowConf(val id : String, val title : String, val component : Component<*>)

class ViewPort(override val node: HTMLDivElement) : Component<HTMLDivElement>() {

    context(scope: NodeScope)
    fun afterBuild() {

        template {
            renderFields(*this@ViewPort.children.toTypedArray())

            foreach(windows, { key -> key.id }) { window, index ->
                window {

                    onClose {
                        windows.remove(window)
                    }

                    div {
                        style {
                            width = "100%"
                            height = "100%"
                        }
                        renderComponent(window.component)
                    }
                }
            }
        }


    }

    companion object {
        val windows = ListProperty<WindowConf>()
    }

}

context(scope: NodeScope)
fun viewport(block: context(NodeScope) ViewPort.() -> Unit = {}): ViewPort {
    val el = scope.create<HTMLDivElement>("div")
    el.classList.add("viewport")
    val c = ViewPort(el)
    scope.attach(c)

    val childScope = scope.fork(
        parent = c.node,
        owner = c,
        ctx = scope.ctx,
        insertPoint = ElementInsertPoint(c.node)
    )

    scope.ui.build.afterBuild {
        with(childScope) {
            c.afterBuild()
        }
    }

    block(childScope, c)

    return c
}
