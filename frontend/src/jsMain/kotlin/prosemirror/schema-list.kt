@file:JsModule("prosemirror-schema-list")
@file:JsNonModule

package prosemirror.schema.list

import orderedmap.OrderedMap
import prosemirror.model.Attrs
import prosemirror.model.NodeRange
import prosemirror.model.NodeSpec
import prosemirror.model.NodeType
import prosemirror.state.Command
import prosemirror.state.Transaction

/**
An ordered list [node spec](https://prosemirror.net/docs/ref/#model.NodeSpec). Has a single
attribute, `order`, which determines the number at which the list
starts counting, and defaults to 1. Represented as an `<ol>`
element.
*/
external val orderedList: NodeSpec
/**
A bullet list node spec, represented in the DOM as `<ul>`.
*/
external val bulletList: NodeSpec
/**
A list item (`<li>`) spec.
*/
external val listItem: NodeSpec
/**
Convenience function for adding list-related node types to a map
specifying the nodes for a schema. Adds
[`orderedList`](https://prosemirror.net/docs/ref/#schema-list.orderedList) as "ordered_list",
[`bulletList`](https://prosemirror.net/docs/ref/#schema-list.bulletList) as "bullet_list", and
[`listItem`](https://prosemirror.net/docs/ref/#schema-list.listItem) as "list_item".

`itemContent` determines the content expression for the list items.
If you want the commands defined in this module to apply to your
list structure, it should have a shape like "paragraph block*" or
"paragraph (ordered_list | bullet_list)*". `listGroup` can be
given to assign a group name to the list node types, for example
"block".
*/
external fun addListNodes(nodes: OrderedMap<NodeSpec>, itemContent: String, listGroup: String? = definedExternally): OrderedMap<NodeSpec>
/**
Returns a command function that wraps the selection in a list with
the given type an attributes. If `dispatch` is null, only return a
value to indicate whether this is possible, but don't actually
perform the change.
*/
external fun wrapInList(listType: NodeType, attrs: Attrs? = definedExternally): Command
/**
Try to wrap the given node range in a list of the given type.
Return `true` when this is possible, `false` otherwise. When `tr`
is non-null, the wrapping is added to that transaction. When it is
`null`, the function only queries whether the wrapping is
possible.
*/
external fun wrapRangeInList(tr: Transaction?, range: NodeRange, listType: NodeType, attrs: Attrs? = definedExternally): Boolean
/**
Build a command that splits a non-empty textblock at the top level
of a list item by also splitting that list item.
*/
external fun splitListItem(itemType: NodeType, itemAttrs: Attrs? = definedExternally): Command
/**
Acts like [`splitListItem`](https://prosemirror.net/docs/ref/#schema-list.splitListItem), but
without resetting the set of active marks at the cursor.
*/
external fun splitListItemKeepMarks(itemType: NodeType, itemAttrs: Attrs? = definedExternally): Command
/**
Create a command to lift the list item around the selection up into
a wrapping list.
*/
external fun liftListItem(itemType: NodeType): Command
/**
Create a command to sink the list item around the selection down
into an inner list.
*/
external fun sinkListItem(itemType: NodeType): Command
