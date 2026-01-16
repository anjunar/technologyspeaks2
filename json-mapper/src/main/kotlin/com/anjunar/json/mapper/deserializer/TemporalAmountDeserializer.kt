package com.anjunar.json.mapper.deserializer

import com.anjunar.json.mapper.JsonContext
import com.anjunar.json.mapper.intermediate.model.JsonNode
import com.anjunar.json.mapper.intermediate.model.JsonString
import java.time.Duration
import java.time.temporal.TemporalAmount

class TemporalAmountDeserializer : Deserializer<TemporalAmount> {
    override fun deserialize(
        json: JsonNode,
        context: JsonContext
    ): TemporalAmount {
        return when (json) {
            is JsonString -> Duration.parse(json.value)
            else -> throw IllegalArgumentException("json must be a string")
        }
    }
}