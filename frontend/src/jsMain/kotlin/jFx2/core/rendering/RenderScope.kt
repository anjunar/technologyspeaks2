package jFx2.core.rendering

import jFx2.core.capabilities.DisposeBag
import jFx2.core.capabilities.DisposeScope
import jFx2.core.capabilities.DomScope
import org.w3c.dom.Node

class Mount(
    val node: Node,
    private val bag: DisposeBag
) {
    fun dispose() = bag.dispose()
}

interface RenderScope {
    fun mount(parent: Node, factory: DisposeScope.() -> Node): Mount
    fun replace(parent: Node, current: Mount?, nextFactory: (DisposeScope.() -> Node)?): Mount?
    fun unmount(parent: Node, current: Mount?)
}

class RenderScopeImpl(
    private val dom: DomScope
) : RenderScope {

    override fun mount(parent: Node, factory: DisposeScope.() -> Node): Mount {
        val bag = DisposeBag()
        val disposeScope = object : DisposeScope {
            override fun register(disposable: () -> Unit) = bag.add(disposable)
            override fun dispose() = bag.dispose()
        }
        val node = disposeScope.factory()
        dom.attach(parent, node)
        return Mount(node, bag)
    }

    override fun replace(
        parent: Node,
        current: Mount?,
        nextFactory: (DisposeScope.() -> Node)?
    ): Mount? {
        if (current == null && nextFactory == null) return null
        if (current != null && nextFactory == null) {
            unmount(parent, current)
            return null
        }
        if (current == null && nextFactory != null) {
            return mount(parent, nextFactory)
        }

        // current != null && nextFactory != null
        val newMount = mount(parent, nextFactory!!)
        parent.replaceChild(newMount.node, current!!.node)
        current.dispose()
        return newMount
    }

    override fun unmount(parent: Node, current: Mount?) {
        if (current == null) return
        dom.detach(current.node)
        current.dispose()
    }
}
