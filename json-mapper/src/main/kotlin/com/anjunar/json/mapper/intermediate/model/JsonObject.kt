package com.anjunar.json.mapper.intermediate.model

import com.anjunar.json.mapper.intermediate.JsonGenerator

class JsonObject(override val value: MutableMap<String, JsonNode> = HashMap()) : JsonNode {

    fun encode() : String {
        return JsonGenerator.generate(this)
    }

    fun getString(key: String) : String {
        return value[key]?.value as String
    }

    fun getJsonObject(key: String) : JsonObject {
        return value[key] as JsonObject
    }

    fun put(key: String, node: JsonNode) : JsonObject {
        value[key] = node
        return this
    }

    fun put(key: String, valueString: String) : JsonObject {
        value[key] = JsonString(valueString)
        return this
    }

    fun put(key: String, valueBoolean: Boolean) : JsonObject {
        value[key] = JsonBoolean(valueBoolean)
        return this
    }

    fun put(key : String, valueNumber : Number) : JsonObject {
        value[key] = JsonNumber(valueNumber.toString())
        return this
    }


}