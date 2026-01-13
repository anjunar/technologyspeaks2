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
import org.w3c.dom.HTMLDivElement

class InputContainer(override val node: HTMLDivElement, val build : BuildScope) : Component<HTMLDivElement>, HasUi {

    override lateinit var ui: UiScope

    private lateinit var field : FormField<*, *>

    fun initialize() {
        component(node) {
            div {
                style {
                    height = "10px"
                }
                span {
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

    var placeholder: String = ""
        set(v) {
            build.apply {
                field = v
                applyPlaceholderToExisting()
            }
        }

    fun <F> field(factory: NodeScope.() -> F): F where F : FormField<*, *> {
        val scope = NodeScope(ui, node)
        val f = scope.factory()

        field = f
        applyContainerDefaults(f)

        return f
    }

    private fun applyContainerDefaults(field: FormField<*, *>) {
        build.apply {
            if (field is HasPlaceholder) {
                field.placeholder = placeholder
            }
        }
    }

    private fun applyPlaceholderToExisting() {
        build.apply {
            if (field is HasPlaceholder) (field as HasPlaceholder).placeholder = placeholder
        }
    }
}

fun NodeScope.inputContainer(block: InputContainer.() -> Unit): InputContainer {
    val el = create<HTMLDivElement>("div")
    el.classList.add("input-container")

    val c = InputContainer(el, build)

    build.afterBuild { c.initialize() }

    attach(c)
    c.block()

    return c
}