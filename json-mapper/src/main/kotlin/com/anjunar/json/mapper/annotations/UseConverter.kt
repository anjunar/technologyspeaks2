package com.anjunar.json.mapper.annotations

import com.anjunar.json.mapper.converter.Converter
import com.anjunar.json.mapper.converter.JacksonJsonConverter
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
annotation class UseConverter(val value : KClass<out Converter> = JacksonJsonConverter::class)
