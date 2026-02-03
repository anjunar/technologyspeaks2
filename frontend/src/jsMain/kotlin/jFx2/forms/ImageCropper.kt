package jFx2.forms

import jFx2.core.capabilities.NodeScope
import jFx2.core.capabilities.UiScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.registerField
import jFx2.state.Disposable
import jFx2.state.ListProperty
import jFx2.state.Property
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import org.w3c.files.File
import org.w3c.files.FileReader
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private data class CropRect(
    val x: Double,
    val y: Double,
    val w: Double,
    val h: Double,
) {
    fun normalize(): CropRect {
        val nx = if (w >= 0) x else x + w
        val ny = if (h >= 0) y else y + h
        return CropRect(nx, ny, abs(w), abs(h))
    }
}

private data class DragState(
    val mode: DragMode,
    val startX: Double,
    val startY: Double,
    val startRect: CropRect?,
)

private enum class DragMode { New, Move, ResizeNW, ResizeNE, ResizeSW, ResizeSE }

class ImageCropper(
    val name: String,
    val ui: UiScope,
    override val node: HTMLDivElement,
) : FormField<String?, HTMLDivElement>(), HasPlaceholder {

    // Cropped result as Data URL (e.g. "data:image/png;base64,...")
    val valueProperty = Property<String?>(null)

    // Source image (Data URL) used for editing.
    val sourceProperty = Property<String?>(null)

    val fileProperty = Property<File?>(null)

    val validatorsProperty = ListProperty<Validator>()

    // If set, crop rectangle keeps this width/height ratio.
    var aspectRatio: Double? = null

    var previewMaxWidth: Int = 480
    var previewMaxHeight: Int = 360

    var outputType: String = "image/png"
    var outputQuality: Double = 0.92

    // Optional output size cap; when set, the cropped image is downscaled to fit.
    var outputMaxWidth: Int? = null
    var outputMaxHeight: Int? = null

    private var defaultValue: String? = null

    private var previewScale: Double = 1.0
    private var loadedImage: HTMLImageElement? = null

    private var crop: CropRect? = null
    private var drag: DragState? = null

    private lateinit var fileInput: HTMLInputElement
    private lateinit var canvas: HTMLCanvasElement
    private lateinit var previewImg: HTMLImageElement
    private lateinit var cropBtn: HTMLButtonElement
    private lateinit var clearBtn: HTMLButtonElement

    override var placeholder: String = ""

    override fun observeValue(listener: (String?) -> Unit): Disposable = valueProperty.observe(listener)

    override fun read(): String? = valueProperty.get()

    context(scope: NodeScope)
    fun initialize() {
        defaultValue = valueProperty.get()

        node.classList.add("image-cropper")
        // Make the field focusable so it can emit "focus" status for InputContainer.
        node.tabIndex = 0

        val toolbar = scope.create<HTMLDivElement>("div").also { it.classList.add("toolbar") }

        fileInput = scope.create<HTMLInputElement>("input").also {
            it.type = "file"
            it.accept = "image/*"
        }

        cropBtn = scope.create<HTMLButtonElement>("button").also {
            it.type = "button"
            it.textContent = "Crop"
            it.classList.add("hover")
        }

        clearBtn = scope.create<HTMLButtonElement>("button").also {
            it.type = "button"
            it.textContent = "Clear"
            it.classList.add("hover")
        }

        toolbar.appendChild(fileInput)
        toolbar.appendChild(cropBtn)
        toolbar.appendChild(clearBtn)

        val canvasWrap = scope.create<HTMLDivElement>("div").also { it.classList.add("canvas-wrap") }
        canvas = scope.create<HTMLCanvasElement>("canvas").also {
            it.classList.add("canvas")
            it.width = 1
            it.height = 1
        }
        canvasWrap.appendChild(canvas)

        previewImg = scope.create<HTMLImageElement>("img").also { it.classList.add("preview") }

        node.appendChild(toolbar)
        node.appendChild(canvasWrap)
        node.appendChild(previewImg)

        onDispose(bindStatusClasses(node, statusProperty))

        val onFileChange: (Event) -> Unit = onFileChange@{
            val f = fileInput.files?.item(0)
            fileProperty.set(f)

            if (f == null) {
                sourceProperty.set(null)
                ui.build.flush()
                return@onFileChange
            }

            val reader = FileReader()
            reader.onload = {
                sourceProperty.set(reader.result as? String)
                ui.build.flush()
            }
            reader.readAsDataURL(f)
        }
        fileInput.addEventListener("change", onFileChange)
        onDispose { fileInput.removeEventListener("change", onFileChange) }

        val onCropClick: (Event) -> Unit = {
            applyCrop()
            ui.build.flush()
        }
        cropBtn.addEventListener("click", onCropClick)
        onDispose { cropBtn.removeEventListener("click", onCropClick) }

        val onClearClick: (Event) -> Unit = {
            clear()
            ui.build.flush()
        }
        clearBtn.addEventListener("click", onClearClick)
        onDispose { clearBtn.removeEventListener("click", onClearClick) }

        // Keep preview in sync even if external bindings set valueProperty directly.
        onDispose(valueProperty.observe { v ->
            if (v.isNullOrBlank()) {
                previewImg.removeAttribute("src")
            } else {
                previewImg.src = v
            }

            if (v.isNullOrBlank()) statusProperty.add(Status.empty.name) else statusProperty.remove(Status.empty.name)
            if (v != defaultValue) statusProperty.add(Status.dirty.name) else statusProperty.remove(Status.dirty.name)

            validate()
        })

        // If the user sets value but not source, use it as the editable source image.
        onDispose(valueProperty.observe { v ->
            if (v != null && sourceProperty.get().isNullOrBlank()) {
                sourceProperty.set(v)
            }
        })

        onDispose(sourceProperty.observe { src ->
            if (src.isNullOrBlank()) {
                loadedImage = null
                crop = null
                render()
                return@observe
            }

            val img = document.createElement("img").unsafeCast<HTMLImageElement>()
            img.onload = {
                loadedImage = img
                setupCanvasFor(img)
                crop = defaultCrop()
                render()
            }
            img.src = src
        })

        wireCanvasDragging()

        // Focus handling for InputContainer styles.
        val onFocusIn: (Event) -> Unit = { statusProperty.add(Status.focus.name); ui.build.flush() }
        val onFocusOut: (Event) -> Unit = { statusProperty.remove(Status.focus.name); ui.build.flush() }
        node.addEventListener("focusin", onFocusIn)
        node.addEventListener("focusout", onFocusOut)
        onDispose {
            node.removeEventListener("focusin", onFocusIn)
            node.removeEventListener("focusout", onFocusOut)
        }
    }

    private fun validate() {
        val current = valueProperty.get().orEmpty()
        val errors = validatorsProperty.get().filter { !it.validate(current) }
        if (errors.isNotEmpty()) {
            statusProperty.add(Status.invalid.name)
            statusProperty.remove(Status.valid.name)
        } else {
            statusProperty.remove(Status.invalid.name)
            statusProperty.add(Status.valid.name)
        }
        errorsProperty.setAll(errors.map { it.message() })
    }

    private fun clear() {
        fileInput.value = ""
        fileProperty.set(null)
        sourceProperty.set(null)
        valueProperty.set(null)
        crop = null
        drag = null
        loadedImage = null
        render()
    }

    private fun setupCanvasFor(img: HTMLImageElement) {
        val w = max(1, img.naturalWidth)
        val h = max(1, img.naturalHeight)

        val s = min(
            1.0,
            min(previewMaxWidth.toDouble() / w.toDouble(), previewMaxHeight.toDouble() / h.toDouble())
        )

        previewScale = s
        canvas.width = max(1, (w * s).roundToInt())
        canvas.height = max(1, (h * s).roundToInt())
    }

    private fun defaultCrop(): CropRect {
        val cw = canvas.width.toDouble()
        val ch = canvas.height.toDouble()

        val r = aspectRatio
        if (r == null || r <= 0.0) return CropRect(0.0, 0.0, cw, ch)

        var w = cw
        var h = w / r
        if (h > ch) {
            h = ch
            w = h * r
        }
        val x = (cw - w) / 2.0
        val y = (ch - h) / 2.0
        return CropRect(x, y, w, h)
    }

    private fun render() {
        val ctx = canvas.getContext("2d")?.unsafeCast<CanvasRenderingContext2D>() ?: return
        val cw = canvas.width.toDouble()
        val ch = canvas.height.toDouble()

        ctx.clearRect(0.0, 0.0, cw, ch)

        val img = loadedImage ?: return

        ctx.drawImage(img, 0.0, 0.0, cw, ch)

        val r = crop?.normalize() ?: return
        if (r.w <= 0.0 || r.h <= 0.0) return

        // Dim the whole image...
        ctx.fillStyle = "rgba(0,0,0,0.40)"
        ctx.fillRect(0.0, 0.0, cw, ch)

        // ...then redraw the cropped part without dimming.
        ctx.save()
        ctx.beginPath()
        ctx.rect(r.x, r.y, r.w, r.h)
        ctx.clip()
        ctx.drawImage(img, 0.0, 0.0, cw, ch)
        ctx.restore()

        // Crop frame
        ctx.strokeStyle = "rgba(255,255,255,0.92)"
        ctx.lineWidth = 1.0
        ctx.strokeRect(r.x + 0.5, r.y + 0.5, max(0.0, r.w - 1.0), max(0.0, r.h - 1.0))

        // Handles (corners)
        val hs = 6.0
        fun handle(cx: Double, cy: Double) {
            val x = cx - hs / 2
            val y = cy - hs / 2
            ctx.fillStyle = "rgba(255,255,255,0.92)"
            ctx.fillRect(x, y, hs, hs)
            ctx.strokeStyle = "rgba(0,0,0,0.55)"
            ctx.strokeRect(x + 0.5, y + 0.5, hs - 1.0, hs - 1.0)
        }
        handle(r.x, r.y)
        handle(r.x + r.w, r.y)
        handle(r.x, r.y + r.h)
        handle(r.x + r.w, r.y + r.h)
    }

    private fun wireCanvasDragging() {
        val onMouseDown: (Event) -> Unit = onMouseDown@{ e ->
            val me = e.unsafeCast<MouseEvent>()
            node.focus()
            if (loadedImage == null) return@onMouseDown

            val p = canvasPoint(me)
            val cur = crop?.normalize()

            val hit = hitTest(cur, p.x, p.y)
            drag = DragState(hit, p.x, p.y, cur)

            if (hit == DragMode.New) {
                crop = CropRect(p.x, p.y, 1.0, 1.0)
            }

            render()
        }

        val onMouseMove: (Event) -> Unit = onMouseMove@{ e ->
            val me = e.unsafeCast<MouseEvent>()
            val st = drag ?: return@onMouseMove
            if (loadedImage == null) return@onMouseMove

            val p = canvasPoint(me)
            val cw = canvas.width.toDouble()
            val ch = canvas.height.toDouble()

            val minSize = 8.0
            val ratio = aspectRatio?.takeIf { it > 0.0 }

            fun clampMove(x: Double, y: Double, w: Double, h: Double): CropRect {
                val nx = x.coerceIn(0.0, max(0.0, cw - w))
                val ny = y.coerceIn(0.0, max(0.0, ch - h))
                return CropRect(nx, ny, w, h)
            }

            fun clampRect(r: CropRect): CropRect {
                var rr = r.normalize()
                var x = rr.x
                var y = rr.y
                var w = max(minSize, rr.w)
                var h = max(minSize, rr.h)

                if (w > cw) w = cw
                if (h > ch) h = ch
                if (x < 0.0) x = 0.0
                if (y < 0.0) y = 0.0
                if (x + w > cw) x = cw - w
                if (y + h > ch) y = ch - h
                return CropRect(x, y, w, h)
            }

            when (st.mode) {
                DragMode.Move -> {
                    val r = st.startRect ?: return@onMouseMove
                    val dx = p.x - st.startX
                    val dy = p.y - st.startY
                    crop = clampMove(r.x + dx, r.y + dy, r.w, r.h)
                }
                DragMode.New -> {
                    val dx = p.x - st.startX
                    val dy = p.y - st.startY
                    var r = CropRect(st.startX, st.startY, dx, dy)
                    r = if (ratio == null) {
                        r
                    } else {
                        val nr = r.normalize()
                        val signX = if (dx >= 0) 1 else -1
                        val signY = if (dy >= 0) 1 else -1
                        val w0 = nr.w
                        val h0 = nr.h
                        val (w1, h1) =
                            if (h0 == 0.0) {
                                Pair(w0, if (ratio == 0.0) 0.0 else w0 / ratio)
                            } else if (w0 / h0 > ratio) {
                                Pair(h0 * ratio, h0)
                            } else {
                                Pair(w0, if (ratio == 0.0) 0.0 else w0 / ratio)
                            }
                        val x = st.startX
                        val y = st.startY
                        CropRect(
                            x,
                            y,
                            w1 * signX,
                            h1 * signY
                        )
                    }
                    crop = clampRect(r)
                }
                DragMode.ResizeNW,
                DragMode.ResizeNE,
                DragMode.ResizeSW,
                DragMode.ResizeSE -> {
                    val r0 = st.startRect ?: return@onMouseMove
                    val anchorX: Double
                    val anchorY: Double
                    val cornerX: Double
                    val cornerY: Double

                    when (st.mode) {
                        DragMode.ResizeNW -> { anchorX = r0.x + r0.w; anchorY = r0.y + r0.h; cornerX = p.x; cornerY = p.y }
                        DragMode.ResizeNE -> { anchorX = r0.x; anchorY = r0.y + r0.h; cornerX = p.x; cornerY = p.y }
                        DragMode.ResizeSW -> { anchorX = r0.x + r0.w; anchorY = r0.y; cornerX = p.x; cornerY = p.y }
                        DragMode.ResizeSE -> { anchorX = r0.x; anchorY = r0.y; cornerX = p.x; cornerY = p.y }
                        else -> return@onMouseMove
                    }

                    var dx = cornerX - anchorX
                    var dy = cornerY - anchorY

                    var r = CropRect(anchorX, anchorY, dx, dy)

                    r = if (ratio == null) {
                        r
                    } else {
                        val nr = r.normalize()
                        val signX = if (dx >= 0) 1 else -1
                        val signY = if (dy >= 0) 1 else -1
                        val w0 = nr.w
                        val h0 = nr.h
                        val (w1, h1) =
                            if (h0 == 0.0) {
                                Pair(w0, if (ratio == 0.0) 0.0 else w0 / ratio)
                            } else if (w0 / h0 > ratio) {
                                Pair(h0 * ratio, h0)
                            } else {
                                Pair(w0, if (ratio == 0.0) 0.0 else w0 / ratio)
                            }
                        CropRect(anchorX, anchorY, w1 * signX, h1 * signY)
                    }

                    crop = clampRect(r)
                }
            }

            render()
        }

        val onMouseUp: (Event) -> Unit = {
            if (drag != null) {
                drag = null
                render()
            }
        }

        canvas.addEventListener("mousedown", onMouseDown)
        window.addEventListener("mousemove", onMouseMove)
        window.addEventListener("mouseup", onMouseUp)

        onDispose {
            canvas.removeEventListener("mousedown", onMouseDown)
            window.removeEventListener("mousemove", onMouseMove)
            window.removeEventListener("mouseup", onMouseUp)
        }
    }

    private fun hitTest(r: CropRect?, x: Double, y: Double): DragMode {
        if (r == null) return DragMode.New

        val rr = r.normalize()
        val hs = 10.0

        fun near(px: Double, py: Double, cx: Double, cy: Double): Boolean =
            abs(px - cx) <= hs && abs(py - cy) <= hs

        val nw = near(x, y, rr.x, rr.y)
        val ne = near(x, y, rr.x + rr.w, rr.y)
        val sw = near(x, y, rr.x, rr.y + rr.h)
        val se = near(x, y, rr.x + rr.w, rr.y + rr.h)

        return when {
            nw -> DragMode.ResizeNW
            ne -> DragMode.ResizeNE
            sw -> DragMode.ResizeSW
            se -> DragMode.ResizeSE
            x >= rr.x && x <= rr.x + rr.w && y >= rr.y && y <= rr.y + rr.h -> DragMode.Move
            else -> DragMode.New
        }
    }

    private data class Pt(val x: Double, val y: Double)

    private fun canvasPoint(e: MouseEvent): Pt {
        val rect = canvas.getBoundingClientRect()
        val sx = if (rect.width == 0.0) 1.0 else canvas.width.toDouble() / rect.width
        val sy = if (rect.height == 0.0) 1.0 else canvas.height.toDouble() / rect.height

        val x = (e.clientX.toDouble() - rect.left) * sx
        val y = (e.clientY.toDouble() - rect.top) * sy

        return Pt(x, y)
    }

    private fun applyCrop() {
        val img = loadedImage ?: return

        val r = (crop?.normalize() ?: defaultCrop()).normalize()
        if (r.w <= 0.0 || r.h <= 0.0) return

        val sx = r.x / previewScale
        val sy = r.y / previewScale
        val sw = r.w / previewScale
        val sh = r.h / previewScale

        var outW = max(1, sw.roundToInt())
        var outH = max(1, sh.roundToInt())

        val maxW = outputMaxWidth
        val maxH = outputMaxHeight
        if ((maxW != null && outW > maxW) || (maxH != null && outH > maxH)) {
            val s = min(
                if (maxW == null) 1.0 else maxW.toDouble() / outW.toDouble(),
                if (maxH == null) 1.0 else maxH.toDouble() / outH.toDouble()
            )
            outW = max(1, (outW * s).roundToInt())
            outH = max(1, (outH * s).roundToInt())
        }

        val out = document.createElement("canvas").unsafeCast<HTMLCanvasElement>()
        out.width = outW
        out.height = outH

        val octx = out.getContext("2d")?.unsafeCast<CanvasRenderingContext2D>() ?: return
        octx.drawImage(img, sx, sy, sw, sh, 0.0, 0.0, outW.toDouble(), outH.toDouble())

        val url = out.toDataURL(outputType, outputQuality)
        valueProperty.set(url)
    }

    private fun bindStatusClasses(node: org.w3c.dom.Element, status: ListProperty<String>): Disposable {
        val owned = LinkedHashSet<String>()

        fun add(cls: String) {
            val c = cls.trim()
            if (c.isEmpty()) return
            if (owned.add(c)) node.classList.add(c)
        }

        fun remove(cls: String) {
            val c = cls.trim()
            if (c.isEmpty()) return
            if (owned.remove(c)) node.classList.remove(c)
        }

        fun resync(items: List<String>) {
            for (c in owned) node.classList.remove(c)
            owned.clear()
            for (c in items) add(c)
        }

        resync(status.get())

        return status.observeChanges { change ->
            when (change) {
                is jFx2.state.ListChange.Add -> change.items.forEach(::add)
                is jFx2.state.ListChange.Remove -> change.items.forEach(::remove)
                is jFx2.state.ListChange.Replace -> { change.old.forEach(::remove); change.new.forEach(::add) }
                is jFx2.state.ListChange.Clear -> { for (c in owned) node.classList.remove(c); owned.clear() }
                is jFx2.state.ListChange.SetAll -> resync(change.new)
            }
        }
    }
}

context(scope: NodeScope)
fun imageCropper(
    name: String,
    block: context(NodeScope) ImageCropper.() -> Unit = {},
): ImageCropper {
    val el = scope.create<HTMLDivElement>("div").also {
        it.classList.add("image-cropper-field")
    }
    val c = ImageCropper(name, scope.ui, el)

    scope.attach(c)
    registerField(name, c)

    val childScope = scope.fork(parent = c.node, owner = c, ctx = scope.ctx, ElementInsertPoint(c.node))
    block(childScope, c)

    scope.ui.build.afterBuild {
        with(childScope) { c.initialize() }
    }

    return c
}
