package jFx2.forms

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.registerField
import jFx2.core.dsl.renderFields
import jFx2.forms.editor.plugins.Heading
import jFx2.state.Disposable
import jFx2.state.Property
import org.w3c.dom.HTMLSelectElement

class Select(override val node: HTMLSelectElement) : FormField<String?, HTMLSelectElement>() {

    val value = Property<String?>(null)

    context(scope: NodeScope)
    fun initialize() {
        node.onchange = { value.set(node.value) }

        observeValue { node.value = it ?: "" }

        renderFields(*this@Select.children.toTypedArray())
    }

    override fun read(): String? = value.get()

    override fun observeValue(listener: (String?) -> Unit): Disposable = value.observe(listener)

}

context(scope: NodeScope)
fun select(name : String, block: context(NodeScope) Select.() -> Unit = {}): Select {
    val el = scope.create<HTMLSelectElement>("select")
    val c = Select(el)
    scope.attach(c)

    registerField(name, c)

    val childScope = scope.fork(
        parent = c.node,
        owner = c,
        ctx = scope.ctx,
        insertPoint = ElementInsertPoint(c.node)
    )

    with(childScope) {
        scope.ui.build.afterBuild {
            c.initialize()
        }
    }

    block(childScope, c)

    return c
}
