@file:Suppress("unused")

package dsl

import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.Node

@DslMarker
annotation class FxDsl


interface HasNode<N : Node> {
    val node: N
}

open class Component<E : Element>(
    final override val node: E
) : HasNode<E> {

    open fun build() {}
    open fun apply() {}
    open fun hook() {}
}

@FxDsl
class UiScope internal constructor(
    internal val parent: Element
) {
    fun <C : HasNode<out Node>> attach(child: C): C {
        parent.appendChild(child.node)
        return child
    }

    internal fun <C : Component<out Element>> mount(
        child: C,
        block: (UiScope.() -> Unit)?
    ): C {
        child.build()
        parent.appendChild(child.node)
        if (block != null) UiScope(child.node).block()
        child.apply()
        child.hook()
        return child
    }
}

fun mountInto(parent: Element, block: UiScope.() -> Unit): UiScope =
    UiScope(parent).apply(block)

private inline fun <reified E : Element> create(tag: String): E =
    document.createElement(tag) as E

/* =========================
 * Standard HTML Components
 * ========================= */
class Div(node: HTMLDivElement) : Component<HTMLDivElement>(node)
class Span(node: HTMLSpanElement) : Component<HTMLSpanElement>(node)
class HBox(node: HTMLDivElement) : Component<HTMLDivElement>(node) // nur semantisch
class VBox(node: HTMLDivElement) : Component<HTMLDivElement>(node) // nur semantisch

fun UiScope.div(block: Div.() -> Unit = {}): Div {
    val div = Div(create("div"))
    return mount(div) {
        div.block()  // Hier wird der Block auf der Div-Instanz ausgeführt
    }
}

fun UiScope.span(block: Span.() -> Unit = {}): Span {
    val span = Span(create("span"))
    return mount(span) {
        span.block()
    }
}

fun UiScope.hbox(block: HBox.() -> Unit = {}): HBox {
    val hbox = HBox(create<HTMLDivElement>("div").also { it.classList.add("hbox") })
    return mount(hbox) {
        hbox.block()
    }
}

fun UiScope.vbox(block: VBox.() -> Unit = {}): VBox {
    val vbox = VBox(create<HTMLDivElement>("div").also { it.classList.add("vbox") })
    return mount(vbox) {
        vbox.block()
    }
}

fun Component<out Element>.id(value: String) {
    node.id = value
}

fun Component<out Element>.className(vararg names: String) {
    node.classList.add(*names)
}

fun Component<out Element>.attr(name: String, value: String) {
    node.setAttribute(name, value)
}

fun Component<out Element>.text(value: String) {
    node.textContent = value
}