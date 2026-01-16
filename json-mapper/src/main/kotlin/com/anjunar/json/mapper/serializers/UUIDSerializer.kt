package com.anjunar.json.mapper.serializers

import com.anjunar.json.mapper.JavaContext
import com.anjunar.json.mapper.intermediate.model.JsonNode
import com.anjunar.json.mapper.intermediate.model.JsonString
import java.util.UUID

class UUIDSerializer : Serializer<UUID> {
    override fun serialize(input: UUID, context: JavaContext): JsonNode {
        return JsonString(input.toString())
    }
}