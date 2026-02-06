package app.pages.core

import jFx2.controls.text
import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.className
import jFx2.core.template
import jFx2.layout.div
import jFx2.router.PageInfo
import jFx2.state.ListProperty
import jFx2.state.Property
import jFx2.virtual.RangeDataProvider
import jFx2.virtual.virtualList
import kotlinx.coroutines.delay
import org.w3c.dom.HTMLDivElement
import kotlin.math.min

data class DemoItem(val id: Int, val title: String, val body: String)

class DemoRangeProvider(
    private val maxItems: Int = 5000
) : RangeDataProvider<DemoItem> {
    override val items: ListProperty<DemoItem> = ListProperty()
    override val totalCount: Property<Int?> = Property(maxItems)

    override suspend fun ensureRange(from: Int, toInclusive: Int) {
        if (toInclusive < 0) return

        val target = min(toInclusive, maxItems - 1)
        if (target < items.size) return

        delay(30)

        val start = items.size
        val batch = ArrayList<DemoItem>(target - start + 1)
        for (i in start..target) {
            val (title, body) = demoText(i)
            batch += DemoItem(i, title, body)
        }
        items.addAll(batch)
    }

    private fun demoText(i: Int): Pair<String, String> {
        val title = "Item #$i"
        val body = when (i % 7) {
            0 -> "Short line."
            1 -> "A slightly longer line that should wrap on smaller widths."
            2 -> "Multi-line demo text to exercise variable heights. This line is intentionally longer to wrap."
            3 -> "Another paragraph of text. It contains more words to produce a taller row."
            4 -> "Longer entry with two sentences. It should be tall enough to stress measurement and anchor correction."
            5 -> "Tiny."
            else -> "Compact text."
        }
        return title to body
    }
}

class VirtualListDemoPage(override val node: HTMLDivElement) : Component<HTMLDivElement>(), PageInfo {

    override val name: String = "Virtual List"
    override val width: Int = -1
    override val height: Int = -1
    override val resizable: Boolean = false
    override var close: () -> Unit = {}

    context(scope: NodeScope)
    fun afterBuild() {
        val provider = DemoRangeProvider()

        template {
            div {
                className { "virtual-list-demo" }

                virtualList(
                    dataProvider = provider,
                    estimateHeightPx = 44,
                    overscanPx = 240,
                    prefetchItems = 80,
                    renderer = { item, index ->


                        template {

                            div {
                                className { "virtual-demo-item" }
                            }

                            div {
                                className { "virtual-demo-title" }
                                text(item?.title ?: "Loading...")
                            }

                            div {
                                className { "virtual-demo-body" }
                                text(item?.body ?: "Please wait while data is fetched.")
                            }

                        }
                    }
                )
            }
        }
    }
}

context(scope: NodeScope)
fun virtualListDemoPage(block: context(NodeScope) VirtualListDemoPage.() -> Unit = {}): VirtualListDemoPage {
    val el = scope.create<HTMLDivElement>("div")
    el.classList.add("virtual-list-demo-page")
    val c = VirtualListDemoPage(el)
    scope.attach(c)

    val childScope = scope.fork(parent = c.node, owner = c, ctx = scope.ctx, ElementInsertPoint(c.node))

    with(childScope) {
        c.afterBuild()
    }

    block(childScope, c)

    return c
}
