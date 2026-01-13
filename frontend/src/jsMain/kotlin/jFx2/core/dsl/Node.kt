package jFx2.core.dsl

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.state.ListChange
import jFx2.state.ListProperty
import jFx2.state.Property
import jFx2.state.ReadOnlyProperty
import jFx2.state.subscribe
import jFx2.state.subscribeBidirectional
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.css.CSSStyleDeclaration

inline fun <reified C : Component<*>> NodeScope.owner(): C =
    (owner as? C) ?: error("No owner of type ${C::class.simpleName} in this NodeScope")

fun NodeScope.text(value: () -> String) {

    build.dirty {
        val result = value()
        parent.textContent = result
    }

}

fun NodeScope.style(block: CSSStyleDeclaration.() -> Unit) {
    val el = (parent as? HTMLElement)
        ?: error("style() can only be used on HTMLElement nodes, but was: ${parent::class.simpleName}")

    build.dirty {
        el.style.block()
    }

}

fun NodeScope.className(callback : () -> String) {
    build.dirty {
        (parent as HTMLElement).className = callback()
    }
}

val NodeScope.classProperty : ListProperty<String>
    get() {
        val component = owner<Component<*>>()
        val property = component.classProperty

        val el: Element = parent as Element

        owner<Component<*>>()

        if (component.classBinding == null) {
            component.classBinding = property.observeChanges { change ->
                when (change) {
                    is ListChange.Add -> {
                        change.items.forEach { cls ->
                            if (cls.isNotBlank()) el.classList.add(cls)
                        }
                    }

                    is ListChange.Remove -> {
                        change.items.forEach { cls ->
                            if (cls.isNotBlank()) el.classList.remove(cls)
                        }
                    }

                    is ListChange.Replace -> {
                        change.old.forEach { cls ->
                            if (cls.isNotBlank()) el.classList.remove(cls)
                        }
                        change.new.forEach { cls ->
                            if (cls.isNotBlank()) el.classList.add(cls)
                        }
                    }

                    is ListChange.Clear -> {
                        el.className = ""
                    }

                    is ListChange.SetAll -> {
                        el.className = ""
                        change.new.forEach { cls ->
                            if (cls.isNotBlank()) el.classList.add(cls)
                        }
                    }
                }
            }
        }

        return property
    }

fun <T> NodeScope.subscribe(source: ListProperty<T>, target: ListProperty<T>) {
    val d = source.subscribe(target)
    dispose.register(d)
}

fun <T> NodeScope.subscribeBidirectional(source: ListProperty<T>, target: ListProperty<T>) {
    val d = source.subscribeBidirectional(target)
    dispose.register(d)
}

fun <T> NodeScope.subscribe(source: ReadOnlyProperty<T>, target: Property<T>) {
    val d = source.subscribe(target)
    dispose.register(d)
}

fun <T> NodeScope.subscribeBidirectional(source: Property<T>, target: Property<T>) {
    val d = source.subscribeBidirectional(target)
    dispose.register(d)
}


fun NodeScope.registerField(name: String, field: Any) {
    val fs = ui.formScope ?: error("registerField() used outside of a form scope")

    val qName = fs.qualify(name)

    val unregister = ui.formRegistry?.register(qName, field)
    if (unregister != null) {
        ui.dispose.register(unregister)
    }
}

fun NodeScope.render(child: Component<out Node>) {
    ui.attach(parent, child)
}