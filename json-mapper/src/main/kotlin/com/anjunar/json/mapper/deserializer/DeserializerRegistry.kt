package com.anjunar.json.mapper.deserializer

import com.anjunar.json.mapper.intermediate.model.JsonArray
import com.anjunar.json.mapper.intermediate.model.JsonBoolean
import com.anjunar.json.mapper.intermediate.model.JsonNode
import com.anjunar.json.mapper.intermediate.model.JsonNumber
import com.anjunar.json.mapper.intermediate.model.JsonObject
import com.anjunar.json.mapper.intermediate.model.JsonString
import java.time.temporal.Temporal
import java.time.temporal.TemporalAmount
import java.util.Locale
import java.util.UUID
import kotlin.reflect.full.isSubclassOf

@Suppress("UNCHECKED_CAST")
object DeserializerRegistry {

    fun <T : Any> findDeserializer(clazz: Class<T>, node : JsonNode): Deserializer<T> {

        val klass = clazz.kotlin

        return when(node) {
            is JsonNumber -> NumberDeserializer()
            is JsonBoolean -> BooleanDeserializer()
            is JsonArray -> ArrayDeserializer()

            is JsonObject -> {
                when {
                    klass.isSubclassOf(Map::class) -> MapDeserializer()
                    klass.isSubclassOf(Any::class) -> BeanDeserializer()
                    else -> throw IllegalArgumentException("Unsupported type: $klass")
                }
            }
            is JsonString -> {
                when {
                    klass == ByteArray::class -> ByteArrayDeserializer()
                    klass.isSubclassOf(Enum::class) -> EnumDeserializer()
                    klass.isSubclassOf(Locale::class) -> LocaleDeserializer()
                    klass.isSubclassOf(String::class) -> StringDeserializer()
                    klass.isSubclassOf(TemporalAmount::class) -> TemporalAmountDeserializer()
                    klass.isSubclassOf(Temporal::class) -> TemporalDeserializer()
                    klass.isSubclassOf(UUID::class) -> UUIDDeserializer()
                    else -> throw IllegalArgumentException("Unsupported type: $klass")
                }
            }
            is JsonNode -> throw IllegalArgumentException("Unsupported type: $klass")
        } as Deserializer<T>


    }

}