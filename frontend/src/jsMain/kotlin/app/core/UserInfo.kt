package app.core

import jFx2.state.Property
import jFx2.state.StringPropertySerializer
import kotlinx.serialization.Serializable

@Serializable
class UserInfo(
    @Serializable(with = StringPropertySerializer::class)
    val firstName: Property<String> = Property(""),
    @Serializable(with = StringPropertySerializer::class)
    val lastName: Property<String> = Property("")) {

    override fun toString(): String {
        return "UserInfo(firstName=${firstName.get()}, lastName=${lastName.get()})"
    }
}