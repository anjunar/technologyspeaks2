package jFx2.modals

import jFx2.controls.Div
import jFx2.controls.button
import jFx2.controls.div
import jFx2.controls.span
import jFx2.controls.text
import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.capabilities.UiScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.className
import jFx2.core.dsl.mousedown
import jFx2.core.dsl.renderField
import jFx2.core.dsl.renderFields
import jFx2.core.rendering.condition
import jFx2.core.runtime.component
import jFx2.core.template
import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import kotlin.js.unsafeCast

class Window(
    override val node: HTMLDivElement,
    val ui: NodeScope
) : Component<HTMLDivElement>() {

    var maximizable = false
    var draggable = true
    var resizeable = true

    private var onClose: (() -> Unit)? = null

    fun onClose(block: () -> Unit) { onClose = block }

    val dragElementMouseDown: (MouseEvent) -> Unit = { event ->
        val element = node as HTMLElement

        var deltaX = 0
        var deltaY = 0
        var pointerX = 0
        var pointerY = 0

        val elementDrag: (Event) -> Unit = { ev ->
            val e = ev.unsafeCast<MouseEvent>()
            e.preventDefault()

            deltaX = pointerX - e.clientX
            deltaY = pointerY - e.clientY
            pointerX = e.clientX
            pointerY = e.clientY

            var top = element.offsetTop - deltaY
            if (top < 0) top = 0
            val left = element.offsetLeft - deltaX

            element.style.top = "${top}px"
            element.style.left = "${left}px"
        }

        lateinit var closeDragElement: (Event) -> Unit
        closeDragElement = {
            document.removeEventListener("mouseup", closeDragElement)
            document.removeEventListener("mousemove", elementDrag)
        }

        if (!maximizable && draggable) {
            event.preventDefault()
            pointerX = event.clientX
            pointerY = event.clientY
            document.addEventListener("mouseup", closeDragElement)
            document.addEventListener("mousemove", elementDrag)
        }
    }

    val nResizeMouseDown: (MouseEvent) -> Unit = { event ->
        val element = node as HTMLElement
        var delta = element.offsetTop
        var pointer = element.offsetTop

        val elementDrag: (Event) -> Unit = { ev ->
            val e = ev.unsafeCast<MouseEvent>()
            e.preventDefault()

            delta = pointer - e.clientY
            pointer = e.clientY
            element.style.height = "${element.offsetHeight - 2 + delta}px"
            element.style.top = "${element.offsetTop - delta}px"
        }

        lateinit var closeDragElement: (Event) -> Unit
        closeDragElement = {
            document.removeEventListener("mouseup", closeDragElement)
            document.removeEventListener("mousemove", elementDrag)
        }

        if (resizeable && !maximizable) {
            event.preventDefault()
            pointer = event.clientY
            document.addEventListener("mouseup", closeDragElement)
            document.addEventListener("mousemove", elementDrag)
        }
    }

    val nwResizeMouseDown: (MouseEvent) -> Unit = { event ->
        val element = node as HTMLElement
        var deltaY = element.offsetTop
        var pointerY = element.offsetTop
        var deltaX = element.offsetLeft
        var pointerX = element.offsetLeft

        val elementDrag: (Event) -> Unit = { ev ->
            val e = ev.unsafeCast<MouseEvent>()
            e.preventDefault()

            deltaY = pointerY - e.clientY
            pointerY = e.clientY
            element.style.height = "${element.offsetHeight - 2 + deltaY}px"
            element.style.top = "${element.offsetTop - deltaY}px"

            deltaX = pointerX - e.clientX
            pointerX = e.clientX
            element.style.width = "${element.offsetWidth - 2 + deltaX}px"
            element.style.left = "${element.offsetLeft - deltaX}px"
        }

        lateinit var closeDragElement: (Event) -> Unit
        closeDragElement = {
            document.removeEventListener("mouseup", closeDragElement)
            document.removeEventListener("mousemove", elementDrag)
        }

        if (resizeable && !maximizable) {
            event.preventDefault()
            pointerY = event.clientY
            pointerX = event.clientX
            document.addEventListener("mouseup", closeDragElement)
            document.addEventListener("mousemove", elementDrag)
        }
    }

    val wResizeMouseDown: (MouseEvent) -> Unit = { event ->
        val element = node as HTMLElement
        var delta = element.offsetLeft
        var pointer = element.offsetLeft

        val elementDrag: (Event) -> Unit = { ev ->
            val e = ev.unsafeCast<MouseEvent>()
            e.preventDefault()

            delta = pointer - e.clientX
            pointer = e.clientX
            element.style.width = "${element.offsetWidth - 2 + delta}px"
            element.style.left = "${element.offsetLeft - delta}px"
        }

        lateinit var closeDragElement: (Event) -> Unit
        closeDragElement = {
            document.removeEventListener("mouseup", closeDragElement)
            document.removeEventListener("mousemove", elementDrag)
        }

        if (resizeable && !maximizable) {
            event.preventDefault()
            pointer = event.clientX
            document.addEventListener("mouseup", closeDragElement)
            document.addEventListener("mousemove", elementDrag)
        }
    }

    val swResizeMouseDown: (MouseEvent) -> Unit = { event ->
        val element = node as HTMLElement
        var deltaY = element.offsetTop
        var pointerY = element.offsetTop
        var deltaX = element.offsetLeft
        var pointerX = element.offsetLeft

        val elementDrag: (Event) -> Unit = { ev ->
            val e = ev.unsafeCast<MouseEvent>()
            e.preventDefault()

            deltaY = pointerY - e.clientY
            pointerY = e.clientY
            element.style.height = "${element.offsetHeight - 2 - deltaY}px"
            element.style.bottom =
                "${element.offsetTop + (element.offsetHeight - 2) - deltaY}px"

            deltaX = pointerX - e.clientX
            pointerX = e.clientX
            element.style.width = "${element.offsetWidth - 2 + deltaX}px"
            element.style.left = "${element.offsetLeft - deltaX}px"
        }

        lateinit var closeDragElement: (Event) -> Unit
        closeDragElement = {
            document.removeEventListener("mouseup", closeDragElement)
            document.removeEventListener("mousemove", elementDrag)
        }

        if (resizeable && !maximizable) {
            event.preventDefault()
            pointerY = event.clientY
            pointerX = event.clientX
            document.addEventListener("mouseup", closeDragElement)
            document.addEventListener("mousemove", elementDrag)
        }
    }

    val neResizeMouseDown: (MouseEvent) -> Unit = { event ->
        val element = node as HTMLElement
        var deltaY = element.offsetTop
        var pointerY = element.offsetTop
        var deltaX = element.offsetLeft
        var pointerX = element.offsetLeft

        val elementDrag: (Event) -> Unit = { ev ->
            val e = ev.unsafeCast<MouseEvent>()
            e.preventDefault()

            deltaY = pointerY - e.clientY
            pointerY = e.clientY
            element.style.height = "${element.offsetHeight - 2 + deltaY}px"
            element.style.top = "${element.offsetTop - deltaY}px"

            deltaX = pointerX - e.clientX
            pointerX = e.clientX
            element.style.width = "${element.offsetWidth - 2 - deltaX}px"
            element.style.right =
                "${element.offsetLeft + (element.offsetWidth - 2) - deltaX}px"
        }

        lateinit var closeDragElement: (Event) -> Unit
        closeDragElement = {
            document.removeEventListener("mouseup", closeDragElement)
            document.removeEventListener("mousemove", elementDrag)
        }

        if (resizeable && !maximizable) {
            event.preventDefault()
            pointerY = event.clientY
            pointerX = event.clientX
            document.addEventListener("mouseup", closeDragElement)
            document.addEventListener("mousemove", elementDrag)
        }
    }

    val eResizeMouseDown: (MouseEvent) -> Unit = { event ->
        val element = node as HTMLElement
        var delta = element.offsetLeft
        var pointer = element.offsetLeft

        val elementDrag: (Event) -> Unit = { ev ->
            val e = ev.unsafeCast<MouseEvent>()
            e.preventDefault()

            delta = pointer - e.clientX
            pointer = e.clientX
            element.style.width = "${element.offsetWidth - 2 - delta}px"
            element.style.right =
                "${element.offsetLeft + (element.offsetWidth - 2) - delta}px"
        }

        lateinit var closeDragElement: (Event) -> Unit
        closeDragElement = {
            document.removeEventListener("mouseup", closeDragElement)
            document.removeEventListener("mousemove", elementDrag)
        }

        if (resizeable && !maximizable) {
            event.preventDefault()
            pointer = event.clientX
            document.addEventListener("mouseup", closeDragElement)
            document.addEventListener("mousemove", elementDrag)
        }
    }

    val seResizeMouseDown: (MouseEvent) -> Unit = { event ->
        val element = node as HTMLElement
        var deltaY = element.offsetTop
        var pointerY = element.offsetTop
        var deltaX = element.offsetLeft
        var pointerX = element.offsetLeft

        val elementDrag: (Event) -> Unit = { ev ->
            val e = ev.unsafeCast<MouseEvent>()
            e.preventDefault()

            deltaY = pointerY - e.clientY
            pointerY = e.clientY
            element.style.height = "${element.offsetHeight - 2 - deltaY}px"
            element.style.bottom =
                "${element.offsetTop + element.offsetHeight - deltaY}px"

            deltaX = pointerX - e.clientX
            pointerX = e.clientX
            element.style.width = "${element.offsetWidth - 2 - deltaX}px"
            element.style.right =
                "${element.offsetLeft + (element.offsetWidth - 2) - deltaX}px"
        }

        lateinit var closeDragElement: (Event) -> Unit
        closeDragElement = {
            document.removeEventListener("mouseup", closeDragElement)
            document.removeEventListener("mousemove", elementDrag)
        }

        if (resizeable && !maximizable) {
            event.preventDefault()
            pointerY = event.clientY
            pointerX = event.clientX
            document.addEventListener("mouseup", closeDragElement)
            document.addEventListener("mousemove", elementDrag)
        }
    }

    val sResizeMouseDown: (MouseEvent) -> Unit = { event ->
        val element = node as HTMLElement
        var delta = element.offsetTop
        var pointer = element.offsetTop

        val elementDrag: (Event) -> Unit = { ev ->
            val e = ev.unsafeCast<MouseEvent>()
            e.preventDefault()

            delta = pointer - e.clientY
            pointer = e.clientY
            element.style.height = "${element.offsetHeight - 2 - delta}px"
            element.style.bottom =
                "${element.offsetTop + (element.offsetHeight - 2) - delta}px"
        }

        lateinit var closeDragElement: (Event) -> Unit
        closeDragElement = {
            document.removeEventListener("mouseup", closeDragElement)
            document.removeEventListener("mousemove", elementDrag)
        }

        if (resizeable && !maximizable) {
            event.preventDefault()
            pointer = event.clientY
            document.addEventListener("mouseup", closeDragElement)
            document.addEventListener("mousemove", elementDrag)
        }
    }

    context(scope: NodeScope)
    fun afterBuild() {
        template {
            div {
                className { "header" }
                mousedown { e -> dragElementMouseDown(e) }

                span {
                    className { "title" }
                    text { "Header name" }
                }

                condition({this@Window.onClose != null}) {
                    then {
                        button("close") {
                            className { "material-icons" }
                            onClick {
                                this@Window.ui.owner.removeChild(this@Window)
                                this@Window.ui.ui.dom.detach(this@Window.node)
                            }
                        }
                    }
                }

            }

            div {
                className { "container" }
                val components = this@Window.children.toTypedArray()
                renderFields(*components)
            }

            div { className { "se" }; mousedown { e -> seResizeMouseDown(e) } }
            div { className { "sw" }; mousedown { e -> swResizeMouseDown(e) } }
            div { className { "nw" }; mousedown { e -> nwResizeMouseDown(e) } }
            div { className { "ne" }; mousedown { e -> neResizeMouseDown(e) } }
            div { className { "n"  }; mousedown { e -> nResizeMouseDown(e)  } }
            div { className { "s"  }; mousedown { e -> sResizeMouseDown(e)  } }
            div { className { "w"  }; mousedown { e -> wResizeMouseDown(e)  } }
            div { className { "e"  }; mousedown { e -> eResizeMouseDown(e)  } }
        }
    }
}

context(scope: NodeScope)
fun window(block: context(NodeScope) Window.() -> Unit = {}): Window {
    val el = scope.create<HTMLDivElement>("div")
    el.classList.add("window")
    val c = Window(el, scope)
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
