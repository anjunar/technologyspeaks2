@file:Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")

package jFx2.forms

import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.registerField
import jFx2.core.dsl.renderFields
import jFx2.core.template
import jFx2.forms.editor.plugins.EditorPlugin
import jFx2.forms.editor.prosemirror.EditorState
import jFx2.forms.editor.prosemirror.EditorStateConfig
import jFx2.forms.editor.prosemirror.EditorView
import jFx2.forms.editor.prosemirror.Plugin
import jFx2.forms.editor.prosemirror.baseKeymap
import jFx2.forms.editor.prosemirror.history
import jFx2.forms.editor.prosemirror.keymap
import jFx2.forms.editor.prosemirror.redo
import jFx2.forms.editor.prosemirror.undo
import jFx2.layout.hbox
import jFx2.forms.editor.prosemirror.schema as basicSchema
import jFx2.state.Disposable
import org.w3c.dom.HTMLDivElement

@Suppress("CAST_NEVER_SUCCEEDS")
class Editor(override val node: HTMLDivElement) : FormField<String, HTMLDivElement>() {

    fun createState(): EditorState {
        val extraKeys = js("({})")
        extraKeys["Mod-z"] = undo
        extraKeys["Mod-y"] = redo

        val plugins = arrayOf(
            history(),
            keymap(extraKeys),
            keymap(baseKeymap)
        )

        val editorPlugins = this@Editor.children.map { (it as EditorPlugin).plugin() as Plugin<Any ?> }

        val cfg = jsObject<EditorStateConfig> {
            schema = basicSchema
            this.plugins = plugins + editorPlugins
        }

        return EditorState.create(cfg)
    }

    context(scope : NodeScope)
    fun afterBuild() {

        template {
            hbox {
                renderFields(*this@Editor.children.toTypedArray())
            }

            val createState = createState()

            val view = EditorView(node, jsObject {
                state = createState
            })

            this@Editor.children.forEach {
                (it as EditorPlugin).view = view
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

    registerField(name, c)

    val childScope = scope.fork(parent = c.node, owner = c, ctx = scope.ctx, ElementInsertPoint(c.node))
    block(childScope, c)

    scope.ui.build.afterBuild {
        with(childScope) {
            c.afterBuild()
        }
    }

    scope.attach(c)
    return c
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
fun <T : Any> jsObject(block: T.() -> Unit): T {
    val o = js("({})") as T
    block(o)
    return o
}