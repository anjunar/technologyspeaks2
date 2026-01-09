package jFx.controls

import jFx.core.AbstractComponent
import jFx.core.DSL
import jFx.core.DSL.NodeBuilder
import jFx.core.DSL.ParentScope
import jFx.state.ListProperty
import jFx.state.Property
import jFx.util.EventHelper
import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener

@Suppress("UNCHECKED_CAST")
class Input(override val ctx: DSL.BuildContext) : AbstractComponent(), NodeBuilder<HTMLInputElement> {

    val valueProperty = Property("")

    val validatorsProperty = ListProperty<Validator<in Any,in Any>>(emptyList())

    val errorsProperty = ListProperty<String>()

    val node by lazy {
        val inputElement = document.createElement("input") as HTMLInputElement

        EventHelper.events(inputElement, { ctx.invalidate() }, "input", "blur", "focus")

        bind(valueProperty)

        inputElement.addEventListener("input", { validate() })

        inputElement
    }

    fun validate() {
        errorsProperty.set(emptyList())

        errorsProperty.set(
            validatorsProperty.get().filter { validator -> !validator.validate(node.value) }
                .map { validator -> validator.message(validator) }
                .toList()
        )
    }

    fun valueWriter(callback: (String) -> Unit) {
        node.addEventListener("input", { callback(node.value) })
    }

    fun validators(vararg validators: Validator<*,*>) = validatorsProperty.set(validators.toList() as List<Validator<in Any,in Any>>)

    var value: String
        get() = read(node.value)
        set(value) = write { node.value = value }

    var placeholder: String
        get() = read(node.placeholder)
        set(value) = write { node.placeholder = value }

    fun onClick(name: String, handler: (event: Event) -> Unit) = write {
        node.addEventListener(name, { handler(it) })
    }

    fun bind(property: Property<String>) {
        write {
            val input = build()

            val disposeObs = property.observe { v ->
                if (input.value != v) input.value = v!!
            }
            onDispose(disposeObs)

            val listener = EventListener {
                val v = input.value
                if (property.get() != v) property.set(v)
            }
            input.addEventListener("input", listener)
            onDispose { input.removeEventListener("input", listener) }
        }
    }

    override fun build(): HTMLInputElement = node

    companion object {
        fun ParentScope.input(body: Input.(DSL.BuildContext) -> Unit): Input {
            val builder = Input(ctx)
            addNode(builder, body)
            return builder
        }

        interface Validator<T, V> {
            fun validate(value: T): Boolean
            val message: (V) -> String
        }

        class SizeValidator(
            val min: Int, val max: Int,
            override val message: (SizeValidator) -> String = { validator -> "Size must be between ${validator.min} and ${validator.max}" }
        ) : Validator<String, SizeValidator> {
            override fun validate(value: String): Boolean {
                return value.length in min..max
            }
        }

        class RegexValidator(
            val regex: Regex,
            override val message: (RegexValidator) -> String = { validator -> "Value must match ${validator.regex.pattern}" }
        ) : Validator<String, RegexValidator> {
            override fun validate(value: String): Boolean {
                return regex.matches(value)
            }
        }

        class NotEmptyValidator(override val message: (NotEmptyValidator) -> String = { "Must not be empty" }) :
            Validator<String, NotEmptyValidator> {
            override fun validate(value: String): Boolean {
                return value.isNotEmpty()
            }
        }

        class NotNullValidator(override val message: (NotNullValidator) -> String = { "Must not be null" }) :
            Validator<Any?, NotNullValidator> {
            override fun validate(value: Any?): Boolean {
                return value != null
            }
        }
    }
}

