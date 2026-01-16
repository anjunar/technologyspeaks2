package com.anjunar.json.mapper.serializers

import com.anjunar.json.mapper.JavaContext
import com.anjunar.json.mapper.intermediate.model.JsonNode
import com.anjunar.json.mapper.intermediate.model.JsonString

class EnumSerializer : Serializer<Enum<*>> {
    override fun serialize(input: Enum<*>, context: JavaContext): JsonNode {
        return JsonString(input.name)
    }
}