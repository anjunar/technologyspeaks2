package app.domain.core

import jFx2.state.ListProperty
import jFx2.state.ListPropertySerializer
import jFx2.state.Property
import jFx2.state.PropertySerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
class User(
    @Serializable(with = PropertySerializer::class)
    val id : Property<String> = Property(""),
    @Serializable(with = PropertySerializer::class)
    val nickName: Property<String> = Property(""),
    @Serializable(with = PropertySerializer::class)
    val image : Property<Media?> = Property(Media()),
    val info: UserInfo = UserInfo(),
    val address: Address = Address(),
    @Serializable(with = ListPropertySerializer::class)
    val emails: ListProperty<Email> = ListProperty()
) {

    override fun toString(): String {
        return "User(id=$id, nickName=$nickName, image=$image, info=$info, address=$address, emails=$emails)"
    }
}
