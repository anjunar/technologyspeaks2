package com.anjunar.json.mapper.serializers

import java.time.temporal.Temporal
import java.time.temporal.TemporalAmount
import java.util.Locale
import java.util.UUID
import kotlin.reflect.full.isSubclassOf

@Suppress("UNCHECKED_CAST")
object SerializerRegistry {

    fun <T : Any> find(clazz: Class<T>): Serializer<T> {

        val klass = clazz.kotlin

        return when {
            klass.isSubclassOf(Collection::class) -> ArraySerializer()
            klass.isSubclassOf(Boolean::class) -> BooleanSerializer()
            klass == ByteArray::class -> ByteArraySerializer()
            klass.isSubclassOf(Enum::class) -> EnumSerializer()
            klass.isSubclassOf(Locale::class) -> LocaleSerializer()
            klass.isSubclassOf(Number::class) -> NumberSerializer()
            klass.isSubclassOf(String::class) -> StringSerializer()
            klass.isSubclassOf(TemporalAmount::class) -> TemporalAmountSerializer()
            klass.isSubclassOf(Temporal::class) -> TemporalSerializer()
            klass.isSubclassOf(UUID::class) -> UUIDSerializer()

            else -> {
                BeanSerializer()
            }
        } as Serializer<T>

    }

}