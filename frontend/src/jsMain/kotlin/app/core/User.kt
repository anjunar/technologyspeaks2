package app.core

import jFx2.state.Property
import jFx2.state.StringPropertySerializer
import kotlinx.serialization.Serializable

@Serializable
class User(
    @Serializable(with = StringPropertySerializer::class)
    val nickName: Property<String> = Property(""),
    val userInfo: UserInfo = UserInfo()) {

    override fun toString(): String {
        return "User(nickname=${nickName.get()}, userInfo=${userInfo})"
    }
}