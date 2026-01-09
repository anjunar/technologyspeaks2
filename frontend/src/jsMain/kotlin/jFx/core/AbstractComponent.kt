package jFx.core

import jFx.core.DSL.LifeCycle
import jFx.state.DisposeBag

abstract class AbstractComponent {

    val applyValues: MutableList<() -> Unit> = mutableListOf()

    var lifeCycle: LifeCycle = LifeCycle.Build

    val disposeBag: DisposeBag = DisposeBag()


}