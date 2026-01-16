package com.anjunar.technologyspeaks.rest

import com.anjunar.json.mapper.provider.EntityProvider
import com.anjunar.technologyspeaks.SpringContext
import org.springframework.core.convert.TypeDescriptor
import org.springframework.core.convert.converter.ConditionalGenericConverter
import org.springframework.core.convert.converter.GenericConverter
import java.util.*
import kotlin.reflect.full.isSubclassOf

class EntityConverter : ConditionalGenericConverter {

    override fun getConvertibleTypes(): Set<GenericConverter.ConvertiblePair> {
        return setOf(GenericConverter.ConvertiblePair(String::class.java, EntityProvider::class.java))
    }

    override fun matches(sourceType: TypeDescriptor, targetType: TypeDescriptor): Boolean {
        return targetType.type.kotlin.isSubclassOf(EntityProvider::class)
    }

    override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? {
        if (source == null || (source as String).isBlank()) return null

        val entityManager = SpringContext.entityManager()
        return try {
            val id = UUID.fromString(source)
            entityManager.find(targetType.type, id)
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}