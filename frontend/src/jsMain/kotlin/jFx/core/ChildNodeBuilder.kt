package jFx.core

import jFx.state.ListProperty
import org.w3c.dom.Node

interface ChildNodeBuilder<C : Node, I : Node> : ElementBuilder<C>, ParentScope {

    val children: ListProperty<ElementBuilder<*>>

    override fun <T : Node, B : ElementBuilder<T>> addNode(builder: B, body: B.(BuildContext) -> Unit): T {
        val ctx = this.ctx

        ctx.push(builder)
        try {
            builder.lifeCycle = LifeCycle.Build
            val node = builder.build()

            builder.body(ctx)

            this.add(builder)

            builder.lifeCycle = LifeCycle.Apply
            builder.applyValues.forEach { it() }
            builder.applyValues.clear()

            builder.lifeCycle = LifeCycle.Finished

            if (builder is NodeBuilder<*>) {
                builder.registerLayoutListener()
            }

            builder.afterBuild()

            return node
        } finally {
            ctx.pop(builder)
            if (ctx.isIdle()) {
                ctx.flushAfterTreeBuilt()
            }
        }
    }

    fun add(child: ElementBuilder<*>)
}
