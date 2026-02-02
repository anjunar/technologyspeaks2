@file:Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")

package jFx2.forms

import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.registerField
import jFx2.core.dsl.renderFields
import jFx2.core.dsl.style
import jFx2.core.template
import jFx2.forms.editor.plugins.EditorPlugin
import jFx2.forms.editor.prosemirror.EditorState
import jFx2.forms.editor.prosemirror.EditorStateConfig
import jFx2.forms.editor.prosemirror.EditorView
import jFx2.forms.editor.prosemirror.Plugin
import jFx2.forms.editor.prosemirror.Schema
import jFx2.forms.editor.prosemirror.addListNodes
import jFx2.forms.editor.prosemirror.baseKeymap
import jFx2.forms.editor.prosemirror.chainCommands
import jFx2.forms.editor.prosemirror.exitCode
import jFx2.forms.editor.prosemirror.history
import jFx2.forms.editor.prosemirror.keymap
import jFx2.forms.editor.prosemirror.liftListItem
import jFx2.forms.editor.prosemirror.newlineInCode
import jFx2.forms.editor.prosemirror.redo
import jFx2.forms.editor.prosemirror.sinkListItem
import jFx2.forms.editor.prosemirror.splitBlock
import jFx2.forms.editor.prosemirror.splitListItem
import jFx2.forms.editor.prosemirror.undo
import jFx2.layout.div
import jFx2.layout.hbox
import jFx2.forms.editor.prosemirror.schema as basicSchema
import jFx2.state.Disposable
import org.w3c.dom.HTMLDivElement
import kotlin.js.json


@Suppress("CAST_NEVER_SUCCEEDS")
class Editor(override val node: HTMLDivElement) : FormField<String, HTMLDivElement>() {

    fun createState(): EditorState {

        val editorPlugins = this@Editor.children.map { (it as EditorPlugin).plugin() as Plugin<Any ?> }

        val specs: dynamic = {}

        this@Editor.children.forEach {
            val p = it as EditorPlugin
            if (p.nodeSpec != null) {
                specs[p.name] = p.nodeSpec
            }
        }

        val customNodes =
            addListNodes(
                basicSchema.spec.nodes.append(specs),
                "paragraph block*",
                "block"
            )

        val customSchema = Schema(
            jsObject {
                nodes = customNodes
                marks = basicSchema.spec.marks
            }
        )

        val itemType = customSchema.nodes["list_item"] ?: error("list_item missing in schema")

        val extraKeys = json(
            "Enter" to chainCommands(
                splitListItem(itemType),
                newlineInCode,
                splitBlock,
                exitCode
            ),

            "Tab" to sinkListItem(itemType),
            "Shift-Tab" to liftListItem(itemType),

            "Mod-z" to undo,
            "Mod-y" to redo
        )

        val plugins = arrayOf(
            history(),
            keymap(extraKeys),
            keymap(baseKeymap)
        )

        val cfg = jsObject<EditorStateConfig> {
            schema = customSchema
            this.plugins = plugins + editorPlugins
        }

        return EditorState.create(cfg)
    }

    context(scope : NodeScope)
    fun afterBuild() {

        template {
            hbox {

                style {
                    alignItems = "center"
                }

                renderFields(*this@Editor.children.toTypedArray())
            }

            div {
                val createState = createState()

                val view = EditorView(this@div.node, jsObject {
                    state = createState
                })

                this@Editor.children.forEach {
                    (it as EditorPlugin).view = view
                }
            }

        }


    }

    override fun read(): String {
        return ""
    }

    override fun observeValue(listener: (String) -> Unit): Disposable {
        return Disposable {}
    }

}


context(scope : NodeScope)
fun editor(name: String, block: context(NodeScope) Editor.() -> Unit = {}): Editor {
    val el = scope.create<HTMLDivElement>("div").also {
        it.classList.add("editor")
    }
    val c = Editor(el)
    scope.attach(c)

    registerField(name, c)

    val childScope = scope.fork(parent = c.node, owner = c, ctx = scope.ctx, ElementInsertPoint(c.node))
    block(childScope, c)

    scope.ui.build.afterBuild {
        with(childScope) {
            c.afterBuild()
        }
    }

    return c
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
fun <T : Any> jsObject(block: T.() -> Unit): T {
    val o = js("({})") as T
    block(o)
    return o
}