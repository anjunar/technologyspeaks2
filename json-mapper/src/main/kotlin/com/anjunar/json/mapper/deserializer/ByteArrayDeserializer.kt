package com.anjunar.json.mapper.deserializer

import com.anjunar.json.mapper.JsonContext
import com.anjunar.json.mapper.intermediate.model.JsonNode
import com.anjunar.json.mapper.intermediate.model.JsonString
import kotlin.io.encoding.Base64

class ByteArrayDeserializer : Deserializer<ByteArray> {
    override fun deserialize(json: JsonNode, context: JsonContext): ByteArray {
        return when (json) {
            is JsonString -> Base64.decode(json.value)
            else -> throw IllegalArgumentException("json must be a string")
        }
    }
}