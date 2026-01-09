package jFx.core

import jFx.state.Disposable
import jFx.state.DisposeBag
import jFx.state.ListProperty
import jFx.state.ReadOnlyProperty
import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.css.CSSStyleDeclaration

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

        val disposeBag: DisposeBag

        fun onDispose(action: () -> Unit) {
            disposeBag.add(action)
        }

        fun dispose() {
            disposeBag.dispose()
        }

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

        override val disposeBag: DisposeBag = DisposeBag()

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

    fun <B> ParentScope.render(slot: ReadOnlyProperty<out B?>)
            where B : ElementBuilder<*>, B : Any {
        addNode(RenderHost(slot), body = {})
    }

    private class RenderHost<B>(private val slot: ReadOnlyProperty<out B?>) : AbstractComponent(), ElementBuilder<HTMLDivElement>
            where B : ElementBuilder<*>, B : Any {

        private val host: HTMLDivElement by lazy {
            document.createElement("div") as HTMLDivElement
        }

        private var subscription: Disposable? = null
        private var initialized = false

        override fun build(): HTMLDivElement {
            if (!initialized) {
                initialized = true
                subscription = slot.observe { builderOrNull ->
                    host.replaceWithBuilder(builderOrNull)
                }
            }
            return host
        }

        private fun HTMLDivElement.replaceWithBuilder(builderOrNull: B?) {
            while (firstChild != null) removeChild(firstChild!!)

            if (builderOrNull != null) {
                val built = builderOrNull.build()
                if (built is Node) {
                    appendChild(built)
                } else {
                    error("render(slot): builder.build() must return a DOM Node, but was ${built!!::class}")
                }
            }
        }

        override fun dispose() {
            subscription?.invoke()
            subscription = null
        }
    }

    fun ParentScope.condition(
        predicate: ReadOnlyProperty<Boolean>,
        body: ParentScope.() -> Unit
    ) {
        addNode(ConditionHost(predicate, body), body = {})
    }

    private class ConditionHost(
        private val predicate: ReadOnlyProperty<Boolean>,
        private val body: ParentScope.() -> Unit
    ) : AbstractComponent(), ElementBuilder<HTMLDivElement> {

        private val host: HTMLDivElement by lazy {
            document.createElement("div") as HTMLDivElement
        }

        private var subscription: Disposable? = null
        private var initialized = false

        private val mountedBuilders: MutableList<ElementBuilder<*>> = mutableListOf()

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

            val hostScope = object : ParentScope {
                override fun <T : Node, B : ElementBuilder<T>> addNode(builder: B, body: B.() -> Unit): T {
                    builder.lifeCycle = LifeCycle.Build
                    val node = builder.build()

                    builder.body()

                    mountedBuilders.add(builder)

                    if (node is Node) {
                        this@update.appendChild(node)
                    } else {
                        error("condition: builder.build() must return a DOM Node")
                    }

                    builder.lifeCycle = LifeCycle.Apply
                    builder.applyValues.forEach { it() }
                    builder.applyValues.clear()

                    builder.lifeCycle = LifeCycle.Finished

                    if (builder is NodeBuilder<*>) {
                        builder.registerLayoutListener()
                    }

                    return node
                }
            }

            hostScope.body()
        }

        private fun clearMounted() {
            while (host.firstChild != null) host.removeChild(host.firstChild!!)

            mountedBuilders.forEach { b ->
                try { b.dispose() } catch (_: Throwable) { /* fail-safe */ }
            }
            mountedBuilders.clear()
        }

        override fun dispose() {
            subscription?.invoke()
            subscription = null
            clearMounted()
        }
    }

    inline fun <T> ParentScope.list(
        items: Iterable<T>,
        crossinline body: ParentScope.(T) -> Unit
    ) {
        for (item in items) {
            this.body(item)
        }
    }

    inline fun <T> ParentScope.listIndexed(
        items: Iterable<T>,
        crossinline body: ParentScope.(Int, T) -> Unit
    ) {
        var i = 0
        for (item in items) {
            this.body(i++, item)
        }
    }

    inline fun ParentScope.repeat(
        times: Int,
        crossinline body: ParentScope.(Int) -> Unit
    ) {
        for (i in 0 until times) {
            this.body(i)
        }
    }

    fun <E : HTMLElement> ElementBuilder<E>.style(block: CSSStyleDeclaration.() -> Unit) {
        write {
            build().style.block()
        }
    }

    var <E : HTMLElement> ElementBuilder<E>.className : String
        get() = read(build().className)
        set(value) = write { build().className = value }

    var <E : HTMLElement> ElementBuilder<E>.id : String
        get() = read(build().id)
        set(value) = write { build().id = value }

}