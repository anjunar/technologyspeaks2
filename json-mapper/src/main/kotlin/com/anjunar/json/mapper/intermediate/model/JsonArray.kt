package com.anjunar.json.mapper.intermediate.model

class JsonArray(override val value : MutableList<JsonNode> = ArrayList()) : JsonNode {

    fun add(node : JsonNode) : JsonArray {
        value.add(node)
        return this
    }

}