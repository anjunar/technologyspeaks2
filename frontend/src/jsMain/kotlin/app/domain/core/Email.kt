package app.domain.core

import jFx2.state.Property
import jFx2.state.PropertySerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
class Email(
    @Serializable(with = PropertySerializer::class)
    val id : Property<String>? = null,
    @Serializable(with = PropertySerializer::class)
    val value: Property<String> = Property("")
) {

    override fun toString(): String {
        return "Email(value=${value.get()})"
    }
}