package jFx2.modals

import jFx2.controls.button
import jFx2.layout.div
import jFx2.controls.span
import jFx2.controls.text
import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.className
import jFx2.core.dsl.mousedown
import jFx2.core.dsl.renderFields
import jFx2.core.rendering.condition
import jFx2.core.template
import kotlinx.browser.document
import kotlinx.browser.window as browserWindow
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import kotlin.js.unsafeCast
import kotlin.math.max
import kotlin.math.roundToInt

class Window(
    override val node: HTMLDivElement,
    val ui: NodeScope
) : Component<HTMLDivElement>() {

    var maximizable = false
    var draggable = true
    var resizeable = true

    var title : String = ""

    var centerOnOpen: Boolean = true
    private var didAutoCenter: Boolean = false

    var rememberPosition: Boolean = true
    var positionStorageKey: String? = null

    var rememberSize: Boolean = true

    private var onClose: (() -> Unit)? = null

    fun onClose(block: () -> Unit) { onClose = block }

    private fun resolvedPositionStorageKey(): String? {
        if (!rememberPosition) return null
        val raw = positionStorageKey?.trim().takeIf { !it.isNullOrBlank() }
            ?: title.trim().takeIf { it.isNotBlank() }
            ?: return null
        return "jFx2.window.position:$raw"
    }

    private fun resolvedSizeStorageKey(): String? {
        if (!rememberSize) return null
        val raw = positionStorageKey?.trim().takeIf { !it.isNullOrBlank() }
            ?: title.trim().takeIf { it.isNotBlank() }
            ?: return null
        return "jFx2.window.size:$raw"
    }

    private fun setLeftTopPx(left: Double, top: Double) {
        val element = node as HTMLElement
        element.style.left = "${left.roundToInt()}px"
        element.style.top = "${top.roundToInt()}px"
        element.style.right = ""
        element.style.bottom = ""
    }

    fun restoreSizeFromStorage(force: Boolean = false): Boolean {
        val key = resolvedSizeStorageKey() ?: return false
        val element = node as HTMLElement

        val raw = runCatching { browserWindow.localStorage?.getItem(key) }.getOrNull()?.trim()
        if (raw.isNullOrBlank()) return false

        val parts = raw.split(',')
        if (parts.size != 2) return false

        val storedWidth = parts[0].trim().toIntOrNull()?.takeIf { it > 0 }
        val storedHeight = parts[1].trim().toIntOrNull()?.takeIf { it > 0 }
        if (storedWidth == null && storedHeight == null) return false

        var applied = false

        if (storedWidth != null && (force || element.style.width.isBlank())) {
            element.style.width = "${storedWidth}px"
            applied = true
        }

        if (storedHeight != null && (force || element.style.height.isBlank())) {
            element.style.height = "${storedHeight}px"
            applied = true
        }

        return applied
    }

    fun restorePositionFromStorage(force: Boolean = false): Boolean {
        val key = resolvedPositionStorageKey() ?: return false

        val element = node as HTMLElement
        if (!force) {
            // Don't override explicitly set inline styles.
            if (element.style.left.isNotBlank() || element.style.top.isNotBlank()) return false
        }

        val raw = runCatching { browserWindow.localStorage?.getItem(key) }.getOrNull()?.trim()
        if (raw.isNullOrBlank()) return false

        val parts = raw.split(',')
        if (parts.size != 2) return false
        val storedLeft = parts[0].trim().toIntOrNull() ?: return false
        val storedTop = parts[1].trim().toIntOrNull() ?: return false

        didAutoCenter = true
        setLeftTopPx(max(0.0, storedLeft.toDouble()), max(0.0, storedTop.toDouble()))

        fun attempt(triesLeft: Int) {
            val width = element.offsetWidth
            val height = element.offsetHeight

            if ((width <= 0 || height <= 0) && triesLeft > 0) {
                browserWindow.requestAnimationFrame { _ -> attempt(triesLeft - 1) }
                return
            }

            val containerWidth = (element.offsetParent as? HTMLElement)?.clientWidth?.takeIf { it > 0 }?.toDouble()
                ?: browserWindow.innerWidth.toDouble()
            val containerHeight = (element.offsetParent as? HTMLElement)?.clientHeight?.takeIf { it > 0 }?.toDouble()
                ?: browserWindow.innerHeight.toDouble()

            val maxLeft = max(0.0, containerWidth - width.toDouble())
            val maxTop = max(0.0, containerHeight - height.toDouble())

            val left = storedLeft.toDouble().coerceIn(0.0, maxLeft)
            val top = storedTop.toDouble().coerceIn(0.0, maxTop)

            setLeftTopPx(left, top)
        }

        browserWindow.requestAnimationFrame { _ -> attempt(5) }
        return true
    }

    private fun persistSizeToStorage() {
        val key = resolvedSizeStorageKey() ?: return
        val element = node as HTMLElement

        fun pxToInt(value: String): Int? {
            val v = value.trim()
            if (v.isBlank()) return null
            if (!v.endsWith("px")) return null
            return v.removeSuffix("px").trim().toIntOrNull()
        }

        val width = pxToInt(element.style.width)
        val height = pxToInt(element.style.height)

        if (width == null && height == null) return

        val payload = "${width ?: ""},${height ?: ""}"
        runCatching { browserWindow.localStorage?.setItem(key, payload) }
    }

    private fun persistPositionToStorage() {
        val key = resolvedPositionStorageKey() ?: return
        val element = node as HTMLElement
        val left = element.offsetLeft
        val top = element.offsetTop
        runCatching { browserWindow.localStorage?.setItem(key, "${left},${top}") }
    }

    private fun persistWindowStateToStorage() {
        persistPositionToStorage()
        persistSizeToStorage()
    }

    fun centerInViewport(force: Boolean = false) {
        if (!force) {
            if (!centerOnOpen || didAutoCenter) return

            val element = node as HTMLElement
            // If user code already positioned the window (inline style), don't override it.
            if (element.style.left.isNotBlank() || element.style.top.isNotBlank()) return
        }

        val element = node as HTMLElement

        fun attempt(triesLeft: Int) {
            val width = element.offsetWidth
            val height = element.offsetHeight

            if ((width <= 0 || height <= 0) && triesLeft > 0) {
                browserWindow.requestAnimationFrame { _ -> attempt(triesLeft - 1) }
                return
            }

            val left = max(0.0, browserWindow.scrollX + (browserWindow.innerWidth - width) / 2.0)
            val top = max(0.0, browserWindow.scrollY + (browserWindow.innerHeight - height) / 2.0)

            setLeftTopPx(left, top)
            didAutoCenter = true
        }

        browserWindow.requestAnimationFrame { _ -> attempt(5) }

    }

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
            persistWindowStateToStorage()
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
            persistWindowStateToStorage()
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
            persistWindowStateToStorage()
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
            persistWindowStateToStorage()
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
            persistWindowStateToStorage()
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
            persistWindowStateToStorage()
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
            persistWindowStateToStorage()
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
            persistWindowStateToStorage()
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
            persistWindowStateToStorage()
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
                    text { title }
                }

                condition({this@Window.onClose != null}) {
                    then {
                        button("close") {
                            className { "material-icons" }
                            onClick {
                                onClose?.invoke()
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

            condition({resizeable}) {
                then {
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

    block(childScope, c)

    scope.ui.build.afterBuild {
        with(childScope) {
            c.afterBuild()
        }
        c.restoreSizeFromStorage()
        if (!c.restorePositionFromStorage()) {
            c.centerInViewport()
        }
    }

    return c
}
