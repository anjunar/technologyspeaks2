package jFx2.forms.editor.prosemirror

fun Schema.nodeType(name: String): NodeType? =
    (nodes.asDynamic()[name] as NodeType?)

fun Node.attrInt(name: String): Int? {
    return when (val v = attrs[name]) {
        is Int -> v
        is Number -> v.toInt()
        else -> null
    }
}