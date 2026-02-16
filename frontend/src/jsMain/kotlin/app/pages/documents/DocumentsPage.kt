package app.pages.documents

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.style
import jFx2.core.template
import jFx2.forms.editor
import jFx2.forms.editor.plugins.basePlugin
import jFx2.forms.editor.plugins.headingPlugin
import jFx2.forms.editor.plugins.imagePlugin
import jFx2.forms.editor.plugins.listPlugin
import jFx2.forms.editor.plugins.linkPlugin
import jFx2.forms.form
import jFx2.router.PageInfo
import org.w3c.dom.HTMLDivElement

class Home(override var node: HTMLDivElement) : Component<HTMLDivElement>(), PageInfo {

    override val name: String = "Documents"
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

context(scope: NodeScope)
fun homePage(block: context(NodeScope) Home.() -> Unit = {}): Home {
    val el = scope.create<HTMLDivElement>("div")
    el.classList.add("home-page")
    val c = Home(el)
    scope.attach(c)

    val childScope = scope.fork(parent = c.node, owner = c, ctx = scope.ctx, ElementInsertPoint(c.node))

    scope.ui.build.afterBuild {
        with(childScope) {
            c.afterBuild()
        }
    }

    block(childScope, c)

    return c
}
