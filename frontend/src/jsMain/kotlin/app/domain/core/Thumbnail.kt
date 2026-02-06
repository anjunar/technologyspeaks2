package app.domain.core

import jFx2.state.Property
import jFx2.state.PropertySerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
open class Thumbnail(
    @Serializable(with = PropertySerializer::class)
    val id : Property<String> = Property(""),
    @Serializable(with = PropertySerializer::class)
    val name : Property<String> = Property(""),
    @Serializable(with = PropertySerializer::class)
    val contentType : Property<String> = Property(""),
    @Serializable(with = PropertySerializer::class)
    val data : Property<String> = Property("")
) {

    fun dataUrl() : String {
        return "data:${contentType.get()};base64,${data.get()}"
    }

}