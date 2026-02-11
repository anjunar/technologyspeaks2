package app.domain.core

import jFx2.state.Property
import jFx2.state.PropertySerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
class UserInfo(
    @Serializable(with = PropertySerializer::class)
    override var id : Property<String>? = null,
    @Serializable(with = PropertySerializer::class)
    val firstName: Property<String> = Property(""),
    @Serializable(with = PropertySerializer::class)
    val lastName: Property<String> = Property(""),
    @Serializable(with = PropertySerializer::class)
    val birthDate: Property<String> = Property(""))  : AbstractEntity {

    override fun toString(): String {
        return "UserInfo(firstName=${firstName.get()}, lastName=${lastName.get()})"
    }
}