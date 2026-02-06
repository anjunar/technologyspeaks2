package com.anjunar.json.mapper

import com.anjunar.json.mapper.deserializer.DeserializerRegistry
import com.anjunar.json.mapper.intermediate.JsonGenerator
import com.anjunar.json.mapper.intermediate.JsonParser
import com.anjunar.json.mapper.intermediate.model.JsonNode
import com.anjunar.json.mapper.serializers.Serializer
import com.anjunar.json.mapper.serializers.SerializerRegistry
import com.anjunar.kotlin.universe.ResolvedClass
import jakarta.persistence.EntityGraph

@Suppress("UNCHECKED_CAST")
object JsonMapper {

    fun deserialize(jsonNode: JsonNode, instance : Any, type: ResolvedClass, graph : EntityGraph<*>?, loader : EntityLoader) : Any {
        val deserializer = DeserializerRegistry.findDeserializer(type.raw)

        return deserializer.deserialize(jsonNode, JsonContext(type, instance, graph, loader, null, null))

    }

    fun serialize(instance : Any, type : ResolvedClass, graph : EntityGraph<*>?) : String {

        val serializer = SerializerRegistry.find(type.raw) as Serializer<Any>

        val node = serializer.serialize(instance, JavaContext(type, graph, null, null))

        return JsonGenerator.generate(node)

    }

}