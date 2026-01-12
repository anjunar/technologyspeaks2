package jFx2.forms

interface FormRegistryScope {
    fun registerInput(formName: String, inputName: String)
    fun unregisterInput(formName: String, inputName: String)
}