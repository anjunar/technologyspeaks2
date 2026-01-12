package jFx2.rendering

import jFx2.core.capabilities.BuildScope
import jFx2.core.capabilities.DisposeScope
import jFx2.state.ReadOnlyProperty
import org.w3c.dom.Node

context(scope: DisposeScope, renderScope: RenderScope, buildScope: BuildScope)
fun condition(
    parent: Node,
    predicate: ReadOnlyProperty<Boolean>,
    whenTrue: (context(DisposeScope) () -> Node)?,
    whenFalse: (context(DisposeScope) () -> Node)? = null
) {
    var current: Mount? = null

    current = renderScope.replace(parent, current, if (predicate.get()) whenTrue else whenFalse)

    val sub = predicate.observe { v ->
        buildScope.dirty {
            current = renderScope.replace(parent, current, if (v) whenTrue else whenFalse)
        }
    }
    scope.register(sub)
}
