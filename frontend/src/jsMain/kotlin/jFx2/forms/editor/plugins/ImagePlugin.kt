package jFx2.forms.editor.plugins

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.forms.editor.prosemirror.*
import jFx2.forms.jsObject
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

interface StyleAttr {
    var src : String
    var style : String?
}

class ImagePlugin(
    override val node: HTMLDivElement
) : Component<HTMLDivElement>(), EditorPlugin {

    override lateinit var view: EditorView

    var onOpenImage: ((attrs: dynamic) -> Unit)? = null

    fun insertImage(
        source: String,
        width: Int? = null,
        height: Int? = null,
        pos: Int? = null
    ) {
        val v = view
        if (source.isBlank()) return

        val state = v.state
        val imageType = state.schema.nodeType("image") ?: return

        var styleString = ""
        if (width != null) styleString += "width:${width}px;"
        if (height != null) styleString += "height:${height}px;"
        val styleAttr = if (styleString.isBlank()) null else styleString

        var baseAttrs: dynamic = js("({})")

        if (pos != null) {
            state.doc.nodeAt(pos)?.let {
                baseAttrs = js("Object.assign({}, it.attrs)")
            }
        }

        val attrs = jsObject<StyleAttr> {
            src = source
            style = styleAttr
        }

        js("Object.assign")(attrs, baseAttrs)

        var tr = state.tr

        when {
            pos != null ->
                tr = tr.setNodeMarkup(pos, imageType, attrs)

            state.selection is NodeSelection &&
                    (state.selection as NodeSelection).node.type == imageType ->
                tr = tr.setNodeMarkup(state.selection.from, imageType, attrs)

            else ->
                tr = tr.replaceSelectionWith(imageType.create(attrs), false)
        }

        v.dispatch(tr.scrollIntoView())
        v.focus()
    }

    private fun openFromSelection() {
        val state = view.state
        val imageType = state.schema.nodeType("image") ?: return

        var attrs: dynamic = js("({})")

        if (state.selection is NodeSelection) {
            val sel = state.selection as NodeSelection
            if (sel.node.type == imageType) {
                attrs = js("Object.assign({}, sel.node.attrs)")
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

    context(scope: NodeScope)
    fun initialize() {
        // optional: Toolbar-Buttons, Commands etc.
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
