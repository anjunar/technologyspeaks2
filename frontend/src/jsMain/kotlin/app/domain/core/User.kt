package app.domain.core

import jFx2.state.ListProperty
import jFx2.state.ListPropertySerializer
import jFx2.state.MediaPropertySerializer
import jFx2.state.Property
import jFx2.state.StringPropertySerializer
import kotlinx.serialization.Serializable

@Serializable
class User(
    @Serializable(with = StringPropertySerializer::class)
    val id : Property<String> = Property(""),
    @Serializable(with = StringPropertySerializer::class)
    val nickName: Property<String> = Property(""),
    @Serializable(with = MediaPropertySerializer::class)
    val image : Property<Media?> = Property(Media()),
    val info: UserInfo = UserInfo(),
    @Serializable(with = ListPropertySerializer::class)
    val emails: ListProperty<Email> = ListProperty()
) {

    override fun toString(): String {
        return "User(nickname=${nickName.get()}, userInfo=${info}), emails=$emails"
    }
}
