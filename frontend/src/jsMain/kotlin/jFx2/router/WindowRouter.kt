package jFx2.router

import jFx2.layout.div
import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.capabilities.UiScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.renderComponent
import jFx2.core.dsl.style
import jFx2.core.rendering.foreachAsync
import jFx2.modals.window
import jFx2.state.ListProperty
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLDivElement
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class WindowRouter(override val node: HTMLDivElement, val ui : UiScope, val routes: List<Route>) : Component<HTMLDivElement>() {

    val windows = ListProperty<RouteMatch>()

    context(scope: NodeScope)
    fun afterBuild() {

        foreachAsync(windows, {key -> key.id.toString()}) { state, index ->
            val component = state.route.factory!!()

            window {

                onClose {
                    windows.remove(state)
                }

                div {
                    style {
                        width = "100%"
                        height = "100%"
                    }
                    renderComponent(component)
                }
            }
        }


        fun addRouteToWindows() {
            val resolveRoutes = resolveRoutes(routes, window.location.pathname)
            val routeMatch = resolveRoutes.matches.last()
            windows.add(routeMatch)
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
