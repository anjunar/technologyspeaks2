package jFx2.core.runtime

import jFx2.core.capabilities.BuildScope
import jFx2.core.capabilities.DisposeBag
import jFx2.core.capabilities.DisposeScope
import jFx2.core.capabilities.DomScope
import org.w3c.dom.Element

class BuildScopeImpl : BuildScope {
    private val afterBuildQ = ArrayDeque<() -> Unit>()
    private val applyQ = ArrayDeque<() -> Unit>()
    private val dirtyQ = ArrayDeque<() -> Unit>()

    override fun afterBuild(action: () -> Unit) { afterBuildQ.addLast(action) }
    override fun apply(action: () -> Unit) { applyQ.addLast(action) }
    override fun dirty(action: () -> Unit) { dirtyQ.addLast(action) }

    override fun flush() {
        while (afterBuildQ.isNotEmpty()) afterBuildQ.removeFirst().invoke()
        while (applyQ.isNotEmpty()) applyQ.removeFirst().invoke()
        dirtyQ.forEach { it.invoke() }
    }
}

class DomScopeImpl(
    override val root: Element
) : DomScope

class DisposeScopeImpl(
    private val bag: DisposeBag
) : DisposeScope {
    override fun register(disposable: () -> Unit) {
        bag.add(disposable)
    }
    override fun dispose() {
        bag.dispose()
    }
}