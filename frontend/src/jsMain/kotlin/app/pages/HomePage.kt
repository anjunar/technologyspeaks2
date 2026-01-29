package app.pages

import jFx2.layout.div
import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.style
import jFx2.core.template
import jFx2.forms.editor
import jFx2.forms.editor.plugins.basePlugin
import jFx2.forms.editor.plugins.headingPlugin
import jFx2.forms.form
import org.w3c.dom.HTMLDivElement

class Home(override var node: HTMLDivElement) : Component<HTMLDivElement>() {

    context(scope: NodeScope)
    fun afterBuild() {

        template {
            style {
                height = "100%"
                width = "100%"
            }


            form {
                style {
                    height = "100%"
                    width = "100%"
                }

                editor("test") {
                    style {
                        height = "100%"
                        width = "100%"
                    }

                    basePlugin {  }
                    headingPlugin {  }
                }
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
