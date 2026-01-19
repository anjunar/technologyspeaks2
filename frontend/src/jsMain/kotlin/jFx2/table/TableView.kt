package jFx2.table

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.state.Disposable
import jFx2.state.Property
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

class TableView<R>(
    override val node: Element,
    private val model: LazyTableModel<R>,
    private val columns: List<Column<R, *>>,
    private val rowHeightPx: Int = 28,
    private val overscan: Int = 6,
) : Component<Element>(), Disposable {

    private val selectionIndex = Property<Int?>(null)
    private val focusedIndex = Property<Int?>(null)

    private lateinit var root: HTMLElement
    private lateinit var viewport: HTMLElement
    private lateinit var content: HTMLElement

    private var flow: VirtualTableFlow<R>? = null

    override fun dispose() {
        flow?.dispose()
        flow = null
    }

    context(scope: NodeScope)
    fun render(): Element {
        // root
        root = scope.create<HTMLDivElement>("div").apply {
            className = "jfx-table"
            style.display = "flex"
            style.flexDirection = "column"
            style.width = "100%"
            style.height = "100%"
            style.border = "1px solid #3333"
        }

        // header
        val header = scope.create<HTMLDivElement>("div").apply {
            className = "jfx-table-header"
            style.display = "flex"
            style.flex = "0 0 auto"
            style.borderBottom = "1px solid #3333"
            style.height = "${rowHeightPx}px"
        }
        columns.forEach { col ->
            val h = scope.create<HTMLDivElement>("div").apply {
                className = "jfx-table-header-cell"
                style.flex = "0 0 ${col.prefWidthPx}px"
                style.padding = "0 8px"
                style.display = "flex"
                style.alignItems = "center"
                style.fontWeight = "600"
                textContent = col.header
            }
            header.appendChild(h)
        }

        // viewport (scroll)
        viewport = scope.create<HTMLDivElement>("div").apply {
            className = "jfx-table-viewport"
            style.position = "relative"
            style.overflowX = "auto"
            style.overflowY = "auto"
            style.flex = "1 1 auto"
        }

        // content (absolute children)
        content = scope.create<HTMLDivElement>("div").apply {
            className = "jfx-table-content"
            style.position = "relative"
            style.width = "fit-content" // horizontal scroll if needed
            style.minWidth = "100%"
        }

        viewport.appendChild(content)
        root.appendChild(header)
        root.appendChild(viewport)

        flow = VirtualTableFlow(
            viewport = viewport,
            content = content,
            rowHeightPx = rowHeightPx,
            overscan = overscan,
            columns = columns,
            model = model,
            selectionIndex = selectionIndex,
            focusedIndex = focusedIndex
        )

        return root
    }
}
