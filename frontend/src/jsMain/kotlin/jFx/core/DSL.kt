package jFx.core

import jFx.state.ListProperty
import org.w3c.dom.Node

object DSL {

    @DslMarker
    annotation class JFxDsl

    enum class LifeCycle {
        Build,
        Apply,
        Finished,
        Layout
    }

    @JFxDsl
    interface ElementBuilder<E> {
        fun build(): E

        fun afterBuild() {}

        val applyValues: MutableList<() -> Unit>

        var lifeCycle: LifeCycle

        fun write(action: () -> Unit) {
            if (lifeCycle == LifeCycle.Finished || lifeCycle == LifeCycle.Layout) {
                action()
            } else {
                applyValues.add(action)
            }
        }

        fun <T> read(value: T): T {
            if (lifeCycle == LifeCycle.Finished || lifeCycle == LifeCycle.Layout) {
                return value
            }

            throw IllegalStateException(
                "Cannot read UI properties during the $lifeCycle phase (only allowed in Finished)."
            )
        }
    }

    interface ParentScope {
        fun <T : Node, B : ElementBuilder<T>> addNode(builder: B, body: B.() -> Unit): T
    }

    interface ChildNodeBuilder<C : Node, I : Node> : ElementBuilder<C>, ParentScope {
        val children: ListProperty<ElementBuilder<*>>

        override fun <T : Node, B : ElementBuilder<T>> addNode(builder: B, body: B.() -> Unit): T {
            builder.lifeCycle = LifeCycle.Build
            val node = builder.build()

            builder.body()

            this.add(builder)

            builder.lifeCycle = LifeCycle.Apply
            builder.applyValues.forEach { it() }
            builder.applyValues.clear()

            builder.lifeCycle = LifeCycle.Finished

            if (builder is NodeBuilder<*>) {
                builder.registerLayoutListener()
            }

            return node
        }

        fun add(child: ElementBuilder<*>)
    }

    interface ComponentBuilder<C> : ElementBuilder<C>, ParentScope {
        fun add(child: ElementBuilder<*>)

        override fun <T : Node, B : ElementBuilder<T>> addNode(builder: B, body: B.() -> Unit): T {
            builder.lifeCycle = LifeCycle.Build
            val node = builder.build()
            builder.body()
            this.add(builder)
            builder.lifeCycle = LifeCycle.Apply
            builder.applyValues.forEach { it() }
            builder.applyValues.clear()
            builder.lifeCycle = LifeCycle.Finished
            return node
        }
    }

    interface NodeBuilder<C : Node> : ElementBuilder<C> {
        fun registerLayoutListener() {}
    }

    class DefaultComponentBuilder<C> : ComponentBuilder<C> {
        private val children: MutableList<ElementBuilder<*>> = mutableListOf()

        override val applyValues: MutableList<() -> Unit> = mutableListOf()

        override var lifeCycle: LifeCycle = LifeCycle.Build

        override fun add(child: ElementBuilder<*>) {
            children.add(child)
        }

        override fun build(): C {
            @Suppress("UNCHECKED_CAST")
            return children.first().build() as C
        }
    }

    fun <C> component(body: DefaultComponentBuilder<C>.() -> Unit): C {
        val root = DefaultComponentBuilder<C>()
        root.body()
        return root.build()
    }
}