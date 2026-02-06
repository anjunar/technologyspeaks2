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
class ArrayDeserializer : Deserializer<Collection<*>> {
    override fun deserialize(json: JsonNode, context: JsonContext): Collection<*> {
        return when (json) {
            is JsonArray -> {

                val collection = when {
                    context.type.kotlin.isSubclassOf(List::class) -> ArrayList<Any>()
                    context.type.kotlin.isSubclassOf(Set::class) -> HashSet<Any>()
                    else -> ArrayList<Any>()
                }

                val elementResolvedClass = context.type.typeArguments[0]
                DeserializerRegistry.findDeserializer(elementResolvedClass.raw)

                for (node in json.value) {

                    when (node) {
                        is JsonObject -> {
                            val entityCollection = context.instance as Collection<EntityProvider>

                            val idNode = node.value["id"] ?: throw IllegalArgumentException("missing property id")

                            val entityId = UUID.fromString(idNode.value.toString())

                            val entity = entityCollection.find { it.id == entityId } ?: elementResolvedClass.raw.getConstructor().newInstance()

                            val jsonContext = JsonContext(elementResolvedClass, entity, context.graph, context.loader, context, context.name)

                            collection.add(DeserializerRegistry
                                .findDeserializer(elementResolvedClass.raw)
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