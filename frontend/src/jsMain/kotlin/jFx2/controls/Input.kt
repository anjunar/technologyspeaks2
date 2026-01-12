package jFx2.controls

import jFx2.core.capabilities.DisposeScope
import jFx2.core.capabilities.DomScope
import jFx2.forms.FormRegistryScope
import jFx2.forms.FormScope
import jFx2.forms.registerInputScoped
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event

/**
 * Input funktioniert standalone.
 * Wenn zusätzlich FormScope + FormRegistryScope im Kontext vorhanden sind, wird registriert.
 */
context(scope: DomScope, disposeScope: DisposeScope)
fun input(
    name: String,
    placeholder: String = "",
    onInput: (String) -> Unit = {}
): HTMLInputElement {
    val el = scope.create<HTMLInputElement>("input")
    el.placeholder = placeholder

    val listener: (Event) -> Unit = {
        onInput(el.value)
    }
    el.addEventListener("input", listener)
    disposeScope.register { el.removeEventListener("input", listener) }

    // Optional: Registrierung nur, wenn diese Kontexte existieren.
    // Kotlin hat kein "optional context receiver"; daher zwei Overloads (siehe unten) oder Wrapper-Funktionen.
    return el
}

/**
 * Overload: Input innerhalb einer Form (explizit).
 * Das ist der saubere Weg, ohne Magie.
 */
context(scope: DomScope, disposeScope: DisposeScope, formScope: FormScope, registryScope: FormRegistryScope)
fun formInput(
    name: String,
    placeholder: String = "",
    onInput: (String) -> Unit = {}
): HTMLInputElement {
    registerInputScoped(name)
    return input(name = name, placeholder = placeholder, onInput = onInput)
}