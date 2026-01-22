@file:JsModule("prosemirror-commands")
@file:JsNonModule

package prosemirror.commands

import prosemirror.model.Attrs
import prosemirror.model.MarkType
import prosemirror.model.Node
import prosemirror.model.NodeType
import prosemirror.model.ResolvedPos
import prosemirror.state.Command
import kotlin.js.nativeGetter
import kotlin.js.nativeSetter

/**
Delete the selection, if there is one.
*/
external val deleteSelection: Command
/**
If the selection is empty and at the start of a textblock, try to
reduce the distance between that block and the one before it—if
there's a block directly before it that can be joined, join them.
If not, try to move the selected block closer to the next one in
the document structure by lifting it out of its parent or moving it
into a parent of the previous block. Will use the view for accurate
(bidi-aware) start-of-textblock detection if given.
*/
external val joinBackward: Command
/**
A more limited form of [`joinBackward`](https://prosemirror.net/docs/ref/#commands.joinBackward)
that only tries to join the current textblock to the one before
it, if the cursor is at the start of a textblock.
*/
external val joinTextblockBackward: Command
/**
A more limited form of [`joinForward`](https://prosemirror.net/docs/ref/#commands.joinForward)
that only tries to join the current textblock to the one after
it, if the cursor is at the end of a textblock.
*/
external val joinTextblockForward: Command
/**
When the selection is empty and at the start of a textblock, select
the node before that textblock, if possible. This is intended to be
bound to keys like backspace, after
[`joinBackward`](https://prosemirror.net/docs/ref/#commands.joinBackward) or other deleting
commands, as a fall-back behavior when the schema doesn't allow
deletion at the selected point.
*/
external val selectNodeBackward: Command
/**
If the selection is empty and the cursor is at the end of a
textblock, try to reduce or remove the boundary between that block
and the one after it, either by joining them or by moving the other
block closer to this one in the tree structure. Will use the view
for accurate start-of-textblock detection if given.
*/
external val joinForward: Command
/**
When the selection is empty and at the end of a textblock, select
the node coming after that textblock, if possible. This is intended
to be bound to keys like delete, after
[`joinForward`](https://prosemirror.net/docs/ref/#commands.joinForward) and similar deleting
commands, to provide a fall-back behavior when the schema doesn't
allow deletion at the selected point.
*/
external val selectNodeForward: Command
/**
Join the selected block or, if there is a text selection, the
closest ancestor block of the selection that can be joined, with
the sibling above it.
*/
external val joinUp: Command
/**
Join the selected block, or the closest ancestor of the selection
that can be joined, with the sibling after it.
*/
external val joinDown: Command
/**
Lift the selected block, or the closest ancestor block of the
selection that can be lifted, out of its parent node.
*/
external val lift: Command
/**
If the selection is in a node whose type has a truthy
[`code`](https://prosemirror.net/docs/ref/#model.NodeSpec.code) property in its spec, replace the
selection with a newline character.
*/
external val newlineInCode: Command
/**
When the selection is in a node with a truthy
[`code`](https://prosemirror.net/docs/ref/#model.NodeSpec.code) property in its spec, create a
default block after the code block, and move the cursor there.
*/
external val exitCode: Command
/**
If a block node is selected, create an empty paragraph before (if
it is its parent's first child) or after it.
*/
external val createParagraphNear: Command
/**
If the cursor is in an empty textblock that can be lifted, lift the
block.
*/
external val liftEmptyBlock: Command
/**
Create a variant of [`splitBlock`](https://prosemirror.net/docs/ref/#commands.splitBlock) that uses
a custom function to determine the type of the newly split off block.
*/
external fun splitBlockAs(splitNode: ((node: Node, atEnd: Boolean, `$from`: ResolvedPos) -> SplitBlockAsResult?)? = definedExternally): Command
/**
Split the parent block of the selection. If the selection is a text
selection, also delete its content.
*/
external val splitBlock: Command
/**
Acts like [`splitBlock`](https://prosemirror.net/docs/ref/#commands.splitBlock), but without
resetting the set of active marks at the cursor.
*/
external val splitBlockKeepMarks: Command
/**
Move the selection to the node wrapping the current selection, if
any. (Will not select the document node.)
*/
external val selectParentNode: Command
/**
Select the whole document.
*/
external val selectAll: Command
/**
Moves the cursor to the start of current text block.
*/
external val selectTextblockStart: Command
/**
Moves the cursor to the end of current text block.
*/
external val selectTextblockEnd: Command
/**
Wrap the selection in a node of the given type with the given
attributes.
*/
external fun wrapIn(nodeType: NodeType, attrs: Attrs? = definedExternally): Command
/**
Returns a command that tries to set the selected textblocks to the
given node type with the given attributes.
*/
external fun setBlockType(nodeType: NodeType, attrs: Attrs? = definedExternally): Command
/**
Create a command function that toggles the given mark with the
given attributes. Will return `false` when the current selection
doesn't support that mark. This will remove the mark if any marks
of that type exist in the selection, or add it otherwise. If the
selection is empty, this applies to the [stored
marks](https://prosemirror.net/docs/ref/#state.EditorState.storedMarks) instead of a range of the
document.
*/
external fun toggleMark(markType: MarkType, attrs: Attrs? = definedExternally, options: ToggleMarkOptions? = definedExternally): Command
/**
Wrap a command so that, when it produces a transform that causes
two joinable nodes to end up next to each other, those are joined.
Nodes are considered joinable when they are of the same type and
when the `isJoinable` predicate returns true for them or, if an
array of strings was passed, if their node type name is in that
array.
*/
external fun autoJoin(command: Command, isJoinable: dynamic): Command
/**
Combine a number of command functions into a single function (which
calls them one by one until one returns true).
*/
external fun chainCommands(vararg commands: Command): Command
/**
A basic keymap containing bindings not specific to any schema.
Binds the following keys (when multiple commands are listed, they
are chained with [`chainCommands`](https://prosemirror.net/docs/ref/#commands.chainCommands)):

* **Enter** to `newlineInCode`, `createParagraphNear`, `liftEmptyBlock`, `splitBlock`
* **Mod-Enter** to `exitCode`
* **Backspace** and **Mod-Backspace** to `deleteSelection`, `joinBackward`, `selectNodeBackward`
* **Delete** and **Mod-Delete** to `deleteSelection`, `joinForward`, `selectNodeForward`
* **Mod-Delete** to `deleteSelection`, `joinForward`, `selectNodeForward`
* **Mod-a** to `selectAll`
*/
external val pcBaseKeymap: CommandKeymap
/**
A copy of `pcBaseKeymap` that also binds **Ctrl-h** like Backspace,
**Ctrl-d** like Delete, **Alt-Backspace** like Ctrl-Backspace, and
**Ctrl-Alt-Backspace**, **Alt-Delete**, and **Alt-d** like
Ctrl-Delete.
*/
external val macBaseKeymap: CommandKeymap
/**
Depending on the detected platform, this will hold
[`pcBasekeymap`](https://prosemirror.net/docs/ref/#commands.pcBaseKeymap) or
[`macBaseKeymap`](https://prosemirror.net/docs/ref/#commands.macBaseKeymap).
*/
external val baseKeymap: CommandKeymap

external interface CommandKeymap {
    @nativeGetter
    operator fun get(key: String): Command?

    @nativeSetter
    operator fun set(key: String, value: Command)
}

external interface SplitBlockAsResult {
    val type: NodeType
    var attrs: Attrs?
}

external interface ToggleMarkOptions {
    /**
    Controls whether, when part of the selected range has the mark
    already and part doesn't, the mark is removed (`true`, the
    default) or added (`false`).
    */
    var removeWhenPresent: Boolean?

    /**
    When set to false, this will prevent the command from acting on
    the content of inline nodes marked as
    [atoms](https://prosemirror.net/docs/ref/#model.NodeSpec.atom) that are completely covered by a
    selection range.
    */
    var enterInlineAtoms: Boolean?

    /**
    By default, this command doesn't apply to leading and trailing
    whitespace in the selection. Set this to `true` to change that.
    */
    var includeWhitespace: Boolean?
}
