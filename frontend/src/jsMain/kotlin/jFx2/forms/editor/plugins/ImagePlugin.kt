package jFx2.forms.editor.plugins

import jFx2.controls.text
import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.forms.Select
import jFx2.forms.editor.prosemirror.EditorState
import jFx2.forms.editor.prosemirror.EditorView
import jFx2.forms.editor.prosemirror.Plugin
import jFx2.forms.editor.prosemirror.PluginKey
import jFx2.forms.editor.prosemirror.PluginSpec
import jFx2.forms.editor.prosemirror.attrInt
import jFx2.forms.editor.prosemirror.nodeType
import jFx2.forms.editor.prosemirror.setBlockType
import jFx2.forms.jsObject
import jFx2.forms.option
import jFx2.forms.select
import org.w3c.dom.HTMLDivElement
import kotlin.js.unsafeCast

class ImagePlugin(override val node: HTMLDivElement) : Component<HTMLDivElement>(), EditorPlugin {

    override lateinit var view: EditorView

    private fun jsPluginView(
        onUpdate: (view: EditorView, prevState: EditorState?) -> Unit
    ): dynamic {
        val obj = js("({})")
        obj.update = { v: dynamic, prev: dynamic ->
            onUpdate(
                v.unsafeCast<EditorView>(),
                prev?.unsafeCast<EditorState>()
            )
        }
        return obj
    }

    private fun levelToValue(level: Int): String =
        if (level in 1..6) "h$level" else "p"

    override fun plugin(): Plugin<*> {
        val spec = jsObject<PluginSpec<Unit>> {
            key = KEY
            view = { _ ->
                jsPluginView { v, prev ->
                    if (prev == null ||
                        prev.doc != v.state.doc ||
                        prev.selection != v.state.selection
                    ) {

                    }
                }
            }
        }
        return Plugin(spec)
    }

    context(scope: NodeScope)
    fun initialize() {


    }

    companion object {
        val KEY = PluginKey<Unit>("image-sync")
    }

}

context(scope: NodeScope)
fun imagePlugin(block: context(NodeScope) ImagePlugin.() -> Unit = {}): ImagePlugin {
    val el = scope.create<HTMLDivElement>("div")
    val c = ImagePlugin(el)
    scope.attach(c)

    val childScope = scope.fork(
        parent = c.node,
        owner = c,
        ctx = scope.ctx,
        insertPoint = ElementInsertPoint(c.node)
    )

    scope.ui.build.afterBuild {
        with(childScope) {
            c.initialize()
        }
    }

    block(childScope, c)

    return c
}
