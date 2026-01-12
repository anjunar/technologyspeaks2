package jFx.core

import jFx.state.DisposeBag

abstract class AbstractComponent<E>  : ElementBuilder<E> {

    override val applyValues: MutableList<() -> Unit> = mutableListOf()

    override val dirtyValues: MutableList<() -> Unit> = mutableListOf()

    override var lifeCycle: LifeCycle = LifeCycle.Build

    override val disposeBag: DisposeBag = DisposeBag()

    override fun dispose() {
        disposeBag.dispose()
    }

}