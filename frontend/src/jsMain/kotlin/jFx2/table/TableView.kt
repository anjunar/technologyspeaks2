package jFx2.table

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.state.Property
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

class TableView<R>(
    private val model: LazyTableModel<R>,
    private val columns: List<Column<R, *>>,
    private val rowHeightPx: Int = 28,
    private val overscan: Int = 6
) : Component<HTMLDivElement>() {

    override val node: HTMLDivElement

    val selectedIndex = Property<Int?>(null)
    val focusedIndex = Property<Int?>(null)

    private lateinit var viewport: HTMLElement
    private lateinit var content: HTMLElement

    private var flow: VirtualTableFlow<R>? = null

    init {
        // node gets built later in build() with a NodeScope; but Component requires val node.
        // We'll create a placeholder and then populate in build().
        node = document.createElement("div") as HTMLDivElement
        node.className = "jfx-table"
    }

    context(scope: NodeScope)
    fun build(): TableView<R> {
        scope.ui.dom.clear(node)

        node.style.display = "flex"
        node.style.flexDirection = "column"
        node.style.width = "100%"
        node.style.height = "100%"
        node.style.border = "1px solid #3333"

        // header
        val header = scope.create<HTMLDivElement>("div").apply {
            className = "jfx-table-header"
            style.display = "flex"
            style.flex = "0 0 auto"
            style.height = "${rowHeightPx}px"
            style.borderBottom = "1px solid #3333"
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

        // viewport
        viewport = scope.create<HTMLDivElement>("div").apply {
            className = "jfx-table-viewport"
            style.position = "relative"
            style.overflowX = "auto"
            style.overflowY = "auto"
            style.flex = "1 1 auto"
        }

        // content
        content = scope.create<HTMLDivElement>("div").apply {
            className = "jfx-table-content"
            style.position = "relative"
            style.width = "fit-content"
            style.minWidth = "100%"
        }

        viewport.appendChild(content)
        node.appendChild(header)
        node.appendChild(viewport)

        // create row pool NOW (with real NodeScope available)
        val poolSize = computeInitialPoolSize(viewport, rowHeightPx, overscan)
        val pool = buildRowPool(poolSize)

        val f = VirtualTableFlow(
            viewport = viewport,
            content = content,
            rowHeightPx = rowHeightPx,
            overscan = overscan,
            columns = columns,
            model = model,
            selectedIndex = selectedIndex,
            focusedIndex = focusedIndex
        )
        f.setRows(pool)

        flow = f
        onDispose(f)

        return this
    }

    override fun dispose() {
        super.dispose()
        flow = null
    }

    private fun computeInitialPoolSize(viewport: HTMLElement, rowHeightPx: Int, overscan: Int): Int {
        val vh = viewport.clientHeight.takeIf { it > 0 } ?: 600
        val visible = kotlin.math.ceil(vh.toDouble() / rowHeightPx).toInt()
        return visible + overscan * 2
    }

    context(scope: NodeScope)
    private fun buildRowPool(poolSize: Int): List<VirtualTableFlow.VirtualRow<R>> {
        val rows = ArrayList<VirtualTableFlow.VirtualRow<R>>(poolSize)

        repeat(poolSize) { _ ->
            val rowEl = scope.create<HTMLDivElement>("div").unsafeCast<HTMLElement>().apply {
                className = "jfx-table-row"
                style.position = "absolute"
                style.left = "0px"
                style.right = "0px"
                style.height = "${rowHeightPx}px"
                style.display = "flex"
            }

            val cellHolders = columns.map { col ->
                val host = scope.create<HTMLDivElement>("div").unsafeCast<HTMLElement>().apply {
                    className = "jfx-table-cell-host"
                    style.flex = "0 0 ${col.prefWidthPx}px"
                    style.overflowX = "hidden"
                    style.overflowY = "hidden"
                    style.whiteSpace = "nowrap"
                    style.textOverflow = "ellipsis"
                    style.padding = "0 8px"
                    style.display = "flex"
                    style.alignItems = "center"
                }
                rowEl.appendChild(host)

                @Suppress("UNCHECKED_CAST")
                val typedCol = col as Column<R, Any?>

                // Build cell with real NodeScope (important!)
                val cell = typedCol.cellFactory(scope)

                host.appendChild(cell.node)

                VirtualTableFlow.cellHolder(typedCol, cell, host)
            }

            rowEl.addEventListener("click", {
                val idx = rows.firstOrNull { it.node === rowEl }?.boundIndex ?: -1
                if (idx >= 0) selectedIndex.set(idx)
            })

            content.appendChild(rowEl)
            rows += VirtualTableFlow.virtualRow(rowEl, cellHolders)
        }

        return rows
    }
}
