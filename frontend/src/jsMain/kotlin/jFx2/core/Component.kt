package jFx2.core

interface Component<N : org.w3c.dom.Node> {
    val node: N
    fun dispose() {}
}