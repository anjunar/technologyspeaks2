package jFx.core

import jFx.state.DisposeBag

@JFxDsl
interface ElementBuilder<E> {
    val ctx: BuildContext

    fun build(): E

    fun afterBuild() {}

    val applyValues: MutableList<() -> Unit>
    val dirtyValues: MutableList<() -> Unit>

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

    fun dirty(action: () -> Unit) {
        dirtyValues.add(action)
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
