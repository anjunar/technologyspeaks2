package app

import jFx2.core.runtime.component
import jFx2.rendering.RenderScopeImpl
import jFx2.state.ReadOnlyProperty
import jFx2.rendering.condition
import jFx2.core.capabilities.Disposable
import jFx2.controls.button
import jFx2.controls.div
import jFx2.controls.input
import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.Node

// Minimal Property fürs Demo
class Property<T>(initial: T) : ReadOnlyProperty<T> {
    private var value: T = initial
    private val listeners = LinkedHashMap<Int, (T) -> Unit>()
    private var nextId = 1

    override fun get(): T = value

    fun set(newValue: T) {
        if (newValue == value) return
        value = newValue
        listeners.values.toList().forEach { it(newValue) }
    }

    override fun observe(listener: (T) -> Unit): Disposable {
        val id = nextId++
        listeners[id] = listener
        listener(value)
        return { listeners.remove(id) }
    }
}

fun main() {
    val root = document.createElement("div") as HTMLDivElement

    component(root) {
        val render = RenderScopeImpl(dom)

        val count = Property(0)
        val showExtra = Property(true)

        render.mount(dom.root) {
            with(dom) {
                with(render) {
                    with(build) {

                        div {

                            val plusBtn = button(
                                text = "Count +1",
                                onClick = {
                                    count.set(count.get() + 1)
                                    build.flush()
                                }
                            )
                            attach(this, plusBtn)

                            val toggleBtn = button(
                                text = "Toggle extra",
                                onClick = {
                                    showExtra.set(!showExtra.get())
                                    build.flush()
                                }
                            )
                            attach(this, toggleBtn)

                            val label = create<HTMLDivElement>("div")
                            label.textContent = "Count: ${count.get()}"
                            attach(this, label)

                            val sub = count.observe { v ->
                                dirty { label.textContent = "Count: $v" }
                            }
                            register(sub)

                            val inp = input(
                                name = "dummy",
                                placeholder = "Type (not used)"
                            )
                            attach(this, inp)

                            condition(
                                parent = this@div,
                                predicate = showExtra,
                                whenTrue = {
                                    with(dom) {
                                        val extra = create<org.w3c.dom.HTMLInputElement>("input")
                                        extra.placeholder = "Extra input visible"
                                        extra
                                    }
                                },
                                whenFalse = {
                                    with(dom) {
                                        val info = create<HTMLDivElement>("div")
                                        info.textContent = "Extra input hidden"
                                        info
                                    }
                                }
                            )

                        }
                    }
                }

            }
        }

        build.flush()
    }

    document.body!!.appendChild(root)
}
