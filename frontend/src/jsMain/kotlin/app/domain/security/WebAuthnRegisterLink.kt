package app.domain.security

import app.domain.core.Link
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("web-authn-register-options")
class WebAuthnRegisterLink(
    override val id: String,
    override val rel: String,
    override val url: String,
    override val method : String = "GET") : Link() {

    override val name : String = "Register with WebAuthn"

}