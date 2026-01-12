package jFx.controls

import jFx.core.AbstractChildrenComponent
import jFx.core.DSL
import jFx.core.DSL.ParentScope
import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLFieldSetElement

class SubForm(override val ctx: DSL.BuildContext) : AbstractChildrenComponent<HTMLFieldSetElement, HTMLElement>(), Formular {

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
        fun ParentScope.subForm(body: SubForm.(DSL.BuildContext) -> Unit): SubForm {
            val builder = SubForm(ctx)

            val owner = ctx.nearestFormular()
            owner?.register(builder)
            builder.onDispose { owner?.unregister(builder) }

            ctx.pushFormular(builder)
            try {
                addNode(builder, body)
            } finally {
                ctx.popFormular()
            }
            return builder
        }
    }

}