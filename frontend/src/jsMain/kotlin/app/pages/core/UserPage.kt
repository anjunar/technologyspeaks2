package app.pages.core

import app.pages.security.PasswordLoginPage
import jFx2.controls.text
import jFx2.core.Component
import jFx2.core.capabilities.NodeScope
import jFx2.core.dom.ElementInsertPoint
import jFx2.layout.div
import jFx2.state.Property
import jFx2.table.DataProvider
import jFx2.table.LazyTableModel
import jFx2.table.cells.TextCell
import jFx2.table.dsl.tableView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

data class UserRow(val id: Long, val nick: String, val email: String)

class UsersProvider : DataProvider<UserRow> {
    override val totalCount = Property<Int?>(100_000)

    override suspend fun loadRange(offset: Int, limit: Int): List<UserRow> {
        // TODO: call backend. For now dummy:
        return (offset until (offset + limit)).map {
            UserRow(it.toLong(), "User$it", "user$it@example.com")
        }
    }
}

class UserPage(override val node: HTMLDivElement) : Component<HTMLDivElement>() {

    context(scope: NodeScope)
    fun afterBuild() {

        val provider = UsersProvider()
        val cs = CoroutineScope(SupervisorJob())
        val model = LazyTableModel(cs, provider, pageSize = 200, prefetchPages = 2)

        tableView(model, rowHeightPx = 28) {
            column(
                header = "ID",
                prefWidthPx = 100,
                value = { it.id },
                cellFactory = {
                    val host = scope.create<HTMLDivElement>("div")
                    TextCell<UserRow, Long>(host)
                }
            )
            column(
                header = "Nick",
                prefWidthPx = 200,
                value = { it.nick },
                cellFactory = {
                    val host = scope.create<HTMLDivElement>("div")
                    TextCell<UserRow, String>(host)
                }
            )
            column(
                header = "Email",
                prefWidthPx = 320,
                value = { it.email },
                cellFactory = {
                    val host = scope.create<HTMLDivElement>("div")
                    TextCell<UserRow, String>(host)
                }
            )
        }
    }

}

context(scope: NodeScope)
fun usersPage(block: context(NodeScope) UserPage.() -> Unit = {}): UserPage {
    val el = scope.create<HTMLDivElement>("div")
    el.classList.add("login-page")
    val c = UserPage(el)
    scope.attach(c)

    val childScope = scope.fork(parent = c.node, owner = c, ctx = scope.ctx, ElementInsertPoint(c.node))

    with(childScope) {
        c.afterBuild()
    }

    block(childScope, c)

    return c
}


