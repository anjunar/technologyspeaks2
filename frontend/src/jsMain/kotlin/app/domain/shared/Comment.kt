package app.domain.shared

import app.domain.core.User
import jFx2.state.Property
import jFx2.state.PropertySerializer
import kotlinx.serialization.Serializable

@Serializable
class Comment(
    @Serializable(with = PropertySerializer::class)
    val id: Property<String>? = null,
    @Serializable(with = PropertySerializer::class)
    val user: Property<User>? = null,
    @Serializable(with = PropertySerializer::class)
    val editor: Property<String> = Property("")
)

