@file:OptIn(ExperimentalUuidApi::class)

package jFx2.forms.editor.plugins

import jFx2.controls.Image
import jFx2.controls.button
import jFx2.controls.image
import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.className
import jFx2.core.dsl.style
import jFx2.core.dsl.subscribeBidirectional
import jFx2.core.template
import jFx2.forms.editor.prosemirror.*
import jFx2.forms.form
import jFx2.forms.input
import jFx2.forms.jsObject
import jFx2.layout.div
import jFx2.layout.hbox
import jFx2.modals.ViewPort
import jFx2.modals.WindowConf
import jFx2.state.Property
import kotlinx.browser.window
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader
import kotlin.js.Json
import kotlin.js.json
import kotlin.uuid.ExperimentalUuidApi

class Dimensions(val width : Property<Double> = Property(320.0), val height : Property<Double> = Property(240.0))

class ImagePlugin(override val node: HTMLDivElement) : Component<HTMLDivElement>(), EditorPlugin {

    override lateinit var view: EditorView

    override val name: String = "image"

    var onOpenImage: ((attrs : dynamic) -> Unit)? = { attrs ->

        val dimensions = Dimensions()

        ViewPort.addWindow(
            WindowConf(
            "Add Image",
            {

                form(model = dimensions, clazz = Dimensions::class) { form ->
                    var myImage : Image? = null

                    onSubmit {
                        insertImage(myImage!!.src, form.width.value.toInt(), form.height.value.toInt())
                    }

                    div {
                        style {
                            display = "flex"
                            justifyContent = "center"
                            width = "320px"
                            height = "240px"
                        }

                        myImage = image {
                            if (attrs["src"] != null) {
                                src = attrs["src"] as String
                            }
                            style {
                                maxWidth = "320px"
                                maxHeight = "240px"
                            }

                            window.setTimeout({
                                form.width.set(myImage!!.node.width.toDouble())
                                form.height.set(myImage!!.node.height.toDouble())
                            }, 100)

                        }
                    }


                    input("image", "file") {

                        onChange { event ->
                            val input = event.target as HTMLInputElement

                            val reader = FileReader()

                            reader.onload = {
                                myImage!!.src = reader.result as String

                                window.setTimeout({
                                    dimensions.width.set(myImage.node.width.toDouble())
                                    dimensions.height.set(myImage.node.height.toDouble())
                                }, 100)

                            }

                            reader.readAsDataURL(input.files?.item(0)!!)
                        }

                    }

                    hbox {
                        input("width", "number") {
                            placeholder = "Width"
                            subscribeBidirectional(form.width, valueAsNumberProperty)
                        }

                        input("height", "number") {
                            placeholder = "Height"
                            subscribeBidirectional(form.height, valueAsNumberProperty)
                        }
                    }

                    button("Submit") {
                        style {
                            marginLeft = "10px"
                        }
                    }

                }

            }
        ))
    }

    fun insertImage(source: String, width: Int, height: Int) {

        val state = view.state
        val imageType = state.schema.nodes["image"] ?: return

        val styleString = "width:${width}px;height:${height}px;"

        val attrs = json(
            "src" to source,
            "style" to styleString
        )

        var tr = state.tr

        tr = tr.replaceSelectionWith(imageType.create(attrs), false)

        view.dispatch(tr.scrollIntoView())
        view.focus()
    }

    private fun openFromSelection() {
        val state = view.state
        val imageType = state.schema.nodes["image"] ?: return

        var attrs: dynamic = {}

        if (state.selection is NodeSelection) {
            val sel = state.selection
            if (sel.node.type == imageType) {
                attrs = sel.node.attrs
            }
        }

        onOpenImage?.invoke(attrs)
    }

    override fun plugin(): Plugin<*> {
        val spec = jsObject<PluginSpec<Unit>> {
            key = KEY

            props = jsObject {
                handleDOMEvents = jsObject {
                    dblclick = { v: dynamic, e: dynamic ->
                        val target = e.target as? HTMLElement
                        if (target != null && target.nodeName == "IMG") {
                            openFromSelection()
                            true
                        } else {
                            false
                        }
                    }
                }
            }
        }

        return Plugin(spec)
    }

    override val nodeSpec: NodeSpec = jsObject {
        inline = true

        attrs = jsObject {
            src = jsObject { default = null }
            alt = jsObject { default = null }
            title = jsObject { default = null }
            style = jsObject { default = null }
        }

        group = "inline"
        draggable = true

        parseDOM = arrayOf(
            jsObject {
                tag = "img[src]"
                getAttrs = { dom ->
                    val el = dom as Element
                    json(
                        "src" to el.getAttribute("src"),
                        "alt" to el.getAttribute("alt"),
                        "title" to el.getAttribute("title"),
                        "style" to el.getAttribute("style")
                    )
                }
            }
        )

        toDOM = { node ->
            val attrs: Json = json(
                "src" to node.attrs["src"],
                "alt" to node.attrs["alt"],
                "title" to node.attrs["title"],
                "style" to node.attrs["style"]
            )

            arrayOf("img", attrs) as DOMOutputSpec
        }
    }

    context(scope: NodeScope)
    fun initialize() {
        template {

            button("image") {
                className { "material-icons" }
                type("button")
                onClick { openFromSelection() }
            }

        }
    }

    companion object {
        val KEY = PluginKey<Unit>("image-plugin")
    }
}

context(scope: NodeScope)
fun imagePlugin(
    block: context(NodeScope) ImagePlugin.() -> Unit = {}
): ImagePlugin {

    val el = scope.create<HTMLDivElement>("div")
    val plugin = ImagePlugin(el)
    scope.attach(plugin)

    val childScope = scope.fork(
        parent = plugin.node,
        owner = plugin,
        ctx = scope.ctx,
        insertPoint = ElementInsertPoint(plugin.node)
    )

    scope.ui.build.afterBuild {
        with(childScope) {
            plugin.initialize()
        }
    }

    block(childScope, plugin)
    return plugin
}
