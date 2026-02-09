package app.domain.time

import app.domain.core.AbstractEntity
import app.domain.core.User
import app.domain.shared.Like
import jFx2.state.ListProperty
import jFx2.state.ListPropertySerializer
import jFx2.state.Property
import jFx2.state.PropertySerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
class Post (
    @Serializable(with = PropertySerializer::class)
    override val id : Property<String>? = null,
    @Serializable(with = PropertySerializer::class)
    val user : Property<User>? = null,
    @Serializable(with = PropertySerializer::class)
    val editor: Property<String> = Property(""),
    @Serializable(with = ListPropertySerializer::class)
    val likes : ListProperty<Like> = ListProperty()
) : AbstractEntity
