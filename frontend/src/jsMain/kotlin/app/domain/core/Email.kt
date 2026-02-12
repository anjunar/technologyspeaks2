package app.domain.core

import jFx2.state.ListProperty
import jFx2.state.ListPropertySerializer
import jFx2.state.Property
import jFx2.state.PropertySerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Email(
    @Serializable(with = PropertySerializer::class)
    override var id : Property<String>? = null,
    @Serializable(with = PropertySerializer::class)
    val value: Property<String> = Property(""),
    @SerialName($$"$links")
    @Serializable(with = ListPropertySerializer::class)
    override val links : ListProperty<Link> = ListProperty()
)  : AbstractEntity {

    override fun toString(): String {
        return "Email(value=${value.get()})"
    }
}