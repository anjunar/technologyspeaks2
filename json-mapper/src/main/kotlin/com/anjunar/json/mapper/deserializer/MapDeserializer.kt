package com.anjunar.json.mapper.deserializer

import com.anjunar.json.mapper.EntityLoader
import com.anjunar.json.mapper.provider.EntityProvider
import com.anjunar.json.mapper.JsonContext
import com.anjunar.json.mapper.intermediate.model.JsonArray
import com.anjunar.json.mapper.intermediate.model.JsonNode
import com.anjunar.json.mapper.intermediate.model.JsonObject
import java.util.UUID
import kotlin.reflect.full.isSubclassOf

@Suppress("UNCHECKED_CAST")
class MapDeserializer : Deserializer<Map<String, *>> {
    override fun deserialize(json: JsonNode, context: JsonContext): Map<String, *> {
        return when (json) {
            is JsonObject -> {

                val collection = mutableMapOf<String, Any>()

                val elementResolvedClass = context.type.typeArguments[1]
                DeserializerRegistry.findDeserializer(elementResolvedClass.raw, json)

                for ((key, node) in json.value) {

                    when (node) {
                        is JsonObject -> {
                            val entityCollection = context.instance as Collection<EntityProvider>

                            val idNode = node.value["id"] ?: throw IllegalArgumentException("missing property id")

                            val entityId = UUID.fromString(idNode.value.toString())

                            val entity = entityCollection.find { it.id == entityId } ?: elementResolvedClass.raw.getConstructor().newInstance()

                            val jsonContext = JsonContext(elementResolvedClass, entity, context.graph, context.loader, context, context.name)

                            collection.put(key, DeserializerRegistry
                                .findDeserializer(elementResolvedClass.raw, node)
                                .deserialize(node, jsonContext)
                            )

                        }
                        else -> throw IllegalArgumentException("json array must contain a json object")
                    }

                }

                collection

            }

            else -> throw IllegalStateException("not a json array: $json")
        }
    }
}