@file:JsModule("prosemirror-history")
@file:JsNonModule

package prosemirror

import kotlin.js.*

/**
Set a flag on the given transaction that will prevent further steps
from being appended to an existing history event (so that they
require a separate undo command to undo).
*/
external fun closeHistory(vararg args: dynamic): dynamic

external interface HistoryOptions

    /**
    The amount of history events that are collected before the
    oldest events are discarded. Defaults to 100.
    */
    /**
    The delay between changes after which a new group should be
    started. Defaults to 500 (milliseconds). Note that when changes
    aren't adjacent, a new group is always started.
    */
/**
Returns a plugin that enables the undo history for an editor. The
plugin will track undo and redo stacks, which can be used with the
[`undo`](https://prosemirror.net/docs/ref/#history.undo) and [`redo`](https://prosemirror.net/docs/ref/#history.redo) commands.

You can set an `"addToHistory"` [metadata
property](https://prosemirror.net/docs/ref/#state.Transaction.setMeta) of `false` on a transaction
to prevent it from being rolled back by undo.
*/
external fun history(vararg args: dynamic): dynamic

/**
A command function that undoes the last change, if any.
*/
external val undo: dynamic

/**
A command function that redoes the last undone change, if any.
*/
external val redo: dynamic

/**
A command function that undoes the last change. Don't scroll the
selection into view.
*/
external val undoNoScroll: dynamic

/**
A command function that redoes the last undone change. Don't
scroll the selection into view.
*/
external val redoNoScroll: dynamic

/**
The amount of undoable events available in a given state.
*/
external fun undoDepth(vararg args: dynamic): dynamic

/**
The amount of redoable events available in a given editor state.
*/
external fun redoDepth(vararg args: dynamic): dynamic
