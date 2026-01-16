package com.anjunar.json.mapper.serializers

import com.anjunar.json.mapper.JavaContext
import com.anjunar.json.mapper.intermediate.model.JsonNode
import com.anjunar.json.mapper.intermediate.model.JsonString

class StringSerializer : Serializer<String> {
    override fun serialize(input: String, context: JavaContext): JsonNode {
        return JsonString(input)
    }
}