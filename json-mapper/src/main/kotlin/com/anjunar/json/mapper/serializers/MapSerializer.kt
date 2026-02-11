package com.anjunar.json.mapper.serializers

import com.anjunar.json.mapper.JavaContext
import com.anjunar.json.mapper.intermediate.model.JsonArray
import com.anjunar.json.mapper.intermediate.model.JsonNode
import com.anjunar.json.mapper.intermediate.model.JsonObject

@Suppress("UNCHECKED_CAST")
class MapSerializer : Serializer<Map<String, *>> {
    override fun serialize(input: Map<String, *>, context: JavaContext): JsonNode {

        val nodes = mutableMapOf<String, JsonNode>()
        val jsonArray = JsonObject(nodes)

        for ((key, value) in input) {
            val serializer = SerializerRegistry.find(context.type.typeArguments[1].raw, value!!) as Serializer<Any>

            val javaContext = JavaContext(
                context.type.typeArguments[1],
                context.graph,
                context,
                context.name
            )

            val node = serializer.serialize(value as Any, javaContext)

            nodes.put(key, node)

        }

        return jsonArray
    }
}