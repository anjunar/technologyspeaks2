package jFx2.state

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object StringPropertySerializer : KSerializer<Property<String>> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Property<String>", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Property<String>) {
        encoder.encodeString(value.get())
    }

    override fun deserialize(decoder: Decoder): Property<String> {
        return Property(decoder.decodeString())
    }
}