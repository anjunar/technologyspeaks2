@file:Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")

package jFx2.forms

import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.registerField
import jFx2.core.template
import jFx2.forms.editor.EditorNode
import jFx2.forms.editor.plugins.EditorPlugin
import jFx2.forms.editor.prosemirror.DOMSerializer
import jFx2.forms.editor.prosemirror.Schema
import jFx2.forms.editor.prosemirror.addListNodes
import jFx2.layout.div
import jFx2.state.Disposable
import jFx2.state.Property
import org.w3c.dom.HTMLDivElement
import jFx2.forms.editor.prosemirror.schema as basicSchema
import kotlin.js.unsafeCast

/**
 * Read-only editor renderer.
 *
 * Uses plugin `nodeSpec`s (same mechanism as [Editor]) but does **not** use `prosemirror-view` / `EditorView`.
 */
@Suppress("CAST_NEVER_SUCCEEDS")
class EditorView(override val node: HTMLDivElement) : FormField<EditorNode?, HTMLDivElement>() {

    val valueProperty = Property<EditorNode?>(null)

    private var editorSchema: Schema? = null
    private var domSerializer: DOMSerializer? = null

    private fun parseDoc(schema: Schema, value: EditorNode?): jFx2.forms.editor.prosemirror.Node? {
        return runCatching {
            schema.asDynamic().nodeFromJSON(value).unsafeCast<jFx2.forms.editor.prosemirror.Node>()
        }.getOrNull()
    }

    private fun ensureSchema(): Schema {
        val existing = editorSchema
        if (existing != null) return existing

        val specs: dynamic = {}
        this@EditorView.children.forEach {
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

        editorSchema = customSchema
        domSerializer = DOMSerializer.fromSchema(customSchema)
        return customSchema
    }

    private fun clear(node: HTMLDivElement) {
        while (node.firstChild != null) {
            node.removeChild(node.firstChild!!)
        }
    }

    private fun renderInto(mount: HTMLDivElement, value: EditorNode?) {
        val schema = ensureSchema()
        val doc = parseDoc(schema, value)

        clear(mount)
        if (doc == null) return

        val serializer = domSerializer ?: DOMSerializer.fromSchema(schema).also { domSerializer = it }
        mount.appendChild(serializer.serializeFragment(doc.content))
    }

    context(scope: NodeScope)
    fun afterBuild() {
        template {
            div {
                val mount = this@div.node
                mount.classList.add("ProseMirror")
                mount.setAttribute("contenteditable", "false")

                val initialValue = valueProperty.get()
                renderInto(mount, initialValue)

                var lastSeenValue = initialValue
                onDispose(
                    valueProperty.observe { v ->
                        if (v == lastSeenValue) return@observe
                        renderInto(mount, v)
                        lastSeenValue = v
                    }
                )
            }
        }
    }

    override fun read(): EditorNode? = valueProperty.get()

    override fun observeValue(listener: (EditorNode?) -> Unit): Disposable = valueProperty.observe(listener)
}

context(scope: NodeScope)
fun editorView(name: String, block: context(NodeScope) EditorView.() -> Unit = {}): EditorView {
    val el = scope.create<HTMLDivElement>("div").also {
        it.classList.add("editor-view")
    }
    val c = EditorView(el)
    scope.attach(c)

    registerField(name, c)

    val childScope = scope.fork(parent = c.node, owner = c, ctx = scope.ctx, insertPoint = ElementInsertPoint(c.node))
    block(childScope, c)

    scope.ui.build.afterBuild {
        with(childScope) {
            c.afterBuild()
        }
    }

    return c
}

