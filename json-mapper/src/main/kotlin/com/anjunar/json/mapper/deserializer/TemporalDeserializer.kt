package com.anjunar.json.mapper.deserializer

import com.anjunar.json.mapper.JsonContext
import com.anjunar.json.mapper.intermediate.model.JsonNode
import com.anjunar.json.mapper.intermediate.model.JsonString
import java.time.temporal.Temporal
import kotlin.reflect.full.functions

class TemporalDeserializer : Deserializer<Temporal> {
    override fun deserialize(
        json: JsonNode,
        context: JsonContext
    ): Temporal {

        val parseMethod = context.type.kotlin::functions.get().find { it.name == "parse" }

        return when (json) {
            is JsonString -> parseMethod?.call(json.value) as Temporal
            else -> throw IllegalArgumentException("json must be a string")
        }
    }
}