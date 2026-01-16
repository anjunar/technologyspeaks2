package com.anjunar.json.mapper.converter

import com.anjunar.json.mapper.ObjectMapperProvider
import com.anjunar.kotlin.universe.ResolvedClass

class JacksonJsonConverter : Converter {

    override fun toJson(input: Any?, resolvedClass: ResolvedClass): String {
        return ObjectMapperProvider.mapper.writeValueAsString(input)
    }

    override fun toJava(json: String?, resolvedClass: ResolvedClass): Any {
        return ObjectMapperProvider.mapper.readerFor(resolvedClass.raw).readValue<Any>(json)
    }

}