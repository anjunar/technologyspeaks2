package com.anjunar.json.mapper.converter

import com.anjunar.kotlin.universe.ResolvedClass

interface Converter {

    fun toJson(input: Any?, resolvedClass : ResolvedClass) : String

    fun toJava(json: String?, resolvedClass : ResolvedClass) : Any

}