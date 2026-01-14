package jFx2.core.runtime

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.capabilities.UiScope
import jFx2.forms.NamedFormContext
import jFx2.forms.RootContext
import org.w3c.dom.Element

class ComponentMount<E : Element>(
    val root: E,
    val ui: UiScope,
    val dispose: () -> Unit
)

fun <E : Element> component(root: E, owner: Component<*>? = null, body: NodeScope.() -> Unit): ComponentMount<E> {
    val rt = createRuntime(root)

    val ui = UiScope(
        dom = rt.dom,
        build = rt.build,
        render = rt.render,
        dispose = rt.dispose,
    )

    val rootForms = RootContext()

    NodeScope(ui, root, owner, rootForms).body()
    rt.build.flush()

    return ComponentMount(
        root = root,
        ui = ui,
        dispose = { rt.dispose.dispose() }
    )
}