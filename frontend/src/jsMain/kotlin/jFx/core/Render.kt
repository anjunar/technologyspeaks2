package jFx.core

import jFx.state.Disposable
import jFx.state.ReadOnlyProperty
import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.Node

object Render {

    fun <B> ParentScope.render(slot: ReadOnlyProperty<out B?>)
            where B : ElementBuilder<*>, B : Any {
        addNode(RenderHost(slot, ctx), body = {})
    }

    private class RenderHost<B>(private val slot: ReadOnlyProperty<out B?>, override val ctx: BuildContext) :
        AbstractComponent<HTMLDivElement>()
            where B : ElementBuilder<*>, B : Any {

        private val host: HTMLDivElement by lazy {
            document.createElement("div") as HTMLDivElement
        }

        private var subscription: Disposable? = null
        private var initialized = false

        override fun build(): HTMLDivElement {
            if (!initialized) {
                initialized = true
                subscription = slot.observe { builderOrNull ->
                    host.replaceWithBuilder(builderOrNull)
                }
            }
            return host
        }

        private fun HTMLDivElement.replaceWithBuilder(builderOrNull: B?) {
            while (firstChild != null) removeChild(firstChild!!)

            if (builderOrNull != null) {
                val built = builderOrNull.build()
                if (built is Node) {
                    appendChild(built)
                } else {
                    error("render(slot): builder.build() must return a DOM Node, but was ${built!!::class}")
                }
            }
        }

        override fun dispose() {
            subscription?.invoke()
            subscription = null
            super<AbstractComponent>.dispose()
        }
    }


}