package jFx2.forms

import jFx2.core.capabilities.DisposeScope

class FormRegistryImpl : FormRegistryScope {
    private val map = linkedMapOf<String, MutableSet<String>>()

    override fun registerInput(formName: String, inputName: String) {
        map.getOrPut(formName) { linkedSetOf() }.add(inputName)
    }

    override fun unregisterInput(formName: String, inputName: String) {
        map[formName]?.remove(inputName)
    }

    fun snapshot(): Map<String, Set<String>> =
        map.mapValues { it.value.toSet() }
}

/**
 * Kontext für "wir sind gerade in einer Form".
 */
data class FormScope(val formName: String)

/**
 * Helper: registriert automatisch in DisposeScope (so wird unregister garantiert).
 */
context(scope: FormScope, registryScope: FormRegistryScope, disposeScope: DisposeScope)
fun registerInputScoped(inputName: String) {
    val f = scope.formName
    registryScope.registerInput(f, inputName)
    disposeScope.register { registryScope.unregisterInput(f, inputName) }
}