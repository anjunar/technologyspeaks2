package core

import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.events.EventListener
import state.ReadOnlyProperty

class DivComponent(node: HTMLDivElement) : Component<HTMLDivElement>(node)
class SpanComponent(node: HTMLSpanElement) : Component<HTMLSpanElement>(node)
class HBoxComponent(node: HTMLDivElement) : Component<HTMLDivElement>(node)
class VBoxComponent(node: HTMLDivElement) : Component<HTMLDivElement>(node)

fun Component<out Element>.text(value: ReadOnlyProperty<String?>) {
    val d = value.observe { node.textContent = it }
    track(d)
}

fun Component<out Element>.className(value: ReadOnlyProperty<String>) {
    val d = value.observe { node.className = it }
    track(d)
}

fun UiScope.div(block: DivComponent.() -> Unit = {}): DivComponent {
    val div = DivComponent(create("div"))
    return mount(div) {
        div.block()
    }
}

fun UiScope.span(block: SpanComponent.() -> Unit = {}): SpanComponent {
    val span = SpanComponent(create("span"))
    return mount(span) {
        span.block()
    }
}

fun UiScope.hbox(block: HBoxComponent.() -> Unit = {}): HBoxComponent {
    val hbox = HBoxComponent(create<HTMLDivElement>("div").also { it.classList.add("hbox") })
    return mount(hbox) {
        hbox.block()
    }
}

fun UiScope.vbox(block: VBoxComponent.() -> Unit = {}): VBoxComponent {
    val vbox = VBoxComponent(create<HTMLDivElement>("div").also { it.classList.add("vbox") })
    return mount(vbox) {
        vbox.block()
    }
}

var Component<out Element>.id: String
    get() = node.id
    set(value) {
        node.id = value
    }

var Component<out Element>.className: String
    get() = node.className
    set(value) {
        node.className = value
    }

var Component<out Element>.text: String?
    get() = node.textContent
    set(value) {
        node.textContent = value
    }

fun Component<out Element>.attr(name: String, value: String) {
    node.setAttribute(name, value)
}

fun Component<out Element>.addEventLister(name: String, value: EventListener) {
    node.addEventListener(name, value)
}
