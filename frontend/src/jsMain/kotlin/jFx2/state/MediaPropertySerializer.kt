package jFx2.state

import app.domain.core.Media
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object MediaPropertySerializer : KSerializer<Property<Media>> {
    private val mediaSerializer: KSerializer<Media> = Media.serializer()

    override val descriptor: SerialDescriptor = mediaSerializer.descriptor

    override fun serialize(encoder: Encoder, value: Property<Media>) {
        encoder.encodeSerializableValue(mediaSerializer, value.get())
    }

    override fun deserialize(decoder: Decoder): Property<Media> {
        return Property(decoder.decodeSerializableValue(mediaSerializer))
    }
}
