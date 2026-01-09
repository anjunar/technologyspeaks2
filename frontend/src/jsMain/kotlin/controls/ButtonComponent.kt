package controls

import core.UiScope
import core.create
import org.w3c.dom.HTMLButtonElement

object Button {

    class Component(node: HTMLButtonElement) : core.Component<HTMLButtonElement>(node)

    fun UiScope.button(block: Component.() -> Unit = {}): Component {
        val div = Component(create("button"))
        return mount(div) {
            div.block()
        }
    }

}

