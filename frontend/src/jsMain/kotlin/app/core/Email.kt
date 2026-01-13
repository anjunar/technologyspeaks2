package app.core

import jFx2.state.Property
import jFx2.state.StringPropertySerializer
import kotlinx.serialization.Serializable

@Serializable
class Email(
    @Serializable(with = StringPropertySerializer::class)
    val value: Property<String> = Property("")
) {

    override fun toString(): String {
        return "Email(value=${value.get()})"
    }
}