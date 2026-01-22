@file:JsModule("prosemirror-schema-basic")
@file:JsNonModule

package prosemirror.schema.basic

import prosemirror.model.MarkSpec
import prosemirror.model.NodeSpec
import prosemirror.model.Schema

/**
[Specs](https://prosemirror.net/docs/ref/#model.NodeSpec) for the nodes defined in this schema.
*/
external val nodes: SchemaBasicNodes
/**
[Specs](https://prosemirror.net/docs/ref/#model.MarkSpec) for the marks in the schema.
*/
external val marks: SchemaBasicMarks
/**
This schema roughly corresponds to the document schema used by
[CommonMark](http://commonmark.org/), minus the list elements,
which are defined in the [`prosemirror-schema-list`](https://prosemirror.net/docs/ref/#schema-list)
module.

To reuse elements from this schema, extend or read from its
`spec.nodes` and `spec.marks` [properties](https://prosemirror.net/docs/ref/#model.Schema.spec).
*/
external val schema: Schema<dynamic, dynamic>

external interface SchemaBasicNodes {
    /**
    NodeSpec The top level document node.
    */
    val doc: NodeSpec
    /**
    A plain paragraph textblock. Represented in the DOM
    as a `<p>` element.
    */
    val paragraph: NodeSpec
    /**
    A blockquote (`<blockquote>`) wrapping one or more blocks.
    */
    val blockquote: NodeSpec
    /**
    A horizontal rule (`<hr>`).
    */
    val horizontal_rule: NodeSpec
    /**
    A heading textblock, with a `level` attribute that
    should hold the number 1 to 6. Parsed and serialized as `<h1>` to
    `<h6>` elements.
    */
    val heading: NodeSpec
    /**
    A code listing. Disallows marks or non-text inline
    nodes by default. Represented as a `<pre>` element with a
    `<code>` element inside of it.
    */
    val code_block: NodeSpec
    /**
    The text node.
    */
    val text: NodeSpec
    /**
    An inline image (`<img>`) node. Supports `src`,
    `alt`, and `href` attributes. The latter two default to the empty
    string.
    */
    val image: NodeSpec
    /**
    A hard line break, represented in the DOM as `<br>`.
    */
    val hard_break: NodeSpec
}

external interface SchemaBasicMarks {
    /**
    A link. Has `href` and `title` attributes. `title`
    defaults to the empty string. Rendered and parsed as an `<a>`
    element.
    */
    val link: MarkSpec
    /**
    An emphasis mark. Rendered as an `<em>` element. Has parse rules
    that also match `<i>` and `font-style: italic`.
    */
    val em: MarkSpec
    /**
    A strong mark. Rendered as `<strong>`, parse rules also match
    `<b>` and `font-weight: bold`.
    */
    val strong: MarkSpec
    /**
    Code font mark. Represented as a `<code>` element.
    */
    val code: MarkSpec
}
