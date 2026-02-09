@file:Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")

package jFx2.forms

import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.className
import jFx2.core.dsl.registerField
import jFx2.core.dsl.renderFields
import jFx2.core.dsl.style
import jFx2.core.template
import jFx2.forms.editor.plugins.EditorPlugin
import jFx2.forms.editor.prosemirror.EditorState
import jFx2.forms.editor.prosemirror.EditorStateConfig
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
import jFx2.state.Property
import org.w3c.dom.HTMLDivElement
import kotlin.js.json
import kotlin.js.unsafeCast
import jFx2.forms.editor.prosemirror.EditorView as ProseMirrorEditorView


@Suppress("CAST_NEVER_SUCCEEDS")
class Editor(override val node: HTMLDivElement) : FormField<String, HTMLDivElement>() {

    val valueProperty = Property("")

    private var editorSchema: Schema? = null
    private var editorPlugins: Array<Plugin<Any?>> = emptyArray()
    private var editorView: ProseMirrorEditorView? = null

    private fun parseDoc(schema: Schema, value: String): jFx2.forms.editor.prosemirror.Node? {
        val raw = value.trim()
        if (raw.isEmpty()) return null

        return runCatching {
            val first: dynamic = js("JSON.parse")(raw)
            val parsed: dynamic = if (jsTypeOf(first) == "string") js("JSON.parse")(first) else first
            schema.asDynamic().nodeFromJSON(parsed).unsafeCast<jFx2.forms.editor.prosemirror.Node>()
        }.getOrNull()
    }

    private fun normalizeNodeJson(input: dynamic): dynamic {
        fun isArray(v: dynamic): Boolean =
            jsTypeOf(v) != "undefined" && v != null && (js("Array.isArray")(v) as Boolean)

        fun safeObject(v: dynamic): dynamic =
            if (jsTypeOf(v) != "undefined" && v != null) v else json()

        val type = (input?.type as? String).orEmpty()
        val text = (input?.text as? String).orEmpty()

        val content: Array<dynamic> =
            if (isArray(input?.content)) {
                input.content.unsafeCast<Array<dynamic>>().map { normalizeNodeJson(it) }.toTypedArray()
            } else {
                emptyArray()
            }

        val marks: Array<dynamic> =
            if (isArray(input?.marks)) {
                input.marks.unsafeCast<Array<dynamic>>().map { normalizeNodeJson(it) }.toTypedArray()
            } else {
                emptyArray()
            }

        return json(
            "type" to type,
            "content" to content,
            "attrs" to safeObject(input?.attrs),
            "text" to text,
            "marks" to marks
        )
    }

    private fun serializeDoc(doc: jFx2.forms.editor.prosemirror.Node): String {
        val rawJson = doc.asDynamic().toJSON()
        val normalized = normalizeNodeJson(rawJson)
        return js("JSON.stringify")(normalized).unsafeCast<String>()
    }

    fun createState(initialValue: String = valueProperty.get()): EditorState {

        val pluginInstances = this@Editor.children.map { (it as EditorPlugin).plugin() as Plugin<Any?> }

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

        val defaultPlugins = arrayOf(
            history(),
            keymap(extraKeys),
            keymap(baseKeymap)
        )

        val allPlugins = defaultPlugins + pluginInstances

        editorSchema = customSchema
        editorPlugins = allPlugins

        val initialDoc = parseDoc(customSchema, initialValue)

        val cfg = jsObject<EditorStateConfig> {
            schema = customSchema
            this.plugins = allPlugins
            if (initialDoc != null) {
                doc = initialDoc
            }
        }

        return EditorState.create(cfg)
    }

    context(scope : NodeScope)
    fun afterBuild() {
        val ui = scope.ui

        template {
            hbox {

                style {
                    alignItems = "center"
                }

                renderFields(*this@Editor.children.toTypedArray())
            }

            div {

                style {
                    flex = "1"
                }

                val initialValue = valueProperty.get()
                val initialState = createState(initialValue)

                var lastSeenValue = initialValue

                lateinit var view: ProseMirrorEditorView
                view = ProseMirrorEditorView(this@div.node, jsObject {
                    state = initialState
                    dispatchTransaction = { tr ->
                        val newState = view.state.apply(tr)
                        view.updateState(newState)

                        if (tr.docChanged) {
                            val next = serializeDoc(newState.doc)
                            lastSeenValue = next
                            valueProperty.set(next)
                            ui.build.flush()
                        }
                    }
                })

                editorView = view

                this@Editor.children.forEach {
                    (it as EditorPlugin).view = view
                }

                onDispose(Disposable { runCatching { view.destroy() } })

                if (initialValue.isBlank() && valueProperty.get().isBlank()) {
                    val next = serializeDoc(view.state.doc)
                    lastSeenValue = next
                    valueProperty.set(next)
                }

                onDispose(
                    valueProperty.observe { v ->
                        if (v == lastSeenValue) return@observe

                        val schema = editorSchema ?: return@observe
                        val plugins = editorPlugins

                        val doc = parseDoc(schema, v)

                        val nextState = EditorState.create(
                            jsObject<EditorStateConfig> {
                                this.schema = schema
                                this.plugins = plugins
                                if (doc != null) {
                                    this.doc = doc
                                }
                            }
                        )

                        view.updateState(nextState)
                        lastSeenValue = v
                    }
                )
            }

        }


    }

    override fun read(): String {
        return valueProperty.get()
    }

    override fun observeValue(listener: (String) -> Unit): Disposable = valueProperty.observe(listener)

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
