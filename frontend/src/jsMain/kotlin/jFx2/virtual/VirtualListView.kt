package jFx2.virtual

import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.state.JobRegistry
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

class VirtualListView<T>(
    private val dataProvider: RangeDataProvider<T>,
    private val renderer: VirtualListRenderer<T>,
    private val estimateHeightPx: Int = 32,
    private val overscanPx: Int = 200,
    private val prefetchItems: Int = 40,
    private val observeResize: Boolean = true,
    private val coroutineScope: CoroutineScope = JobRegistry.instance.scope
) : Component<HTMLDivElement>() {

    override val node: HTMLDivElement = document.createElement("div") as HTMLDivElement

    private lateinit var viewport: HTMLElement
    private var list: VirtualList<T>? = null

    init {
        node.className = "jfx-virtual-list"
    }

    context(scope: NodeScope)
    fun build(): VirtualListView<T> {
        scope.ui.dom.clear(node)

        viewport = scope.create<HTMLDivElement>("div").apply {
            className = "jfx-virtual-list-viewport"
        }

        node.appendChild(viewport)

        val vlist = VirtualList(
            outerScope = scope,
            viewport = viewport,
            dataProvider = dataProvider,
            renderer = renderer,
            estimateHeightPx = estimateHeightPx,
            overscanPx = overscanPx,
            prefetchItems = prefetchItems,
            observeResize = observeResize,
            coroutineScope = coroutineScope
        )
        list = vlist

        onDispose(vlist)

        return this
    }

    fun invalidate() {
        list?.invalidate()
    }

    override fun dispose() {
        super.dispose()
        list = null
    }
}

context(scope: NodeScope)
fun <T> virtualListView(
    dataProvider: RangeDataProvider<T>,
    renderer: VirtualListRenderer<T>,
    estimateHeightPx: Int = 32,
    overscanPx: Int = 200,
    prefetchItems: Int = 40,
    observeResize: Boolean = true,
    coroutineScope: CoroutineScope = JobRegistry.instance.scope
): VirtualListView<T> {
    val view = VirtualListView(
        dataProvider = dataProvider,
        renderer = renderer,
        estimateHeightPx = estimateHeightPx,
        overscanPx = overscanPx,
        prefetchItems = prefetchItems,
        observeResize = observeResize,
        coroutineScope = coroutineScope
    )

    scope.attach(view)

    return view.build()
}
