@file:OptIn(ExperimentalUuidApi::class)

package jFx2.modals

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.renderComponent
import jFx2.core.dsl.renderFields
import jFx2.core.dsl.style
import jFx2.core.dsl.subscribeBidirectional
import jFx2.core.rendering.foreach
import jFx2.core.template
import jFx2.layout.div
import jFx2.router.PageInfo
import jFx2.state.ListProperty
import jFx2.state.Property
import kotlinx.browser.window
import org.w3c.dom.HTMLDivElement
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class WindowConf(
    val title : String,
    val component : context(NodeScope) () -> Component<*>,
    val zIndex : Property<Int> = Property(0),
    val onClose: ((Window) -> Unit)? = null,
    val onClick: ((Window) -> Unit)? = null,
    val maximized: Property<Boolean> = Property(false),
    val resizable: Boolean = false
) {
    val id : String = Uuid.generateV4().toString()
}

class ViewPort(override val node: HTMLDivElement) : Component<HTMLDivElement>() {

    context(scope: NodeScope)
    fun afterBuild() {

        template {
            renderFields(*this@ViewPort.children.toTypedArray())

            foreach(windows, { key -> key.id }) { window, index ->

                window {

                    subscribeBidirectional(window.zIndex, zIndex)
                    subscribeBidirectional(window.maximized, maximized)

                    title = window.title
                    resizeable = window.resizable

                    onCloseWindow {
                        closeWindow(window)
                    }

                    onClickWindow {
                        window.onClick?.invoke(it)
                        touchWindow(window)
                    }

                    div {
                        style {
                            width = "100%"
                            height = "100%"
                        }
                        val field = window.component()

                        if (field is PageInfo) {
                            (field as PageInfo).close = { closeWindowById(window.id) }
                        }

                        renderComponent(field)
                    }
                }
            }
        }


    }

    companion object {
        val windows = ListProperty<WindowConf>()

        fun isActive(conf: WindowConf) = windows.all { (it == conf) || (it.zIndex.get() < conf.zIndex.get()) }

        fun touchWindow(conf: WindowConf) {
            var index = 0
            windows.forEach { window -> window.zIndex.set(index++) }
            conf.zIndex.set(index)
            conf.maximized.set(true)
        }

        fun addWindow(conf: WindowConf) {
            windows.add(conf)
            var index = 0
            windows.forEach { window -> window.zIndex.set(index++) }
            conf.zIndex.set(index)
        }

        fun closeWindow(conf: WindowConf) {
            windows.remove(conf)
        }

        fun closeWindowById(id: String) {
            windows.firstOrNull { it.id == id }?.let {
                windows.remove(it)
            }
        }
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
