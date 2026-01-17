package app.domain

import app.domain.core.Link
import app.domain.core.User
import jFx2.state.ListProperty
import jFx2.state.ListPropertySerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Application(
    val user: User,
    @SerialName($$"$links")
    @Serializable(with = ListPropertySerializer::class)
    val links: ListProperty<Link>
)