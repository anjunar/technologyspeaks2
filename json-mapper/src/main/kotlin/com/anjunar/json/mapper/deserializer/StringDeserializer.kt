package com.anjunar.json.mapper.deserializer

import com.anjunar.json.mapper.JsonContext
import com.anjunar.json.mapper.intermediate.model.JsonNode
import com.anjunar.json.mapper.intermediate.model.JsonString

class StringDeserializer : Deserializer<String> {
    override fun deserialize(
        json: JsonNode,
        context: JsonContext
    ): String {
        when (json) {
            is JsonString -> return json.value
            else -> throw IllegalArgumentException("json must be a string")
        }
    }
}