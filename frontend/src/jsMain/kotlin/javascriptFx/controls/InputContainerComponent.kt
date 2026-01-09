package javascriptFx.controls

import javascriptFx.core.Component
import javascriptFx.core.UiScope
import javascriptFx.core.children
import javascriptFx.core.create
import javascriptFx.core.mountInto
import javascriptFx.core.span
import javascriptFx.core.text
import javascriptFx.core.track
import javascriptFx.core.vbox
import javascriptFx.state.ReadOnlyProperty
import javascriptFx.state.computed
import javascriptFx.state.map
import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement

class InputContainerComponent(node : HTMLDivElement) : Component<HTMLDivElement>(node) {

    override fun build() {
        node.classList.add("input-container")

        mountInto(this) {
            vbox {
                span { text = "Input:" }
            }
        }

    }

    override fun onChildrenAttached(children: List<Component<*>>) {
        val inp = children.first() as InputComponent

        val empty: ReadOnlyProperty<Boolean> = inp.value.map { it.isBlank() }
        val invalid: ReadOnlyProperty<Boolean> = computed(inp.dirty, empty) { d, e -> d && e }

        track(inp.dirty.observe { d ->
            node.classList.toggle("is-dirty", d)
        })

        track(empty.observe { e ->
            node.classList.toggle("is-empty", e)
        })

        track(invalid.observe { inv ->
            node.classList.toggle("is-invalid", inv)
        })
    }
}

fun UiScope.inputContainer(block: InputContainerComponent.() -> Unit = {}): InputContainerComponent {
    val containerComponent = InputContainerComponent(create("div"))
    return mount(containerComponent) {
        containerComponent.block()
    }
}
