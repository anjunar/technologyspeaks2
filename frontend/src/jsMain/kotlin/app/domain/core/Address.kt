package app.domain.core

import jFx2.state.Property
import jFx2.state.StringPropertySerializer
import kotlinx.serialization.Serializable

@Serializable
class Address(
    @Serializable(with = StringPropertySerializer::class)
    val id : Property<String> = Property(""),
    @Serializable(with = StringPropertySerializer::class)
    val street: Property<String> = Property(""),
    @Serializable(with = StringPropertySerializer::class)
    val number: Property<String> = Property(""),
    @Serializable(with = StringPropertySerializer::class)
    val zipCode: Property<String> = Property(""),
    @Serializable(with = StringPropertySerializer::class)
    val country: Property<String> = Property("")) {

    override fun toString(): String {
        return "Address(id=$id, street=$street, number=$number, zipCode=$zipCode, country=$country)"
    }
}