package com.anjunar.json.mapper.serializers

import com.anjunar.json.mapper.JavaContext
import com.anjunar.json.mapper.intermediate.model.JsonNode
import com.anjunar.json.mapper.intermediate.model.JsonString
import kotlin.io.encoding.Base64

class ByteArraySerializer : Serializer<ByteArray> {
    override fun serialize(input: ByteArray, context: JavaContext): JsonNode {
        return JsonString(Base64.encode(input))
    }
}