package jFx2.core.runtime

import jFx2.core.capabilities.DisposeBag
import jFx2.core.capabilities.LogScope
import jFx2.core.capabilities.NoopLogScope
import org.w3c.dom.Element

fun <E : Element> component(
    root: E,
    log: LogScope = NoopLogScope,
    body: Runtime.() -> Unit
): E {
    val build = BuildScopeImpl()
    val bag = DisposeBag()

    val runtime = Runtime(
        dom = DomScopeImpl(root),
        build = build,
        disposeBag = bag,
        dispose = DisposeScopeImpl(bag),
        log = log
    )

    runtime.body()
    build.flush()
    return root
}