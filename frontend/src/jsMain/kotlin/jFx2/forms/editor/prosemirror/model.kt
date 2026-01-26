@file:JsModule("prosemirror-model")
@file:JsNonModule

package jFx2.forms.editor.prosemirror

import org.w3c.dom.Node as DomNode

external class Schema(spec: SchemaSpec = definedExternally) {
    val nodes: Any
    val marks: Any
}

external interface SchemaSpec {
    var nodes: Any
    var marks: Any
    var topNode: String?
}

external class Node {
    val type: NodeType
    val attrs: dynamic

    val marks: Array<Mark>

    fun nodeAt(pos: Int): Node?

    fun nodesBetween(
        from: Int,
        to: Int,
        f: (node: Node, pos: Int, parent: Node, index: Int) -> Boolean,
        startPos: Int = definedExternally,
    )
}

external class NodeType {
    val name: String
    fun create(
        attrs: dynamic = definedExternally,
        content: Fragment = definedExternally,
        marks: Array<Mark> = definedExternally
    ): Node
}

external class Mark {
    val type: MarkType
    val attrs: dynamic
}

external class MarkType {
    val name: String
    fun isInSet(set: Array<Mark>): Mark?
}

external class Fragment {
    companion object {
        fun from(node: Node): Fragment
    }
}

external class Slice(content: Fragment, openStart: Int, openEnd: Int)

external class ResolvedPos {
    val pos: Int
    val parent: Node
    fun marks(): Array<Mark>
}

external class DOMParser private constructor() {
    fun parse(dom: DomNode, options: ParseOptions = definedExternally): Node

    companion object {
        fun fromSchema(schema: Schema): DOMParser
    }
}

external interface ParseOptions {
    var preserveWhitespace: dynamic /* Boolean | "full" */
}

external class DOMSerializer private constructor() {
    fun serializeFragment(fragment: Fragment, options: dynamic = definedExternally): DomNode

    companion object {
        fun fromSchema(schema: Schema): DOMSerializer
    }
}

external class NodeSelection : Selection {
    val node: Node

    companion object {
        fun create(doc: Node, from: Int): NodeSelection
    }
}

external object TextSelection