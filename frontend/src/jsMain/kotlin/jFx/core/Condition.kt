package jFx.core

import jFx.state.Disposable
import jFx.state.ReadOnlyProperty
import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.Node

object Condition {

    fun ParentScope.condition(
        predicate: () -> Boolean,
        body: ParentScope.() -> Unit
    ) {
        addNode(ConditionReaderHost(ctx, predicate, body), body = {})
    }

    private class ConditionReaderHost(
        override val ctx: BuildContext,
        private val predicate: () -> Boolean,
        private val body: ParentScope.() -> Unit
    ) : AbstractComponent<HTMLDivElement>() {

        private val host: HTMLDivElement by lazy {
            document.createElement("div") as HTMLDivElement
        }

        private val mountedBuilders: MutableList<ElementBuilder<*>> = mutableListOf()

        private val capturedScope: Scope = ctx.scope

        override fun build(): HTMLDivElement {
            ctx.addDirtyComponent(this)

            dirty {
                host.update(predicate())
            }
            write {
                host.update(predicate())
            }
            return host
        }

        private fun HTMLDivElement.update(visible: Boolean) {
            clearMounted()
            if (!visible) return

            val prevScope = ctx.scope
            ctx.scope = capturedScope
            try {
                val hostScope = object : ParentScope {
                    override val ctx: BuildContext = this@ConditionReaderHost.ctx

                    override fun <T : Node, B : ElementBuilder<T>> addNode(builder: B, body: B.(BuildContext) -> Unit): T {
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

                hostScope.body()
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
            clearMounted()
            super.dispose()
        }
    }


    fun ParentScope.condition(
        predicate: ReadOnlyProperty<Boolean>,
        body: ParentScope.() -> Unit
    ) {
        addNode(ConditionHost(this.ctx, predicate, body), body = {})
    }

    private class ConditionHost(
        override val ctx: BuildContext,
        private val predicate: ReadOnlyProperty<Boolean>,
        private val body: ParentScope.() -> Unit
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
                subscription = predicate.observe { visible ->
                    host.update(visible == true)
                }
            }
            return host
        }

        private fun HTMLDivElement.update(visible: Boolean) {
            clearMounted()
            if (!visible) return

            val prevScope = ctx.scope
            ctx.scope = capturedScope
            try {
                val hostScope = object : ParentScope {
                    override val ctx: BuildContext = this@ConditionHost.ctx

                    override fun <T : Node, B : ElementBuilder<T>> addNode(
                        builder: B,
                        body: B.(BuildContext) -> Unit
                    ): T {
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

                hostScope.body()
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