@file:OptIn(ExperimentalUuidApi::class)

package jFx2.forms

import app.domain.core.AbstractEntity
import app.domain.core.Data
import jFx2.controls.heading
import jFx2.controls.image
import jFx2.controls.text
import jFx2.core.capabilities.NodeScope
import jFx2.core.codegen.JfxComponentBuilder
import jFx2.core.dsl.className
import jFx2.core.dsl.onClick
import jFx2.core.dsl.style
import jFx2.core.rendering.condition
import jFx2.core.template
import jFx2.layout.div
import jFx2.modals.Viewport
import jFx2.router.navigateByRel
import jFx2.state.Disposable
import jFx2.state.Property
import jFx2.table.ComponentCell
import jFx2.table.DataProvider
import jFx2.table.LazyTableModel
import jFx2.table.tableView
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.events.Event
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@JfxComponentBuilder(classes = ["combo-box"])
class ComboBox<E : Any>(override val node: HTMLDivElement, override val name: String, val provider : DataProvider<E>) : FormField<List<E>, HTMLDivElement>() {

    val valueProperty = Property<List<E>>(emptyList())

    val placeholderProperty = Property("")

    val openProperty = Property(false)

    val editable = Property(true)

    var render: (context(NodeScope) (E, Int) -> Unit)? = null

    var onItemClick: ((E) -> Unit)? = null

    private val job = SupervisorJob()
    private val cs = CoroutineScope(job)
    private val model = LazyTableModel(cs, provider, pageSize = 200, prefetchPages = 2)

    override var disabled: Boolean
        get() = ! editable.get()
        set(value) {
            editable.set(!value)
        }

    fun placeholder(value: String) {
        placeholderProperty.set(value)
    }

    fun render(function : context(NodeScope) (E, Int) -> Unit) {
        render = function
    }

    context(scope: NodeScope)
    fun afterBuild() {
        val overlayId = Uuid.generateV4().toString()

        val callback: (Event) -> Unit = { openProperty.set(false) }
        window.addEventListener("click", callback)
        onDispose {
            window.removeEventListener("click", callback)
        }

        fun closeOverlay() {
            Viewport.closeOverlayById(overlayId)
        }

        fun openOverlay() {
            val conf = Viewport.Companion.OverlayConf(
                id = overlayId,
                anchor = node,
                offsetXPx = 10.0,
                offsetYPx = 8.0,
                widthPx = 200.0,
                maxHeightPx = 300.0,
            ) {
                onClick { it.stopPropagation() }

                tableView(model, rowHeightPx = 64, headerVisible = false) {

                    columnProperty("image", "Image", 200, value = { it }) {
                        ComponentCell(
                            outerScope = scope,
                            node = scope.create("div"),
                            render = { row, idx, v ->
                                val renderer = render
                                if (renderer != null) {
                                    renderer(row, idx)
                                }
                            }
                        )
                    }

                    onRowDoubleClick { entity, _ ->
                        onItemClick?.invoke(entity)
                    }
                }
            }

            Viewport.addOverlay(conf)
        }

        onDispose(openProperty.observe { open ->
            if (open) openOverlay() else closeOverlay()
        })

        onDispose { closeOverlay() }

        template {


            style {
                position = "relative"
            }

            div {

                style {
                    height = "16px"
                }

                text {
                    if (valueProperty.get().isEmpty()) {
                        placeholderProperty.get()
                    } else {
                        valueProperty.get().joinToString(", ")
                    }
                }

                onClick {
                    it.stopPropagation()
                    openProperty.set(!openProperty.get())
                }

            }

        }
    }

    override fun read(): List<E> {
        return valueProperty.get()
    }

    override fun observeValue(listener: (List<E>) -> Unit): Disposable {
        return valueProperty.observe(listener)
    }
}
