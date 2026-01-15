package jFx2.controls

import jFx2.core.capabilities.NodeScope
import kotlinx.browser.document

context(scope: NodeScope)
fun text(value: () -> String) {
    // simplest text node handling: re-create on flush
    val tn = document.createTextNode(value())
    scope.parent.appendChild(tn)
    // You can make this reactive later with your RenderScope; kept minimal here.
}
