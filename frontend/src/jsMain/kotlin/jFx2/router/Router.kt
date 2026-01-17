package jFx2.router

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.capabilities.UiScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.rendering.dynamicOutlet
import jFx2.state.Property
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLDivElement

class Router(override val node: HTMLDivElement, val ui: UiScope, val routes: List<Route<*>>) :
    Component<HTMLDivElement>() {

    val content = Property<Component<*>?>(null)

    context(scope: NodeScope)
    fun afterBuild() {

        dynamicOutlet(content)

        fun extracted() {
            val resolveRoutes = resolveRoutes(routes, window.location.pathname)
            val routeMatch = resolveRoutes.matches.last()

            val scope = CoroutineScope(SupervisorJob())

            scope.launch {
                content.set(routeMatch.route.factory())
                ui.build.flush()
            }
        }

        window.addEventListener("popstate", {

            extracted()

        })

        extracted()
    }

}

context(scope: NodeScope)
fun router(routes: List<Route<*>>, block: context(NodeScope) Router.() -> Unit = {}): Router {
    val el = scope.create<HTMLDivElement>("div")
    el.classList.add("router")
    val c = Router(el, scope.ui, routes)
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
