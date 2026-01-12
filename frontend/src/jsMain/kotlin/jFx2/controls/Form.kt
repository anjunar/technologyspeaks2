package jFx2.controls

import jFx2.core.capabilities.DisposeScope
import jFx2.core.capabilities.DomScope
import jFx2.forms.FormRegistryImpl
import jFx2.forms.FormRegistryScope
import jFx2.forms.FormScope
import org.w3c.dom.HTMLFormElement
import org.w3c.dom.Node

context(scope: DomScope, disposeScope: DisposeScope)
fun form(
    name: String,
    registry: FormRegistryImpl,
    buildChildren: context(Node, FormScope, FormRegistryScope, DisposeScope) () -> Unit
): HTMLFormElement {
    val el = scope.create<HTMLFormElement>("form")
    val formScope = FormScope(name)

    with(el as Node) {
        with(formScope) {
            with(registry as FormRegistryScope) {
                buildChildren(el, formScope, registry, disposeScope)
            }
        }
    }
    return el
}