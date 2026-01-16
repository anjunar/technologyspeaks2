package com.anjunar.json.mapper.serializers

import com.anjunar.json.mapper.JavaContext
import com.anjunar.json.mapper.intermediate.model.JsonNode
import com.anjunar.json.mapper.intermediate.model.JsonString
import java.util.Locale

class LocaleSerializer : Serializer<Locale> {
    override fun serialize(input: Locale, context: JavaContext): JsonNode {
        return JsonString(input.displayLanguage)
    }
}