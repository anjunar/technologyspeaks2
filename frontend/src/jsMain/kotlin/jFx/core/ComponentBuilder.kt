package jFx.core

import org.w3c.dom.Node

interface ComponentBuilder<C> : ElementBuilder<C>, ParentScope {
    fun add(child: ElementBuilder<*>)

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

            builder.afterBuild()

            return node
        } finally {
            ctx.pop(builder)
            if (ctx.isIdle()) {
                ctx.flushAfterTreeBuilt()
            }
        }
    }
}
