package app.domain.security

import jFx2.state.Property
import jFx2.state.StringPropertySerializer
import kotlinx.serialization.Serializable

@Serializable
class PasswordRegister(
    @Serializable(with = StringPropertySerializer::class)
    val email : Property<String> = Property(""),
    @Serializable(with = StringPropertySerializer::class)
    val nickName : Property<String> = Property(""),
    @Serializable(with = StringPropertySerializer::class)
    val password : Property<String> = Property("")
)