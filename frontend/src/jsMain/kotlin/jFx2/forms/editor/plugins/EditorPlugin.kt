package jFx2.forms.editor.plugins

import jFx2.forms.editor.prosemirror.EditorView
import jFx2.forms.editor.prosemirror.Plugin

interface EditorPlugin {

    var view : EditorView

    fun plugin() : Plugin<*>

}