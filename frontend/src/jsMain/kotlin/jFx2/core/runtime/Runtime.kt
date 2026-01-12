package jFx2.core.runtime

import jFx2.core.capabilities.BuildScope
import jFx2.core.capabilities.Disposable
import jFx2.core.capabilities.DisposeBag
import jFx2.core.capabilities.DisposeScope
import jFx2.core.capabilities.DomScope
import jFx2.core.rendering.RenderScope
import jFx2.core.rendering.RenderScopeImpl
import org.w3c.dom.Element

fun <E : Element> createRuntime(root: E): Runtime {

    // 1) Dispose
    val bag = DisposeBag()
    val disposeScope = object : DisposeScope {
        override fun register(disposable: Disposable) {
            bag.add(disposable)
        }
    }

    val buildScope = BuildScopeImpl()

    val domScope = DomScopeImpl(root)

    val renderScope = RenderScopeImpl(domScope)

    return Runtime(
        dom = domScope,
        build = buildScope,
        render = renderScope,
        dispose = disposeScope,
        bag = bag
    )
}

data class Runtime(
    val dom: DomScope,
    val build: BuildScope,
    val render: RenderScope,
    val dispose: DisposeScope,
    val bag: DisposeBag
)
