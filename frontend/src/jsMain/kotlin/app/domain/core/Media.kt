package app.domain.core

import jFx2.state.Property
import jFx2.state.PropertySerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
class Media(
    @Serializable(with = PropertySerializer::class)
    val id : Property<String>? = null,
    @Serializable(with = PropertySerializer::class)
    val name : Property<String> = Property(""),
    @Serializable(with = PropertySerializer::class)
    val contentType : Property<String> = Property(""),
    @Serializable(with = PropertySerializer::class)
    val data : Property<String> = Property(""),
    val thumbnail: Thumbnail = Thumbnail()
) {

    fun dataUrl() : String {
        return "data:${contentType.get()};base64,${data.get()}"
    }

    fun mediaLink() : String {
        return "/service/core/media/${id?.get()}"
    }

    fun thumbnailLink() : String {
        return "/service/core/media/${id?.get()}/thumbnail"
    }

}
