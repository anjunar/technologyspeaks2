package app.domain.core

import jFx2.state.ListProperty
import jFx2.state.ListPropertySerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Data<E>(
    val data : E,
    @SerialName($$"$links")
    @Serializable(with = ListPropertySerializer::class)
    val links : ListProperty<Link> = ListProperty()
)