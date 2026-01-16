package com.anjunar.json.mapper.serializers

import com.anjunar.json.mapper.JavaContext
import com.anjunar.json.mapper.intermediate.model.JsonNode

interface Serializer<T> {

    fun serialize(input: T, context : JavaContext) : JsonNode

}