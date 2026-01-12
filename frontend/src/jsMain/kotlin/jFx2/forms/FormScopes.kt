package jFx2.forms

import jFx2.core.capabilities.DisposeScope
import org.w3c.dom.Node

class FormRegistry : FormRegistryScope {

    private val forms: MutableMap<String, MutableMap<String, FormField<*, *>>> = linkedMapOf()

    override fun <T> registerField(form: FormScope, name: String, field: FormField<T, *>) {
        forms.getOrPut(form.name) { linkedMapOf() }[name] = field
    }

    override fun unregisterField(form: FormScope, name: String, field: FormField<*, *>) {
        val map = forms[form.name] ?: return
        val current = map[name]
        if (current === field) map.remove(name)
        if (map.isEmpty()) forms.remove(form.name)
    }

    fun values(formName: String): Map<String, Any?> {
        val map = forms[formName] ?: return emptyMap()
        return map.mapValues { (_, f) -> f.read() as Any? }
    }
}

data class FormScope(val name: String)