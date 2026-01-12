package jFx.layout

import jFx.core.AbstractComponent
import jFx.core.DSL
import jFx.core.DSL.ParentScope
import kotlinx.browser.document
import org.w3c.dom.HTMLHRElement

class HorizontalLine(override val ctx: DSL.BuildContext) : AbstractComponent<HTMLHRElement>(), DSL.NodeBuilder<HTMLHRElement> {

    val node by lazy {
        document.createElement("hr") as HTMLHRElement
    }

    override fun build(): HTMLHRElement = node

    companion object {
        fun ParentScope.hr(body: HorizontalLine.(DSL.BuildContext) -> Unit): HorizontalLine {
            val builder = HorizontalLine(this.ctx)
            addNode(builder, body)
            return builder
        }
    }

}