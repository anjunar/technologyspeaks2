package jFx.core

import jFx.controls.Formular
import org.w3c.dom.HTMLElement
import org.w3c.dom.css.CSSStyleDeclaration

object DSL {

    val FormularKey = ScopeKey<Formular>()

    fun <C> element(body: DefaultComponentBuilder<C>.() -> Unit): C {
        val root = DefaultComponentBuilder<C>()
        root.body()
        return root.build()
    }

    fun <E, C : ElementBuilder<E>> component(body: DefaultComponentBuilder<E>.() -> Unit): C {
        val root = DefaultComponentBuilder<E>()
        root.body()
        @Suppress("UNCHECKED_CAST")
        return root as C
    }

    fun <E : HTMLElement> ElementBuilder<E>.style(block: CSSStyleDeclaration.() -> Unit) {
        ctx.addDirtyComponent(this)
        dirty { build().style.block() }
        write {
            build().style.block()
        }
    }

    var <E : HTMLElement> ElementBuilder<E>.className: String
        get() = read(build().className)
        set(value) = write { build().className = value }

    var <E : HTMLElement> ElementBuilder<E>.id: String
        get() = read(build().id)
        set(value) = write { build().id = value }
}
