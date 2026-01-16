package com.anjunar.json.mapper.serializers

import com.anjunar.json.mapper.JavaContext
import com.anjunar.json.mapper.intermediate.model.JsonBoolean
import com.anjunar.json.mapper.intermediate.model.JsonNode

class BooleanSerializer : Serializer<Boolean> {
    override fun serialize(input: Boolean, context: JavaContext): JsonNode {
        return JsonBoolean(input)
    }
}