package jFx2.controls

import jFx2.core.Component
import jFx2.core.capabilities.BuildScope
import jFx2.core.capabilities.HasUi
import jFx2.core.capabilities.NodeScope
import jFx2.core.capabilities.UiScope
import jFx2.core.dsl.className
import jFx2.core.dsl.classProperty
import jFx2.core.dsl.render
import jFx2.core.dsl.style
import jFx2.core.dsl.text
import jFx2.core.rendering.condition
import jFx2.core.runtime.component
import jFx2.forms.FormField
import jFx2.layout.hr
import jFx2.state.subscribe
import kotlinx.browser.window
import org.w3c.dom.HTMLDivElement
import kotlin.random.Random

class InputContainer(override val node: HTMLDivElement,
                     override var ui : UiScope,
                     val placeholder: String) : Component<HTMLDivElement>(), HasUi {

    private lateinit var field : FormField<*, *>

    fun initialize() {
        (field as HasPlaceholder).placeholder = placeholder
    }

    fun template() {
        val componentMount = component(node, this) {
            div {
                className { "label" }
                dispose.register {
                    field.statusProperty.subscribe(classProperty)
                }

                span {
                    style {
                        display = if (field.statusProperty.contains(Status.empty.name)) "none" else "inline"
                    }
                    className { "placeholder" }
                    text { placeholder }
                }
            }

            render(field)

            hr {
                dispose.register {
                    field.statusProperty.subscribe(classProperty)
                }
            }

        }

        field.observeValue { componentMount.ui.build.flush() }
    }

    fun <F> field(factory: NodeScope.() -> F): F where F : FormField<*, *> {
        val scope = NodeScope(ui, node, this)
        val f = scope.factory()

        field = f

        return f
    }

}

fun NodeScope.inputContainer(placeholder: String, block: InputContainer.() -> Unit): InputContainer {
    val el = create<HTMLDivElement>("div")
    el.classList.add("input-container")

    val c = InputContainer(el, ui, placeholder)

    build.afterBuild {
        c.initialize()
        c.template()
    }

    attach(c)
    c.block()

    return c
}