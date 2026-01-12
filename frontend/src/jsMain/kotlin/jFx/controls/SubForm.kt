package jFx.controls

import jFx.core.AbstractChildrenComponent
import jFx.core.BuildContext
import jFx.core.DSL
import jFx.core.ParentScope
import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLFieldSetElement

class SubForm(override val ctx: BuildContext) : AbstractChildrenComponent<HTMLFieldSetElement, HTMLElement>(), Formular {

    val inputs : MutableList<Input> = mutableListOf()

    val subForms : MutableList<SubForm> = mutableListOf()

    val node by lazy {
        document.createElement("fieldset") as HTMLFieldSetElement
    }

    var name : String
        get() = read(node.name)
        set(value) { write { node.name = value } }

    override fun register(input: Input) {
        inputs.add(input)
    }

    override fun register(formular: SubForm) {
        subForms.add(formular)
    }

    override fun unregister(input: Input) {
        inputs.remove(input)
    }

    override fun unregister(formular: SubForm) {
        subForms.remove(formular)
    }

    override fun build(): HTMLFieldSetElement = node

    override fun toString(): String {
        return name + "= { " + inputs.joinToString { it.toString() } + "," + subForms.joinToString { it.toString() } + " }"
    }

    companion object {
        fun ParentScope.subForm(body: SubForm.(BuildContext) -> Unit): SubForm {
            val builder = SubForm(ctx)

            val owner = ctx.scope.get(DSL.FormularKey)
            owner?.register(builder)
            builder.onDispose { owner?.unregister(builder) }

            val prevScope = ctx.scope
            ctx.scope = prevScope.with(DSL.FormularKey, builder)

            addNode(builder, body)

            return builder
        }
    }

}