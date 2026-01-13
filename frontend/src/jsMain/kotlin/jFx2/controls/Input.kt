package jFx2.controls

import jFx2.core.capabilities.NodeScope
import jFx2.core.capabilities.UiScope
import jFx2.core.dsl.registerField
import jFx2.forms.Disposable
import jFx2.forms.FormField
import jFx2.state.ListChange
import jFx2.state.ListProperty
import org.w3c.dom.Element
import org.w3c.dom.HTMLInputElement

enum class Status {
    valid, invalid, dirty, empty, focus
}

interface Validator {
    fun validate(value: String): Boolean
    fun message(): String
}

class SizeValidator(val min: Int, val max: Int) : Validator {
    override fun validate(value: String): Boolean = value.length in min..max
    override fun message(): String = "must be between $min and $max characters"
}

class PatternValidator(val pattern: String) : Validator {
    override fun validate(value: String): Boolean = value.matches(pattern.toRegex())
    override fun message(): String = "must match pattern '$pattern'"
}

class NotBlankValidator : Validator {
    override fun validate(value: String): Boolean = value.isNotBlank()
    override fun message(): String = "must not be blank"
}

class Input(val name: String, val ui: UiScope, override val node: HTMLInputElement) :
    FormField<String, HTMLInputElement>(), HasPlaceholder {

    val validators = ListProperty<Validator>()

    fun initialize() {

        val defaultValue = node.value

        node.addEventListener("input", {
            if (node.value.isBlank()) {
                statusProperty.add(Status.empty.name)
            } else {
                statusProperty.remove(Status.empty.name)
            }

            if (node.value != defaultValue) {
                statusProperty.add(Status.dirty.name)
            } else {
                statusProperty.remove(Status.dirty.name)
            }

            validate()
        })

        node.addEventListener("focus", { statusProperty.add(Status.focus.name) })
        node.addEventListener("blur", { statusProperty.remove(Status.focus.name) })

        bindStatusClasses(node, statusProperty)
    }

    fun validate() {
        val errors = validators.filter { !it.validate(node.value) }
        if (errors.isNotEmpty()) {
            statusProperty.add(Status.invalid.name)
            statusProperty.remove(Status.valid.name)
        } else {
            statusProperty.remove(Status.invalid.name)
            statusProperty.add(Status.valid.name)
        }
    }

    fun bindStatusClasses(
        node: Element,
        status: ListProperty<String>
    ): Disposable {

        val owned = LinkedHashSet<String>()

        fun add(cls: String) {
            val c = cls.trim()
            if (c.isEmpty()) return
            if (owned.add(c)) node.classList.add(c)
        }

        fun remove(cls: String) {
            val c = cls.trim()
            if (c.isEmpty()) return
            if (owned.remove(c)) node.classList.remove(c)
        }

        fun resync(items: List<String>) {
            for (c in owned) node.classList.remove(c)
            owned.clear()
            for (c in items) add(c)
        }

        resync(status.get())

        return status.observeChanges { change ->
            when (change) {
                is ListChange.Add -> change.items.forEach(::add)
                is ListChange.Remove -> change.items.forEach(::remove)

                is ListChange.Replace -> {
                    change.old.forEach(::remove)
                    change.new.forEach(::add)
                }

                is ListChange.Clear -> {
                    for (c in owned) node.classList.remove(c)
                    owned.clear()
                }

                is ListChange.SetAll -> resync(change.new)
            }
        }
    }

    override var placeholder: String
        get() = node.placeholder
        set(v) {
            node.placeholder = v
        }

    fun value(): String = node.value
    fun setValue(v: String) {
        node.value = v
    }

    override fun read(): String {
        return node.value
    }
}

fun NodeScope.input(name: String, block: Input.() -> Unit = {}): Input {
    val el = create<HTMLInputElement>("input")
    el.name = name
    val c = Input(name, ui, el)

    build.afterBuild {
        c.initialize()
    }

    c.block()
    registerField(name, c)
    attach(c)
    return c
}