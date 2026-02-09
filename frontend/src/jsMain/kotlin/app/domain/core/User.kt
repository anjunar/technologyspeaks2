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
    override val id : Property<String>? = null,
    @Serializable(with = PropertySerializer::class)
    val nickName: Property<String> = Property(""),
    @Serializable(with = PropertySerializer::class)
    val image : Property<Media?> = Property(Media()),
    var info: UserInfo? = null,
    var address: Address? = null,
    @Serializable(with = ListPropertySerializer::class)
    val emails: ListProperty<Email> = ListProperty()
)  : AbstractEntity {

    override fun toString(): String {
        return "User(id=$id, nickName=$nickName, image=$image, info=$info, address=$address, emails=$emails)"
    }
}
