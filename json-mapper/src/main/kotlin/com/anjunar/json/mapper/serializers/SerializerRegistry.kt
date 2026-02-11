package com.anjunar.json.mapper.serializers

import com.anjunar.json.mapper.intermediate.model.JsonNode
import java.time.temporal.Temporal
import java.time.temporal.TemporalAmount
import java.util.Locale
import java.util.UUID
import kotlin.reflect.full.isSubclassOf

@Suppress("UNCHECKED_CAST")
object SerializerRegistry {

    fun <T : Any> find(clazz: Class<T>, instance : Any): Serializer<T> {

        val klass = clazz.kotlin

        return when(instance) {
            is String -> StringSerializer()
            is Collection<*> -> ArraySerializer()
            is Boolean -> BooleanSerializer()
            is ByteArray -> ByteArraySerializer()
            is Enum<*> -> EnumSerializer()
            is Locale -> LocaleSerializer()
            is Map<*,*> -> MapSerializer()
            is UUID -> UUIDSerializer()
            is Number -> NumberSerializer()
            is TemporalAmount -> TemporalAmountSerializer()
            is Temporal -> TemporalSerializer()
            else -> {
                BeanSerializer()
            }
        } as Serializer<T>


    }

}