package com.anjunar.json.mapper.deserializer

import com.anjunar.json.mapper.JsonContext
import com.anjunar.json.mapper.intermediate.model.JsonNode
import com.anjunar.json.mapper.intermediate.model.JsonString
import kotlin.reflect.full.isSubclassOf

class NumberDeserializer : Deserializer<Number> {
    override fun deserialize(json: JsonNode, context : JsonContext): Number {
        return when (json) {
            is JsonString -> {
                when {
                    context.type.kotlin.isSubclassOf(Int::class) -> json.value.toInt()
                    context.type.kotlin.isSubclassOf(Long::class) -> json.value.toLong()
                    context.type.kotlin.isSubclassOf(Float::class) -> json.value.toFloat()
                    context.type.kotlin.isSubclassOf(Double::class) -> json.value.toDouble()
                    else -> throw IllegalArgumentException("Unsupported type: ${context.type}")
                }
            }
            else -> throw IllegalArgumentException("json must be a string")
        }
    }
}