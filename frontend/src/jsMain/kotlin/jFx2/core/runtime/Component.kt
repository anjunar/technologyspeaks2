package jFx2.core.runtime

import jFx2.core.capabilities.NodeScope
import jFx2.core.capabilities.UiScope
import jFx2.forms.FormRegistry
import org.w3c.dom.Element

fun <E : Element> component(root: E, body: NodeScope.() -> Unit): E {
    val rt = createRuntime(root)
    val registry = FormRegistry()

    val ui = UiScope(
        dom = rt.dom,
        build = rt.build,
        render = rt.render,
        dispose = rt.dispose,
        formRegistry = registry
    )

    NodeScope(ui, root).body()
    rt.build.flush()
    return root
}