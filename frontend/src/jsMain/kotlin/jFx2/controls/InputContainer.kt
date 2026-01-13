package jFx2.controls

import jFx2.core.Component
import jFx2.core.capabilities.HasUi
import jFx2.core.capabilities.NodeScope
import jFx2.core.capabilities.UiScope
import jFx2.forms.FormField
import org.w3c.dom.HTMLDivElement

class InputContainer(
    override val node: HTMLDivElement
) : Component<HTMLDivElement>, HasUi {

    override lateinit var ui: UiScope

    private val fields = ArrayList<FormField<*, *>>()
    fun fields(): List<FormField<*, *>> = fields.toList()

    var placeholder: String = ""
        set(v) {
            field = v
            applyPlaceholderToExisting()
        }

    fun <F> field(factory: NodeScope.() -> F): F where F : FormField<*, *> {
        val scope = NodeScope(ui, node)
        val f = scope.factory()

        fields += f
        applyContainerDefaults(f)

        return f
    }

    private fun applyContainerDefaults(field: FormField<*, *>) {
        if (field is HasPlaceholder) {
            field.placeholder = placeholder
        }
    }

    private fun applyPlaceholderToExisting() {
        for (f in fields) {
            if (f is HasPlaceholder) f.placeholder = placeholder
        }
    }
}

fun NodeScope.inputContainer(block: InputContainer.() -> Unit): InputContainer {
    val el = create<HTMLDivElement>("div")
    el.classList.add("input-container")

    val c = InputContainer(el)
    attach(c)
    c.block()

    return c
}