package com.anjunar.json.mapper.deserializer

import com.anjunar.json.mapper.JsonContext
import com.anjunar.json.mapper.intermediate.model.JsonNode
import com.anjunar.json.mapper.intermediate.model.JsonString
import java.util.UUID

class UUIDDeserializer : Deserializer<UUID> {
    override fun deserialize(json: JsonNode, context: JsonContext): UUID {
        return when (json) {
            is JsonString -> UUID.fromString(json.value)
            else -> throw IllegalArgumentException("json must be a string")
        }
    }
}