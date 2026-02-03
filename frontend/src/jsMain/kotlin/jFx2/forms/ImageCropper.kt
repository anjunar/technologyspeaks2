package jFx2.forms

import jFx2.core.capabilities.NodeScope
import jFx2.core.capabilities.UiScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.registerField
import jFx2.modals.ViewPort
import jFx2.modals.WindowConf
import jFx2.state.Disposable
import jFx2.state.ListChange
import jFx2.state.ListProperty
import jFx2.state.Property
import org.w3c.dom.Element
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.files.File
import org.w3c.files.FileReader

class ImageCropper(
    val name: String,
    val ui: UiScope,
    override val node: HTMLDivElement,
) : FormField<String?, HTMLDivElement>(), HasPlaceholder {

    // Cropped (or original) result as Data URL (e.g. "data:image/png;base64,...")
    val valueProperty = Property<String?>(null)

    // Source image (Data URL) used for editing.
    val sourceProperty = Property<String?>(null)

    val fileProperty = Property<File?>(null)

    val validatorsProperty = ListProperty<Validator>()

    // If set, crop rectangle keeps this width/height ratio.
    var aspectRatio: Double? = null

    // Used by the dialog to size the crop canvas preview.
    var previewMaxWidth: Int = 480
    var previewMaxHeight: Int = 360

    var outputType: String = "image/png"
    var outputQuality: Double = 0.92

    // Optional output size cap; when set, the cropped image is downscaled to fit.
    var outputMaxWidth: Int? = null
    var outputMaxHeight: Int? = null

    var windowTitle: String = "Crop Image"

    private var defaultValue: String? = null

    private lateinit var fileInput: HTMLInputElement
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

        previewImg = scope.create<HTMLImageElement>("img").also { it.classList.add("preview") }

        node.appendChild(toolbar)
        node.appendChild(previewImg)

        onDispose(bindStatusClasses(node, statusProperty))

        val onFileChange: (Event) -> Unit = onFileChange@{
            val f = fileInput.files?.item(0)
            fileProperty.set(f)
            if (f == null) return@onFileChange

            val reader = FileReader()
            reader.onload = {
                val src = reader.result as? String
                if (src != null) {
                    sourceProperty.set(src)
                    // Update the outside preview immediately; cropping happens in a window.
                    valueProperty.set(src)
                    openCropWindow(src)
                }
                ui.build.flush()
            }
            reader.readAsDataURL(f)
        }
        fileInput.addEventListener("change", onFileChange)
        onDispose { fileInput.removeEventListener("change", onFileChange) }

        val onCropClick: (Event) -> Unit = onCropClick@{
            val src = sourceProperty.get() ?: valueProperty.get()
            if (src.isNullOrBlank()) return@onCropClick
            openCropWindow(src)
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
            if (!v.isNullOrBlank() && sourceProperty.get().isNullOrBlank()) {
                sourceProperty.set(v)
            }
        })

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

    private fun openCropWindow(source: String) {
        if (source.isBlank()) return

        val session = ImageCropperSession(initialValue = valueProperty.get())

        lateinit var conf: WindowConf
        conf = WindowConf(
            title = windowTitle,
            component = {
                imageCropperDialog(this@ImageCropper, conf, source, session)
            },
            onClose = {
                session.closed = true
                if (!session.applied) {
                    valueProperty.set(session.initialValue)
                }
                ui.build.flush()
            }
        )

        ViewPort.addWindow(conf)
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
    }

    private fun bindStatusClasses(node: Element, status: ListProperty<String>): Disposable {
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
                is ListChange.Add -> change.items.forEach(::add)
                is ListChange.Remove -> change.items.forEach(::remove)
                is ListChange.Replace -> { change.old.forEach(::remove); change.new.forEach(::add) }
                is ListChange.Clear -> { for (c in owned) node.classList.remove(c); owned.clear() }
                is ListChange.SetAll -> resync(change.new)
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
