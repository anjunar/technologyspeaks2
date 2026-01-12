package jFx2.core.capabilities

import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.Text

interface DomScope {
    val root: Element

    fun <E : Element> create(tag: String): E =
        document.createElement(tag).unsafeCast<E>()

    fun attach(parent: Node, child: Node) {
        parent.appendChild(child)
    }

    fun detach(node: Node) {
        node.parentNode?.removeChild(node)
    }

    fun clear(parent: Node) {
        while (parent.firstChild != null) parent.removeChild(parent.firstChild!!)
    }

    fun textNode(value: String): Text = document.createTextNode(value)
}