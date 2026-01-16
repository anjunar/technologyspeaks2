package com.anjunar.json.mapper.serializers

import com.anjunar.json.mapper.JavaContext
import com.anjunar.json.mapper.intermediate.model.JsonNode
import com.anjunar.json.mapper.intermediate.model.JsonString
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal

class TemporalSerializer : Serializer<Temporal> {
    override fun serialize(input: Temporal, context: JavaContext): JsonNode {
        return when (input) {
            is java.time.LocalDate -> JsonString(input.format(DateTimeFormatter.ISO_LOCAL_DATE))
            is java.time.LocalDateTime -> JsonString(input.truncatedTo(ChronoUnit.MINUTES).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            is java.time.LocalTime -> JsonString(input.truncatedTo(ChronoUnit.MINUTES).format(DateTimeFormatter.ISO_TIME))
            else -> throw IllegalArgumentException("Unsupported type: ${context.type}")
        }
    }
}