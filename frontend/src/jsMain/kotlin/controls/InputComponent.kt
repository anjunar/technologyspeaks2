package controls

import core.Component
import core.UiScope
import core.create
import core.track
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener
import state.Property
import state.ReadOnlyProperty

class InputComponent(node : HTMLInputElement) : Component<HTMLInputElement>(node)

fun UiScope.input(block: InputComponent.() -> Unit = {}): InputComponent {
    val vbox = InputComponent(create("input"))
    return mount(vbox) {
        vbox.block()
    }
}

var InputComponent.placeholder: String
    get() = node.placeholder
    set(value) {
        node.placeholder = value
    }

fun InputComponent.placeholder(value: ReadOnlyProperty<String>) {
    val d = value.observe { node.placeholder = it }
    track(d)
}

fun InputComponent.value(model: Property<String>) {
    val d1 = model.observe { v ->
        if (node.value != v) node.value = v
    }
    track(d1)

    val listener = EventListener { _: Event ->
        val v = node.value
        if (model.get() != v) model.set(v)
    }
    node.addEventListener("input", listener)
    track { node.removeEventListener("input", listener) }
}

fun InputComponent.checked(model: Property<Boolean>) {
    val d1 = model.observe { v ->
        if (node.checked != v) node.checked = v
    }
    track(d1)

    val listener = EventListener { _: Event ->
        val v = node.checked
        if (model.get() != v) model.set(v)
    }
    node.addEventListener("change", listener)
    track { node.removeEventListener("change", listener) }
}

fun InputComponent.onInput(setter: (String) -> Unit) {
    val listener = EventListener { setter(node.value) }
    node.addEventListener("input", listener)
    track { node.removeEventListener("input", listener) }
}