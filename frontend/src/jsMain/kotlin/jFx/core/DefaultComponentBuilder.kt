package jFx.core

import jFx.state.DisposeBag

class DefaultComponentBuilder<C> : ComponentBuilder<C> {
    override val ctx: BuildContext = BuildContext()

    private val children: MutableList<ElementBuilder<*>> = mutableListOf()

    override val applyValues: MutableList<() -> Unit> = mutableListOf()
    override val dirtyValues: MutableList<() -> Unit> = mutableListOf()

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
