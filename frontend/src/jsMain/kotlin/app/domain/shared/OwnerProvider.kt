package app.domain.shared

import app.domain.core.User
import jFx2.state.Property

interface OwnerProvider {

    val user : Property<User>?

}