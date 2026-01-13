package jFx2.core.rendering

import jFx2.core.capabilities.DisposeScope
import jFx2.core.capabilities.NodeScope
import jFx2.core.capabilities.UiScope
import jFx2.state.ReadOnlyProperty
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.Node

class ConditionBuilder internal constructor(
    private val scope: NodeScope,
    private val predicate: ReadOnlyProperty<Boolean>
) {
    private var whenTrue: (NodeScope.() -> Unit)? = null
    private var whenFalse: (NodeScope.() -> Unit)? = null

    fun then(block: NodeScope.() -> Unit) { whenTrue = block }
    fun elseDo(block: NodeScope.() -> Unit) { whenFalse = block }

    fun install() {
        val dom = scope.dom
        val build = scope.build
        val render = scope.render
        val outerDispose = scope.dispose

        val host = dom.create<HTMLDivElement>("div")
        dom.attach(scope.parent, host)

        var current: Mount? = null

        fun toFactory(block: (NodeScope.() -> Unit)?): (DisposeScope.() -> Node)? {
            if (block == null) return null

            return {
                val innerUi = UiScope(
                    dom = dom,
                    build = build,
                    render = render,
                    dispose = this
                )

                val container = dom.create<HTMLDivElement>("div")

                val branchScope = NodeScope(
                    ui = innerUi,
                    parent = container,
                    owner = scope.owner,
                    forms = scope.forms
                )
                branchScope.block()

                container
            }
        }

        fun select(v: Boolean) = if (v) toFactory(whenTrue) else toFactory(whenFalse)

        current = render.replace(host, current, select(predicate.get()))

        val sub = predicate.observe { v ->
            current = render.replace(host, current, select(v))
        }

        outerDispose.register(sub)
    }
}

fun NodeScope.condition(predicate: ReadOnlyProperty<Boolean>, block: ConditionBuilder.() -> Unit) {
    val b = ConditionBuilder(this, predicate)
    b.block()
    b.install()
}
