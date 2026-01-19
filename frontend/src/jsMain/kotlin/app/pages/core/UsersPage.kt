package app.pages.core

import jFx2.controls.text
import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.className
import jFx2.layout.div
import jFx2.state.Property
import jFx2.table.DataProvider
import jFx2.table.LazyTableModel
import jFx2.table.SortState
import jFx2.table.cells.ComponentCell
import jFx2.table.cells.TextCell
import jFx2.table.dsl.tableView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.w3c.dom.HTMLDivElement

data class UserRow(val id: Property<Long>, val nick: Property<String>, val email: Property<String>)

class UsersProvider : DataProvider<UserRow> {
    override val totalCount = Property<Int?>(100_000)
    override val sortState: Property<SortState?> = Property(null)

    override suspend fun loadRange(offset: Int, limit: Int): List<UserRow> {
        // TODO: call backend. For now dummy:
        return (offset until (offset + limit)).map {
            UserRow(Property(it.toLong()), Property("User$it"), Property("user$it@example.com"))
        }
    }
}

class UsersPage(override val node: HTMLDivElement) : Component<HTMLDivElement>() {

    context(scope: NodeScope)
    fun afterBuild() {

        val provider = UsersProvider()
        val job = SupervisorJob()
        val cs = CoroutineScope(job)
        onDispose { job.cancel() }
        val model = LazyTableModel(cs, provider, pageSize = 200, prefetchPages = 2)

        div {
            className { "users-page-table" }

            tableView(model, rowHeightPx = 28) {
                columnProperty(
                    id = "id",
                    header = "ID",
                    prefWidthPx = 100,
                    valueProperty = { it.id },
                    cellFactory = {
                        val host = scope.create<HTMLDivElement>("div")
                        TextCell<UserRow, Long>(host)
                    }
                )
                columnProperty(
                    id = "nick",
                    header = "Nick",
                    prefWidthPx = 200,
                    valueProperty = { it.nick },
                    cellFactory = {
                        val host = scope.create<HTMLDivElement>("div")
                        TextCell<UserRow, String>(host)
                    }
                )
                columnProperty("Email", "Email", 160, valueProperty = { it.email }) {
                    ComponentCell(
                        outerScope = scope,
                        node = scope.create("div"),
                        render = { row, idx, v ->
                            div {
                                text { v.toString() }
                            }
                        }
                    )
                }
            }
        }

    }

}

context(scope: NodeScope)
fun usersPage(block: context(NodeScope) UsersPage.() -> Unit = {}): UsersPage {
    val el = scope.create<HTMLDivElement>("div")
    el.classList.add("users-page")
    val c = UsersPage(el)
    scope.attach(c)

    val childScope = scope.fork(parent = c.node, owner = c, ctx = scope.ctx, ElementInsertPoint(c.node))

    with(childScope) {
        c.afterBuild()
    }

    block(childScope, c)

    return c
}

