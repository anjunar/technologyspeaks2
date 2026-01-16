package com.anjunar.json.mapper.serializers

import com.anjunar.json.mapper.JavaContext
import com.anjunar.json.mapper.intermediate.model.JsonArray
import com.anjunar.json.mapper.intermediate.model.JsonNode

@Suppress("UNCHECKED_CAST")
class ArraySerializer : Serializer<Collection<*>> {
    override fun serialize(input: Collection<*>, context: JavaContext): JsonNode {

        val serializer = SerializerRegistry.find(context.type.typeArguments[0].raw) as Serializer<Any>

        val nodes  = ArrayList<JsonNode>()

        val jsonArray = JsonArray(nodes)

        for (any in input) {

            val javaContext = JavaContext(
                context.type.typeArguments[0],
                context.graph,
                context,
                context.name
            )

            val node = serializer.serialize(any as Any, javaContext)

            nodes.add(node)

        }

        return jsonArray
    }
}