package javascriptFx.core

import javascriptFx.state.ListProperty
import org.w3c.dom.Node

enum class LifeCycle {
    Build,
    Hook,
    Apply,
    Bind,
    Finished,
    Layout
}

class BuildContext {
    val stack: ArrayDeque<ElementBuilder<*>> = ArrayDeque()
}

class Ref<B>(var value: B? = null)

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

interface ChildBuilder<C> {
    fun add(child: ElementBuilder<*>)
}

interface ChildNodeBuilder<C : Node, I> : ElementBuilder<C>, ChildBuilder<C> {
    val children: ListProperty<ElementBuilder<*>>
    val fxObservableList: ListProperty<I>
}

interface ChildElementBuilder<C, I> : ElementBuilder<C>, ChildBuilder<C> {
    val children: ListProperty<ElementBuilder<*>>
    val fxObservableList: ListProperty<I>
}

interface NodeBuilder<C : Node> : ElementBuilder<C> {
    fun registerLayoutListener() {}
}

interface ComponentBuilder<C> : ElementBuilder<C> {
    fun add(child: ElementBuilder<*>)
}

interface GridPaneChild {
    val gridPaneX: Int
    val gridPaneY: Int
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

object KotlinDSL {
    fun <C> component(body: ElementBuilder<C>.(BuildContext) -> Unit): C {
        val ctx = BuildContext()
        val root = DefaultComponentBuilder<C>()

        root.body(ctx)

        val node = root.build()

        ctx.stack.forEach { builder ->
            builder.afterBuild()
        }

        return node
    }

    fun <T, B : ElementBuilder<out T>> createBuilder(
        ref: Ref<B>,
        construct: B,
        ctx: BuildContext,
        body: B.(BuildContext) -> Unit
    ): B {
        val builder = construct

        ctx.stack.addLast(builder)
        ref.value = builder
        builder.lifeCycle = LifeCycle.Build

        builder.build()

        builder.body(ctx)

        builder.lifeCycle = LifeCycle.Apply
        builder.applyValues.forEach { action -> action() }
        builder.applyValues.clear()

        builder.lifeCycle = LifeCycle.Bind
//        bindChildren(builder)

        return builder
    }

    fun <T, B : ElementBuilder<out T>> create(
        ref: Ref<B>,
        construct: B,
        ctx: BuildContext,
        parent: ElementBuilder<*>,
        body: B.(BuildContext) -> Unit
    ): T {
        val builder = construct
        ref.value = builder

        ctx.stack.addLast(builder)
        builder.lifeCycle = LifeCycle.Build

        val node = builder.build()

        builder.body(ctx)

        builder.lifeCycle = LifeCycle.Hook
        when (parent) {
            is ChildNodeBuilder<*, *> -> parent.add(builder)
            is ChildElementBuilder<*, *> -> parent.add(builder)
            is ComponentBuilder<*> -> parent.add(builder)
        }

        builder.lifeCycle = LifeCycle.Apply
        builder.applyValues.forEach { action -> action() }
        builder.applyValues.clear()

        builder.lifeCycle = LifeCycle.Bind
//        bindChildren(builder)

        builder.lifeCycle = LifeCycle.Finished

        if (builder is NodeBuilder<*>) {
            builder.registerLayoutListener()
        }

        return node
    }
}