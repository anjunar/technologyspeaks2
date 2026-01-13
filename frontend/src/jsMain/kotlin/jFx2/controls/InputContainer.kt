package jFx2.controls

import jFx2.core.Component
import jFx2.core.capabilities.BuildScope
import jFx2.core.capabilities.HasUi
import jFx2.core.capabilities.NodeScope
import jFx2.core.capabilities.UiScope
import jFx2.core.dsl.render
import jFx2.core.dsl.style
import jFx2.core.dsl.text
import jFx2.core.rendering.condition
import jFx2.core.runtime.component
import jFx2.forms.FormField
import jFx2.layout.hr
import kotlinx.browser.window
import org.w3c.dom.HTMLDivElement
import kotlin.random.Random

class InputContainer(override val node: HTMLDivElement,
                     override var ui : UiScope,
                     val placeholder: String) : Component<HTMLDivElement>, HasUi {

    private lateinit var field : FormField<*, *>

    fun initialize() {
        (field as HasPlaceholder).placeholder = placeholder
    }

    fun template() {
        component(node) {
            div {
                style {
                    height = "12px"
                    marginBottom = "5px"
                }
                span {
                    style {
                        fontSize = "10px"
                    }
                    text { placeholder }
                }
            }

            render(field)

            hr {
                style {
                    margin = "0"
                }
            }

        }
    }

    fun <F> field(factory: NodeScope.() -> F): F where F : FormField<*, *> {
        val scope = NodeScope(ui, node)
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