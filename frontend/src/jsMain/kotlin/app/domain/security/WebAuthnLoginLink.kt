package app.domain.security

import app.domain.core.Link
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("login-options")
class WebAuthnLoginLink(override val rel: String, override val url: String, override val method : String = "GET") : Link() {

    override val name : String = "Login"

}