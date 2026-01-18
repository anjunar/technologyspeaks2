package app.domain.security

import jFx2.state.Property

class WebAuthnRegister(val email : Property<String> = Property(""), val nickname : Property<String> = Property(""))