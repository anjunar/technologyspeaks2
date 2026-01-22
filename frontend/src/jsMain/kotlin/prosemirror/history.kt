@file:JsModule("prosemirror-history")
@file:JsNonModule

package prosemirror.history

import prosemirror.state.Command
import prosemirror.state.EditorState
import prosemirror.state.Plugin
import prosemirror.state.Transaction

/**
Set a flag on the given transaction that will prevent further steps
from being appended to an existing history event (so that they
require a separate undo command to undo).
*/
external fun closeHistory(tr: Transaction): Transaction
external interface HistoryOptions {
    /**
    The amount of history events that are collected before the
    oldest events are discarded. Defaults to 100.
    */
    var depth: Double?
    /**
    The delay between changes after which a new group should be
    started. Defaults to 500 (milliseconds). Note that when changes
    aren't adjacent, a new group is always started.
    */
    var newGroupDelay: Double?
}
/**
Returns a plugin that enables the undo history for an editor. The
plugin will track undo and redo stacks, which can be used with the
[`undo`](https://prosemirror.net/docs/ref/#history.undo) and [`redo`](https://prosemirror.net/docs/ref/#history.redo) commands.

You can set an "addToHistory" [metadata
property](https://prosemirror.net/docs/ref/#state.Transaction.setMeta) of `false` on a transaction
to prevent it from being rolled back by undo.
*/
external fun history(config: HistoryOptions? = definedExternally): Plugin
/**
A command function that undoes the last change, if any.
*/
external val undo: Command
/**
A command function that redoes the last undone change, if any.
*/
external val redo: Command
/**
A command function that undoes the last change. Don't scroll the
selection into view.
*/
external val undoNoScroll: Command
/**
A command function that redoes the last undone change. Don't
scroll the selection into view.
*/
external val redoNoScroll: Command
/**
The amount of undoable events available in a given state.
*/
external fun undoDepth(state: EditorState): Any?
/**
The amount of redoable events available in a given editor state.
*/
external fun redoDepth(state: EditorState): Any?
