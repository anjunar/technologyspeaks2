package com.anjunar.json.mapper.deserializer

import com.anjunar.json.mapper.JsonContext
import com.anjunar.json.mapper.intermediate.model.JsonNode
import com.anjunar.json.mapper.intermediate.model.JsonString
import java.util.Locale

class LocaleDeserializer : Deserializer<Locale> {
    override fun deserialize(
        json: JsonNode,
        context: JsonContext
    ): Locale {
        when (json) {
            is JsonString -> return Locale.forLanguageTag(json.value)
            else -> throw IllegalArgumentException("json must be a string")
        }
    }
}