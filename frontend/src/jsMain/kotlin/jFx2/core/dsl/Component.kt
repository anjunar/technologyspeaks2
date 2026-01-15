package jFx2.core.dsl

import jFx2.core.Component
import jFx2.state.ListChange
import jFx2.state.ListProperty
import jFx2.state.Property
import jFx2.state.ReadOnlyProperty
import jFx2.state.subscribe
import jFx2.state.subscribeBidirectional
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.css.CSSStyleDeclaration
import org.w3c.dom.events.MouseEvent

fun Component<*>.mousedown(value: (MouseEvent) -> Unit) {
    node.addEventListener("mousedown", { value(it as MouseEvent) })
}

fun Component<*>.text(value: () -> String) {

    ui.build.dirty {
        val result = value()
        node.textContent = result
    }

}

fun Component<*>.style(block: CSSStyleDeclaration.() -> Unit) {
    val el = (node as? HTMLElement)
        ?: error("style() can only be used on HTMLElement nodes, but was: ${node::class.simpleName}")

    ui.build.dirty {
        el.style.block()
    }

}


fun Component<*>.className(callback: () -> String) {
    ui.build.dirty {
        (node as HTMLElement).className = callback()
    }
}

/*
val Component<*>.classProperty: ListProperty<String>
    get() {
        val property = classProperty

        val el: Element = node as Element

        if (classBinding == null) {
            classBinding = property.observeChanges { change ->
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
*/

fun <T> Component<*>.subscribe(source: ListProperty<T>, target: ListProperty<T>) {
    val d = source.subscribe(target)
    ui.dispose.register(d)
}

fun <T> Component<*>.subscribeBidirectional(source: ListProperty<T>, target: ListProperty<T>) {
    val d = source.subscribeBidirectional(target)
    ui.dispose.register(d)
}

fun <T> Component<*>.subscribe(source: ReadOnlyProperty<T>, target: Property<T>) {
    val d = source.subscribe(target)
    ui.dispose.register(d)
}

fun <T> Component<*>.subscribeBidirectional(source: Property<T>, target: Property<T>) {
    val d = source.subscribeBidirectional(target)
    ui.dispose.register(d)
}

