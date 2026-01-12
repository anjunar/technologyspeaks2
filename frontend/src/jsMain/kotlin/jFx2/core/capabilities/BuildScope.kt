package jFx2.core.capabilities

interface BuildScope {

    fun afterBuild(action: () -> Unit)

    fun apply(action: () -> Unit)

    fun dirty(action: () -> Unit)

    fun flush()
}