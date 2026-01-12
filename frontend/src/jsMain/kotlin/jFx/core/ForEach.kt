package jFx.core

import jFx.state.Disposable
import jFx.state.ListProperty
import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.Node

object ForEach {

    fun <T> ParentScope.foreach(
        items: ListProperty<T>,
        body: ParentScope.(T) -> Unit
    ) {
        addNode(ForEachHost(this.ctx, items, body), body = {})
    }

    private class ForEachHost<T>(
        override val ctx: BuildContext,
        private val items: ListProperty<T>,
        private val body: ParentScope.(T) -> Unit
    ) : AbstractComponent<HTMLDivElement>() {

        private val host: HTMLDivElement by lazy {
            document.createElement("div") as HTMLDivElement
        }

        private var subscription: Disposable? = null
        private var initialized = false

        private val mountedBuilders: MutableList<ElementBuilder<*>> = mutableListOf()

        private val capturedScope: Scope = ctx.scope

        override fun build(): HTMLDivElement {
            if (!initialized) {
                initialized = true
                subscription = items.observe { list ->
                    host.update(list)
                }
            }
            return host
        }

        private fun HTMLDivElement.update(items: List<T>) {
            clearMounted()
            if (items.isEmpty()) return

            val prevScope = ctx.scope
            ctx.scope = capturedScope
            try {
                val hostScope = object : ParentScope {
                    override val ctx: BuildContext = this@ForEachHost.ctx

                    override fun <N : Node, B : ElementBuilder<N>> addNode(
                        builder: B,
                        body: B.(BuildContext) -> Unit
                    ): N {
                        val ctx = this.ctx

                        ctx.push(builder)
                        try {
                            builder.lifeCycle = LifeCycle.Build
                            val node = builder.build()

                            builder.body(ctx)

                            mountedBuilders.add(builder)
                            this@update.appendChild(node)

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
                }

                items.forEach { item ->
                    hostScope.body(item)
                }
            } finally {
                ctx.scope = prevScope
            }
        }

        private fun clearMounted() {
            while (host.firstChild != null) host.removeChild(host.firstChild!!)

            mountedBuilders.forEach { b ->
                try { b.dispose() } catch (_: Throwable) { }
            }
            mountedBuilders.clear()
        }

        override fun dispose() {
            subscription?.invoke()
            subscription = null
            clearMounted()
            super.dispose()
        }
    }

}