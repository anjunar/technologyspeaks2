package jFx2.core

import jFx2.state.CompositeDisposable
import jFx2.state.Disposable
import org.w3c.dom.Node

abstract class Component<N : Node> {

    abstract val node: N

    val disposeBag = CompositeDisposable()

    fun onDispose(d: Disposable) = disposeBag.add(d)

    open fun dispose() {
        disposeBag.dispose()
    }
}

