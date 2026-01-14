package jFx2.forms

interface Formular {

    fun registerInput(name: String, input: Any)

    fun unregisterInput(name: String)

    fun inputOrNull(name: String): Any?

    fun registerField(name: String, field: Any): () -> Unit

}