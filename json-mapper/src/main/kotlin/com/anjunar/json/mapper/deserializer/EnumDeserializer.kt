package com.anjunar.json.mapper.deserializer

import com.anjunar.json.mapper.JsonContext
import com.anjunar.json.mapper.intermediate.model.JsonNode

@Suppress("UNCHECKED_CAST")
class EnumDeserializer : Deserializer<Enum<*>> {
    override fun deserialize(
        json: JsonNode,
        context: JsonContext
    ): Enum<*> {
        val enumConstants = context.type.raw.enumConstants as Array<Enum<*>>
        return enumConstants.find { enum -> enum.name == json.value } ?: throw IllegalArgumentException("invalid enum value")
    }
}