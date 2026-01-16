package com.anjunar.json.mapper.deserializer

import com.anjunar.json.mapper.JsonContext
import com.anjunar.json.mapper.intermediate.model.JsonBoolean
import com.anjunar.json.mapper.intermediate.model.JsonNode

class BooleanDeserializer : Deserializer<Boolean> {
    override fun deserialize(json: JsonNode, context: JsonContext): Boolean {
        return when (json) {
            is JsonBoolean -> json.value
            else -> throw IllegalArgumentException("json must be a boolean")
        }
    }
}