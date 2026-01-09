package javascriptFx.controls

import javascriptFx.core.Component
import javascriptFx.core.UiScope
import javascriptFx.core.create
import javascriptFx.core.track
import javascriptFx.state.Property
import javascriptFx.state.ReadOnlyProperty
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener

class InputComponent(node : HTMLInputElement) : Component<HTMLInputElement>(node) {

    val value: Property<String> = Property("")
    val dirty: Property<Boolean> = Property(false)

    override fun hook() {
        node.value = value.get()

        val onInput = EventListener {
            val v = node.value
            if (value.get() != v) value.set(v)
            if (!dirty.get()) dirty.set(true)
        }
        node.addEventListener("input", onInput)

        val d1 = value.observe { v ->
            if (node.value != v) node.value = v
        }

        track { node.removeEventListener("input", onInput) }
        track(d1)
    }
}

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