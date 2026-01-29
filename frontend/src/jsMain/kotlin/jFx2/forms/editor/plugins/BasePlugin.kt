package jFx2.forms.editor.plugins

import jFx2.controls.Button
import jFx2.controls.button
import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.className
import jFx2.core.template
import jFx2.forms.editor.prosemirror.EditorState
import jFx2.forms.editor.prosemirror.EditorView
import jFx2.forms.editor.prosemirror.Node
import jFx2.forms.editor.prosemirror.Plugin
import jFx2.forms.editor.prosemirror.PluginKey
import jFx2.forms.editor.prosemirror.PluginSpec
import jFx2.forms.editor.prosemirror.Schema
//import jFx2.forms.editor.prosemirror.toggleMark
import jFx2.forms.editor.prosemirror.undo
import jFx2.forms.editor.prosemirror.redo
import jFx2.forms.editor.prosemirror.toggleMark
import jFx2.forms.jsObject
import jFx2.layout.hbox
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import kotlin.js.unsafeCast

class BasePlugin(override val node: HTMLDivElement) : Component<HTMLDivElement>(), EditorPlugin {

    override lateinit var view: EditorView

    private lateinit var boldBtn: Button
    private lateinit var italicBtn: Button
    private lateinit var undoBtn: Button
    private lateinit var redoBtn: Button

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

    override fun plugin(): Plugin<*> {
        val spec = jsObject<PluginSpec<Unit>> {
            key = KEY
            view = { _ ->
                jsPluginView { v, prev ->
                    if (prev == null ||
                        prev.doc != v.state.doc ||
                        prev.selection != v.state.selection
                    ) {
                        updateUiFromState()
                    }
                }
            }
        }
        return Plugin(spec)
    }

    private fun toggleMarkCommand(markName: String) {
        val v = view
        val state = v.state

        val type = state.schema.marks.asDynamic()[markName]
        if (type != null) {
            toggleMark(type)(state, { tr -> v.dispatch(tr) }, v)
        }
        v.focus()
        updateUiFromState()
    }

    private fun isMarkActive(markTypeName: String): Boolean {
        val state = view.state
        val selection = state.selection
        val from = selection.from
        val to = selection.to
        val empty = selection.empty

        val type = state.schema.marks.asDynamic()[markTypeName] ?: return false

        return if (empty) {
            val stored = state.storedMarks
            if (stored != null) {
                type.isInSet(stored) != null
            } else {
                type.isInSet(state.selection._from.marks()) != null
            }
        } else {
            var found = false
            state.doc.nodesBetween(from, to, { node: Node, pos: Int, parent: Node, index: Int ->
                if (type.isInSet(node.marks) != null) {
                    found = true
                    false
                } else {
                    true
                }
            })
            found
        }
    }

    private fun undoCommand() {
        val v = view
        undo(v.state, { tr -> v.dispatch(tr)}, view)
        v.focus()
        updateUiFromState()
    }

    private fun redoCommand() {
        val v = view
        redo(v.state, { tr -> v.dispatch(tr)}, view)
        v.focus()
        updateUiFromState()
    }

    private fun updateUiFromState() {
        val boldActive = isMarkActive("strong") || isMarkActive("bold")
        val italicActive = isMarkActive("em") || isMarkActive("italic")

        setActive(boldBtn.node, boldActive)
        setActive(italicBtn.node, italicActive)

        undoBtn.node.disabled = false
        redoBtn.node.disabled = false
    }

    private fun setActive(btn: HTMLButtonElement, active: Boolean) {
        if (active) btn.classList.add("active") else btn.classList.remove("active")
    }

    context(scope: NodeScope)
    fun initialize() {

        template {
            hbox {

                boldBtn = button("format_bold") {
                    className { "material-icons" }
                    onClick { toggleMarkCommand("strong") }
                }

                italicBtn = button("format_italic") {
                    className { "material-icons" }
                    onClick { toggleMarkCommand("em") }
                }

                undoBtn = button("undo") {
                    className { "material-icons" }
                    onClick { undoCommand() }
                }

                redoBtn = button("redo") {
                    className { "material-icons" }
                    onClick { redoCommand() }
                }
            }
        }

    }

    companion object {
        val KEY = PluginKey<Unit>("base-sync")
    }
}

context(scope: NodeScope)
fun basePlugin(block: context(NodeScope) BasePlugin.() -> Unit = {}): BasePlugin {
    val el = scope.create<HTMLDivElement>("div")
    val c = BasePlugin(el)
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
            c.initialize()
        }
    }

    return c
}
