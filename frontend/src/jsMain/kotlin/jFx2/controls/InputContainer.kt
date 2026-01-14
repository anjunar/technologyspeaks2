package jFx2.controls

import jFx2.core.Component
import jFx2.core.capabilities.HasUi
import jFx2.core.capabilities.NodeScope
import jFx2.core.capabilities.UiScope
import jFx2.core.dsl.*
import jFx2.core.runtime.component
import jFx2.forms.FormContext
import jFx2.forms.FormField
import jFx2.layout.hr
import org.w3c.dom.HTMLDivElement

class InputContainer(
    override val node: HTMLDivElement,
    override var ui: UiScope,
    val forms: FormContext?,
    val placeholder: String
) : Component<HTMLDivElement>(), HasUi {

    private lateinit var field: FormField<*, *>

    private lateinit var errors : Span

    fun initialize() {
        (field as HasPlaceholder).placeholder = placeholder
        field.errorsProperty.observe { errors.node.textContent = it.joinToString(" ") }
    }

    fun template() {
        val componentMount = component(node, this) {
            div {
                className { "label" }

                span {
                    style {
                        display = if (field.statusProperty.contains(Status.empty.name)) "none" else "inline"
                        fontSize = "10px"
                    }
                    subscribe(field.statusProperty, classProperty)
                    text { placeholder }
                }
            }

            render(field)

            hr {
                subscribe(field.statusProperty, classProperty)
            }

            div {
                className { "errors" }
                errors = span {}
            }

        }

        field.observeValue { componentMount.ui.build.flush() }
    }

    fun <F> field(factory: NodeScope.() -> F): F where F : FormField<*, *> {
        val scope = NodeScope(ui, node, owner = this, forms = forms)   // <- forms rein
        val f = scope.factory()
        field = f
        return f
    }
}

fun NodeScope.inputContainer(placeholder: String, block: InputContainer.() -> Unit): InputContainer {
    val el = create<HTMLDivElement>("div")
    el.classList.add("input-container")

    val c = InputContainer(el, ui, forms, placeholder)

    build.afterBuild {
        c.template()
        c.initialize()
    }

    attach(c)
    c.block()

    return c
}