package com.anjunar.json.mapper.serializers

import com.anjunar.json.mapper.JavaContext
import com.anjunar.json.mapper.intermediate.model.JsonNode
import com.anjunar.json.mapper.intermediate.model.JsonString
import java.time.temporal.TemporalAmount

class TemporalAmountSerializer : Serializer<TemporalAmount> {
    override fun serialize(input: TemporalAmount, context: JavaContext): JsonNode {
        return JsonString(input.toString())
    }
}