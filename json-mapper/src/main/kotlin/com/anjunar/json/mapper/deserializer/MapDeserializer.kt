package com.anjunar.json.mapper.deserializer

import com.anjunar.json.mapper.EntityLoader
import com.anjunar.json.mapper.provider.EntityProvider
import com.anjunar.json.mapper.JsonContext
import com.anjunar.json.mapper.intermediate.model.JsonArray
import com.anjunar.json.mapper.intermediate.model.JsonNode
import com.anjunar.json.mapper.intermediate.model.JsonNumber
import com.anjunar.json.mapper.intermediate.model.JsonObject
import com.anjunar.json.mapper.intermediate.model.JsonString
import java.util.UUID
import kotlin.reflect.full.isSubclassOf

@Suppress("UNCHECKED_CAST")
class MapDeserializer : Deserializer<Map<String, *>> {
    override fun deserialize(json: JsonNode, context: JsonContext): Map<String, *> {
        return when (json) {
            is JsonObject -> {

                val collection = mutableMapOf<String, Any>()
                val elementResolvedClass = context.type.typeArguments[1]

                for ((key, node) in json.value) {
                    val entityCollection = context.instance as Map<String, Any>

                    val entity = entityCollection[key] ?: elementResolvedClass.raw.getConstructor().newInstance()

                    val jsonContext = JsonContext(elementResolvedClass, entity, context.graph, context.loader, context, context.name)

                    collection.put(key, DeserializerRegistry
                        .findDeserializer(elementResolvedClass.raw, node)
                        .deserialize(node, jsonContext)
                    )


                }

                collection

            }

            else -> throw IllegalStateException("not a json array: $json")
        }
    }
}