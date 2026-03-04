package com.anjunar.json.mapper

import com.anjunar.json.mapper.deserializer.DeserializerRegistry
import com.anjunar.json.mapper.intermediate.JsonGenerator
import com.anjunar.json.mapper.intermediate.model.JsonNode
import com.anjunar.json.mapper.serializers.Serializer
import com.anjunar.json.mapper.serializers.SerializerRegistry
import com.anjunar.kotlin.universe.ResolvedClass
import jakarta.persistence.EntityGraph
import jakarta.validation.Validator

@Suppress("UNCHECKED_CAST")
object JsonMapper {

    fun deserialize(jsonNode: JsonNode, instance : Any, type: ResolvedClass, graph : EntityGraph<*>?, loader : EntityLoader, validator: Validator) : Any {
        val deserializer = DeserializerRegistry.findDeserializer(type.raw, jsonNode)

        val context = JsonContext(type, instance, graph, loader, validator, null, null)

        return deserializer.deserialize(jsonNode, context)

    }

    fun serialize(instance : Any, type : ResolvedClass, graph : EntityGraph<*>?) : String {

        val serializer = SerializerRegistry.find(type.raw, instance) as Serializer<Any>

        val node = serializer.serialize(instance, JavaContext(type, graph, null, null))

        return JsonGenerator.generate(node)

    }

}