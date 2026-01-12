package jFx.controls

import jFx.core.AbstractComponent
import jFx.core.BuildContext
import jFx.core.DSL
import jFx.core.NodeBuilder
import jFx.core.ParentScope
import jFx.state.ListProperty
import jFx.state.Property
import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener

@Suppress("UNCHECKED_CAST")
class Input(override val ctx: BuildContext) : AbstractComponent<HTMLInputElement>(), NodeBuilder<HTMLInputElement> {

    val valueProperty = Property("")

    val validatorsProperty = ListProperty<Validator<in Any,in Any>>(emptyList())

    val errorsProperty = ListProperty<String>()

    val statusProperty = ListProperty<Status>(listOf(Status.empty))

    val node by lazy { document.createElement("input") as HTMLInputElement }

    fun validate() {
        errorsProperty.setAll(emptyList())

        errorsProperty.setAll(
            validatorsProperty.get().filter { validator -> !validator.validate(node.value) }
                .map { validator -> validator.message(validator) }
                .toList()
        )

        if (errorsProperty.get().isEmpty()) {
            statusProperty.remove(Status.invalid)
            statusProperty.add(Status.valid)
        } else {
            statusProperty.add(Status.invalid)
            statusProperty.remove(Status.valid)
        }
    }

    fun valueWriter(callback: (String) -> Unit) {
        node.addEventListener("input", { callback(node.value) })
    }

    fun validators(vararg validators: Validator<*,*>) = validatorsProperty.setAll(validators.toList() as List<Validator<in Any,in Any>>)

    var name : String
        get() = read(node.name)
        set(value) = write { node.name = value }

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

    override fun afterBuild() {
        val defaultValue = node.value

        node.addEventListener("input", {
            ctx.invalidate()
            validate()

            val inputValue = (it.target as HTMLInputElement).value

            if (inputValue.isEmpty()) {
                statusProperty.add(Status.empty)
            } else {
                statusProperty.remove(Status.empty)
            }

            if (inputValue == defaultValue) {
                statusProperty.remove(Status.dirty)
            } else {
                statusProperty.add(Status.dirty)
            }

        })

        node.addEventListener("focus", {
            ctx.invalidate()
            statusProperty.add(Status.focus)
        })

        node.addEventListener("blur", {
            ctx.invalidate()
            statusProperty.remove(Status.focus)
        })

        statusProperty.observeChanges { change ->
            when (change) {
                is ListProperty.Change.Add -> node.classList.add(change.items.first().name)
                is ListProperty.Change.Remove -> node.classList.remove(change.items.first().name)
                else -> {}
            }
        }

        bind(valueProperty)
    }

    override fun toString(): String {
        return "$name:$value"
    }

    companion object {
        fun ParentScope.input(body: Input.(BuildContext) -> Unit): Input {
            val builder = Input(ctx)

            val owner = ctx.scope.get(DSL.FormularKey)
            owner?.register(builder)
            builder.onDispose { owner?.unregister(builder) }

            addNode(builder, body)
            return builder
        }

        enum class Status {
            valid, invalid, empty, focus, dirty
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

