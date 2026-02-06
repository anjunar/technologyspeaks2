package com.anjunar.json.mapper.serializers

import com.anjunar.json.mapper.provider.EntityProvider
import com.anjunar.json.mapper.JavaContext
import com.anjunar.json.mapper.annotations.UseConverter
import com.anjunar.json.mapper.intermediate.model.JsonNode
import com.anjunar.json.mapper.intermediate.model.JsonObject
import com.anjunar.json.mapper.intermediate.model.JsonString
import com.anjunar.json.mapper.schema.SchemaProvider
import com.anjunar.json.mapper.schema.VisibilityRule
import com.anjunar.kotlin.universe.TypeResolver
import com.anjunar.kotlin.universe.introspector.BeanIntrospector
import com.anjunar.kotlin.universe.introspector.BeanProperty
import jakarta.json.bind.annotation.JsonbProperty
import jakarta.persistence.EntityGraph
import jakarta.persistence.Subgraph
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor

@Suppress("UNCHECKED_CAST")
class BeanSerializer : Serializer<Any> {

    override fun serialize(input: Any, context: JavaContext): JsonNode {

        val beanModel = BeanIntrospector.create(context.type)

        val nodes = LinkedHashMap<String, JsonNode>()

        val json = JsonObject(nodes)

        val schemaProvider = context.type.kotlin.companionObjectInstance as SchemaProvider?

        for (property in beanModel.properties) {

            if (property.name != "links" && context.type.kotlin.isSubclassOf(EntityProvider::class)) {
                if (context.graph != null &&  !isSelectedByGraph(context, property)) {
                    continue
                }
            }

            if (schemaProvider != null) {
                val schemaProperties = schemaProvider.schema().properties

                val schemaProperty = schemaProperties[property.name] ?: continue

                val visibilityRule = schemaProperty.rule as VisibilityRule<Any>

                if (!visibilityRule.isVisible(input, property)) {
                    continue
                }
            }

            val value = try {
                property.get(input)
            } catch (e: Exception) {
                null
            }

            when (value) {
                is Boolean -> if (value) convertToJsonNode(property, nodes, value, context)
                is String -> if (value.isNotEmpty()) convertToJsonNode(property, nodes, value, context)
                is Collection<*> -> if (value.isNotEmpty()) convertToJsonNode(property, nodes, value, context)
                else -> if (value != null) convertToJsonNode(property, nodes, value, context)
            }
        }

        return json
    }

    private fun convertToJsonNode(
        property: BeanProperty,
        nodes: LinkedHashMap<String, JsonNode>,
        value: Any,
        context: JavaContext
    ) {
        val jsonbProperty = property.findAnnotation(JsonbProperty::class.java) ?: return

        val name = jsonbProperty.value.ifEmpty {
            property.name
        }

        val propertyType = if (property.propertyType.kotlin.isSubclassOf(Collection::class)) {
            property.propertyType
        } else {
            TypeResolver.resolve(value::class.java)
        }

        val javaContext = JavaContext(
            propertyType,
            context.graph,
            context,
            property.name
        )

        val converterAnnotation = property.findAnnotation(UseConverter::class.java)

        val jsonNode = if (converterAnnotation == null) {
            val serializer = SerializerRegistry.find(property.propertyType.raw) as Serializer<Any>
            serializer.serialize(value, javaContext)
        } else {
            val converter = converterAnnotation.value.primaryConstructor?.call()
            val toJson = converter?.toJson(value, property.propertyType)
            val serializer = SerializerRegistry.find(String::class.java) as Serializer<Any>
            serializer.serialize(toJson!!, javaContext)
        }

        if (jsonNode is JsonObject) {
            if (jsonNode.value.isNotEmpty()) {
                nodes[name] = jsonNode
                nodes[$$"$type"] = JsonString(javaContext.parent!!.type.name)
            }
        } else {
            nodes[name] = jsonNode
            nodes[$$"$type"] = JsonString(javaContext.parent!!.type.name)
        }

    }

    private fun isSelectedByGraph(context: JavaContext, property: BeanProperty): Boolean {
        val currentContainer = resolveContainer(context)

        if (currentContainer != null) {
            val attributeNodes = when (currentContainer) {
                is EntityGraph<*> -> currentContainer.attributeNodes
                is Subgraph<*> -> currentContainer.attributeNodes
                else -> emptyList()
            }
            return attributeNodes.any { it.attributeName == property.name }
        }

        return true
    }

    private fun resolveContainer(context: JavaContext): Any? {
        if (context.parent == null) return context.graph

        if (context.parent.type.raw.let { Collection::class.java.isAssignableFrom(it) || it.isArray }) {
            return resolveContainer(context.parent)
        }

        return findSubgraph(context)
    }

    private fun findSubgraph(context: JavaContext): Subgraph<*>? {
        val parent = context.parent ?: return null

        val parentContainer = resolveContainer(parent)

        val nodes = when (parentContainer) {
            is EntityGraph<*> -> parentContainer.attributeNodes
            is Subgraph<*> -> parentContainer.attributeNodes
            else -> null
        }

        val matchingNode = nodes?.find { it.attributeName == context.name }

        return matchingNode?.subgraphs?.values?.firstOrNull()
    }

}
