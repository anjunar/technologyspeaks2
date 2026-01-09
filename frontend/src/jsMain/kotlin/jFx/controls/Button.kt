package jFx.controls

import jFx.core.AbstractComponent
import jFx.core.DSL
import jFx.core.DSL.NodeBuilder
import jFx.core.DSL.ParentScope
import jFx.state.Property
import jFx.util.EventHelper
import kotlinx.browser.document
import org.w3c.dom.HTMLButtonElement

class Button(val ctx: DSL.BuildContext) : AbstractComponent(), NodeBuilder<HTMLButtonElement> {

    val node by lazy {
        val element = document.createElement("button") as HTMLButtonElement

        EventHelper.events(element, {ctx.flushDirty()}, "click")

        textProperty.observe { element.textContent = it }

        element
    }

    override fun build(): HTMLButtonElement {
        return node
    }

    val textProperty = Property("")

    fun textReader(callback : () -> String) {
        ctx.addDirtyComponent(this)
        dirty { node.textContent = callback() }
    }

    var text: String
        get() = read(node.textContent ?: "")
        set(value) = write { node.textContent = value }

    fun onClick(handler: () -> Unit) = write {
        node.onclick = { handler(); null }
    }

    companion object {
        fun ParentScope.button(body: Button.(DSL.BuildContext) -> Unit): Button {
            val builder = Button(ctx)
            addNode(builder, body)
            return builder
        }
    }
}