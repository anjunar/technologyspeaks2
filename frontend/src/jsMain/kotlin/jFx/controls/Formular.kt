package jFx.controls

interface Formular {

    fun register(input : Input)
    fun unregister(input : Input)

    fun register(formular : SubForm)
    fun unregister(formular : SubForm)

}