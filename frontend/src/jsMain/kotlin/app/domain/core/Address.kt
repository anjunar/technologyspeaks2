package app.domain.core

import jFx2.state.Property
import jFx2.state.PropertySerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
class Address(
    @Serializable(with = PropertySerializer::class)
    val id : Property<String>? = null,
    @Serializable(with = PropertySerializer::class)
    val street: Property<String> = Property(""),
    @Serializable(with = PropertySerializer::class)
    val number: Property<String> = Property(""),
    @Serializable(with = PropertySerializer::class)
    val zipCode: Property<String> = Property(""),
    @Serializable(with = PropertySerializer::class)
    val country: Property<String> = Property("")) {

    override fun toString(): String {
        return "Address(id=$id, street=$street, number=$number, zipCode=$zipCode, country=$country)"
    }
}