package com.anjunar.json.mapper.deserializer

import java.time.temporal.Temporal
import java.time.temporal.TemporalAmount
import java.util.Locale
import java.util.UUID
import kotlin.reflect.full.isSubclassOf

@Suppress("UNCHECKED_CAST")
object DeserializerRegistry {

    fun <T : Any> findDeserializer(clazz: Class<T>): Deserializer<T> {

        val klass = clazz.kotlin

        return when {
            klass.isSubclassOf(Collection::class) -> ArrayDeserializer()
            klass.isSubclassOf(Boolean::class) -> BooleanDeserializer()
            klass == ByteArray::class -> ByteArrayDeserializer()
            klass.isSubclassOf(Enum::class) -> EnumDeserializer()
            klass.isSubclassOf(Locale::class) -> LocaleDeserializer()
            klass.isSubclassOf(Number::class) -> NumberDeserializer()
            klass.isSubclassOf(String::class) -> StringDeserializer()
            klass.isSubclassOf(TemporalAmount::class) -> TemporalAmountDeserializer()
            klass.isSubclassOf(Temporal::class) -> TemporalDeserializer()
            klass.isSubclassOf(UUID::class) -> UUIDDeserializer()

            else -> {
                BeanDeserializer()
            }
        } as Deserializer<T>

    }

}