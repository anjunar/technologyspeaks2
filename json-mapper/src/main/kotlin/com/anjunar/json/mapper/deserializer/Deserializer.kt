package com.anjunar.json.mapper.deserializer

import com.anjunar.json.mapper.JsonContext
import com.anjunar.json.mapper.intermediate.model.JsonNode

interface Deserializer<T> {

    fun deserialize(json: JsonNode, context: JsonContext) : T

}