package app.pages

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.codegen.JfxComponentBuilder
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.style
import jFx2.core.template
import jFx2.router.PageInfo
import org.w3c.dom.HTMLDivElement

@JfxComponentBuilder(classes = ["home-page"])
class HomePage(override var node: HTMLDivElement) : Component<HTMLDivElement>(), PageInfo {

    override val name: String = "Home"
    override val width: Int = -1
    override val height: Int = -1
    override val resizable: Boolean = false
    override var close: () -> Unit = {}

    context(scope: NodeScope)
    fun afterBuild() {

        template {
            style {
                height = "100%"
                width = "100%"
            }
        }


    }
}