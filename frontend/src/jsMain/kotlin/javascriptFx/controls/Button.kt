package javascriptFx.controls

import javascriptFx.core.LifeCycle
import javascriptFx.core.NodeBuilder
import javascriptFx.core.Producer
import kotlinx.browser.document
import org.w3c.dom.HTMLButtonElement

class Button() : NodeBuilder<HTMLButtonElement> {

    val node by lazy { document.createElement("button") as HTMLButtonElement }

    override fun build(): HTMLButtonElement {
        node.textContent = "Button"
        return node
    }

    override val applyValues: MutableList<() -> Unit> = mutableListOf()

    override var lifeCycle: LifeCycle = LifeCycle.Build

    companion object : Producer<Button, HTMLButtonElement>() {
        override fun createBuilder(): Button = Button()
    }

}