package app.domain.core

import app.domain.core.Link
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("users-list")
class UsersLink(
    override val id: String,
    override val rel: String,
    override val url: String,
    override val method: String = "GET"
) : Link() {

    override val name: String = "Users"

}