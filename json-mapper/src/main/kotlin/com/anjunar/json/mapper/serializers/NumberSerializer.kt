package com.anjunar.json.mapper.serializers

import com.anjunar.json.mapper.JavaContext
import com.anjunar.json.mapper.intermediate.model.JsonNode
import com.anjunar.json.mapper.intermediate.model.JsonNumber

class NumberSerializer : Serializer<Number> {
    override fun serialize(input: Number, context: JavaContext): JsonNode {
        return JsonNumber(input.toString())
    }
}