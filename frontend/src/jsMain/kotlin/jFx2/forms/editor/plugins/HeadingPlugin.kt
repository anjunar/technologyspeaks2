package jFx2.forms.editor.plugins

import jFx2.controls.text
import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.template
import jFx2.forms.Select
import jFx2.forms.editor.prosemirror.EditorState
import jFx2.forms.editor.prosemirror.EditorView
import jFx2.forms.editor.prosemirror.NodeSpec
import jFx2.forms.editor.prosemirror.Plugin
import jFx2.forms.editor.prosemirror.PluginKey
import jFx2.forms.editor.prosemirror.attrInt
import jFx2.forms.editor.prosemirror.nodeType
import jFx2.forms.editor.prosemirror.setBlockType
import jFx2.forms.editor.prosemirror.PluginSpec
import jFx2.forms.jsObject
import jFx2.forms.option
import jFx2.forms.select
import org.w3c.dom.HTMLDivElement

class Heading(override val node: HTMLDivElement) : Component<HTMLDivElement>(), EditorPlugin {

    override lateinit var view: EditorView

    lateinit var selectComponent : Select

    override val name: String = "heading"

    private fun activeHeadingLevel(v: EditorView): Int {
        val state = v.state
        val sel = state.selection
        val headingType = state.schema.nodeType("heading") ?: return 0

        var found = 0
        state.doc.nodesBetween(sel.from, sel.to, { node, _, _, _ ->
            if (node.type == headingType) {
                val lvl = node.attrInt("level") ?: 0
                if (lvl in 1..6) {
                    found = lvl
                    return@nodesBetween false // nicht weiter absteigen
                }
            }
            true
        })

        return found
    }

    private fun setParagraph(v: EditorView) {
        val state = v.state
        val paraType = state.schema.nodeType("paragraph") ?: return

        val dispatch = v::dispatch
        setBlockType(paraType)(state, dispatch, v)
        v.focus()
    }

    private fun setHeading(v: EditorView, level: Int) {
        val state = v.state
        val type = state.schema.nodeType("heading") ?: return

        val attrs = js("({})")
        attrs.level = level

        val dispatch = v::dispatch
        setBlockType(type, attrs)(state, dispatch, v)
        v.focus()
    }

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
                        selectComponent.value.set(levelToValue(activeHeadingLevel(v)))
                    }
                }
            }
        }
        return Plugin(spec)
    }

    override val nodeSpec: NodeSpec? = null

    context(scope: NodeScope)
    fun initialize() {

        template {
            selectComponent = select("heading") {

                observeValue { when(it) {
                    "p" -> setParagraph(view)
                    "h1" -> setHeading(view, 1)
                    "h2" -> setHeading(view, 2)
                    "h3" -> setHeading(view, 3)
                    "h4" -> setHeading(view, 4)
                    "h5" -> setHeading(view, 5)
                    "h6" -> setHeading(view, 6)
                } }

                option("p") {
                    text { "Paragraph" }
                }
                option("h1") {
                    text { "Heading 1" }
                }
                option("h2") {
                    text { "Heading 2" }
                }
                option("h3") {
                    text { "Heading 3" }
                }
                option("h4") {
                    text { "Heading 4" }
                }
                option("h5") {
                    text { "Heading 5" }
                }
                option("h6") {
                    text { "Heading 6" }
                }
            }
        }

    }

    companion object {
        val KEY = PluginKey<Unit>("heading-sync")
    }

}

context(scope: NodeScope)
fun headingPlugin(block: context(NodeScope) Heading.() -> Unit = {}): Heading {
    val el = scope.create<HTMLDivElement>("div")
    val c = Heading(el)
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
