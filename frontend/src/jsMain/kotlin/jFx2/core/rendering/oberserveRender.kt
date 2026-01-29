package jFx2.core.rendering

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.RangeInsertPoint
import jFx2.core.runtime.ComponentMount
import jFx2.core.runtime.componentWithScope
import jFx2.state.Disposable
import jFx2.state.ReadOnlyProperty
import kotlinx.browser.document
import org.w3c.dom.Comment

context(scope: NodeScope)
fun <T> observeRender(
    source: ReadOnlyProperty<T>,
    block: context(NodeScope) (T) -> Unit
) {
    val start: Comment = document.createComment("jFx2:observe")
    val end: Comment = document.createComment("jFx2:/observe")
    scope.insertPoint.insert(start)
    scope.insertPoint.insert(end)

    val range = RangeInsertPoint(start, end)
    val owner = RangeOwner(start)

    var currentMount: ComponentMount? = null

    fun rebuild(value: T) {
        currentMount?.dispose()
        currentMount = null
        range.clear()

        val childScope = scope.fork(
            parent = range.parent,
            owner = owner,
            ctx = scope.ctx.fork(),
            insertPoint = range
        )

        with(childScope) {
            scope.ui.build.afterBuild { owner.afterBuild() }
        }

        currentMount = componentWithScope(childScope) {
            block(value)
        }
    }

    val d: Disposable = source.observe { rebuild(it) }
    scope.dispose.register(d)
    scope.dispose.register {
        currentMount?.dispose()
        currentMount = null
        range.dispose()
    }
}
