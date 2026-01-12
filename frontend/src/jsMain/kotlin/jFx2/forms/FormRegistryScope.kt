package jFx2.forms

import jFx2.core.Component
import org.w3c.dom.Node

interface FormRegistryScope {
    fun <T> registerField(form: FormScope, name: String, field: FormField<T, *>)
    fun unregisterField(form: FormScope, name: String, field: FormField<*, *>)
}