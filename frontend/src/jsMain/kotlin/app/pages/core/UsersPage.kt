package app.pages.core

import app.domain.core.Data
import app.domain.core.Table
import app.domain.core.User
import jFx2.client.JsonClient
import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.core.dsl.className
import jFx2.core.template
import jFx2.layout.div
import jFx2.router.PageInfo
import jFx2.state.Property
import jFx2.table.DataProvider
import jFx2.table.LazyTableModel
import jFx2.table.SortState
import jFx2.table.TextCell
import jFx2.table.tableView
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.w3c.dom.CustomEvent
import org.w3c.dom.HTMLDivElement

class UsersProvider : DataProvider<Data<User>> {
    override val totalCount = Property<Int?>(100_000)
    override val sortState: Property<SortState?> = Property(null)

    override suspend fun loadRange(offset: Int, limit: Int): List<Data<User>> {
        val table = JsonClient.invoke<Table<User>>("/service/core/users")

        totalCount.set(table.size)

        return table
            .rows
    }
}

class UsersPage(override val node: HTMLDivElement) : Component<HTMLDivElement>(), PageInfo {

    override val name: String = "Users"
    override val width: Int = -1
    override val height: Int = -1

    context(scope: NodeScope)
    fun afterBuild() {

        val provider = UsersProvider()
        val job = SupervisorJob()
        val cs = CoroutineScope(job)
        onDispose { job.cancel() }
        val model = LazyTableModel(cs, provider, pageSize = 200, prefetchPages = 2)

        template {
            div {
                className { "users-page-table" }

                tableView(model, rowHeightPx = 28) {

                    columnProperty(
                        id = "id",
                        header = "ID",
                        prefWidthPx = 100,
                        valueProperty = { it.data.id },
                        cellFactory = {
                            val host = scope.create<HTMLDivElement>("div")
                            TextCell(host)
                        }
                    )
                    columnProperty(
                        id = "nickName",
                        header = "Nick Name",
                        prefWidthPx = 200,
                        valueProperty = { it.data.nickName },
                        cellFactory = {
                            val host = scope.create<HTMLDivElement>("div")
                            TextCell(host)
                        }
                    )

                    columnProperty(
                        id = "firstName",
                        header = "First Name",
                        prefWidthPx = 200,
                        valueProperty = { it.data.info.firstName },
                        cellFactory = {
                            val host = scope.create<HTMLDivElement>("div")
                            TextCell(host)
                        }
                    )

                    columnProperty(
                        id = "lastName",
                        header = "Last Name",
                        prefWidthPx = 200,
                        valueProperty = { it.data.info.lastName },
                        cellFactory = {
                            val host = scope.create<HTMLDivElement>("div")
                            TextCell(host)
                        }
                    )

                    /*
                                        columnProperty("Email", "Email", 160, valueProperty = { it.email }) {
                                            ComponentCell(
                                                outerScope = scope,
                                                node = scope.create("div"),
                                                render = { row, idx, v ->
                                                    template {
                                                        div {
                                                            text { v.toString() }
                                                        }
                                                    }
                                                }
                                            )
                                        }
                    */

                    onRowDoubleClick { user, _ ->
                        window.history.pushState(null, "", "/core/users/user/" + user.data.id.get())
                        window.dispatchEvent(CustomEvent("popstate"))
                    }
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

