package jFx2.router

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.capabilities.UiScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.modals.ViewPort
import jFx2.modals.WindowConf
import jFx2.state.JobRegistry
import kotlinx.browser.window
import org.w3c.dom.HTMLDivElement
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class WindowRouter(override val node: HTMLDivElement, val ui : UiScope, val routes: List<Route>) : Component<HTMLDivElement>() {

    context(scope: NodeScope)
    fun afterBuild() {

        fun addRouteToWindows() {

            JobRegistry.instance.launch("Router", "Router") {
                val resolveRoutes = resolveRoutes(routes, window.location.pathname)
                val routeMatch = resolveRoutes.matches.last()
                val component = routeMatch.route.factory!!(routeMatch.params)
                val page = component as PageInfo
                ViewPort.addWindow(WindowConf(page.name, {component}, resizable = page.resizable))
            }

        }

        window.addEventListener("popstate", {
            addRouteToWindows()
        })

        addRouteToWindows()


    }

}

context(scope: NodeScope)
fun windowRouter(routes: List<Route>, block: context(NodeScope) WindowRouter.() -> Unit = {}): WindowRouter {
    val el = scope.create<HTMLDivElement>("div")
    el.classList.add("router")
    val c = WindowRouter(el, scope.ui, routes)
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
