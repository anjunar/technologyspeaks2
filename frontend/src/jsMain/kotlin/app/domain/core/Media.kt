package app.domain.core

import jFx2.state.Property
import jFx2.state.StringPropertySerializer
import kotlinx.serialization.Serializable

@Serializable
class Media(
    @Serializable(with = StringPropertySerializer::class)
    val id : Property<String> = Property(""),
    @Serializable(with = StringPropertySerializer::class)
    val name : Property<String> = Property(""),
    @Serializable(with = StringPropertySerializer::class)
    val contentType : Property<String> = Property(""),
    @Serializable(with = StringPropertySerializer::class)
    val data : Property<String> = Property(""),
    val thumbnail: Thumbnail = Thumbnail()
)
