package com.anjunar.json.mapper.deserializer

import com.anjunar.json.mapper.provider.EntityProvider
import com.anjunar.json.mapper.JsonContext
import com.anjunar.json.mapper.annotations.UseConverter
import com.anjunar.json.mapper.intermediate.model.JsonNode
import com.anjunar.json.mapper.intermediate.model.JsonObject
import com.anjunar.json.mapper.intermediate.model.JsonString
import com.anjunar.json.mapper.schema.SchemaProvider
import com.anjunar.json.mapper.schema.VisibilityRule
import com.anjunar.kotlin.universe.introspector.BeanIntrospector
import com.anjunar.kotlin.universe.introspector.BeanProperty
import jakarta.persistence.EntityGraph
import jakarta.persistence.Subgraph
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor

@Suppress("UNCHECKED_CAST")
class BeanDeserializer : Deserializer<Any> {

    override fun deserialize(json: JsonNode, context: JsonContext): Any {
        return when (json) {
            is JsonObject -> {

                val beanModel = BeanIntrospector.create(context.type)

                val schemaProvider = context.type.kotlin.companionObjectInstance as SchemaProvider?

                for (property in beanModel.properties) {

                    if (property.name == "id") {
                        continue
                    }

                    if (context.instance is EntityProvider && context.instance.version > -1L) {
                        if (property.name != "links" && context.type.kotlin.isSubclassOf(EntityProvider::class)) {
                            if (! isSelectedByGraph(context, property)) {
                                continue
                            }
                        }
                    }

                    if (schemaProvider != null) {
                        val schemaProperties = schemaProvider.schema().properties

                        val schemaProperty = schemaProperties[property.name] ?: continue

                        val visibilityRule = schemaProperty.rule as VisibilityRule<Any>

                        if (! visibilityRule.isWriteable(context.instance!!, property)) {
                            continue
                        }
                    }

                    val propertyType = property.propertyType.kotlin

                    val oldValue = try {
                        property.get(context.instance!!)
                    } catch (e: Exception) {
                        null
                    }

                    val node = json.value[property.name]

                    when {
                        propertyType.isSubclassOf(EntityProvider::class) -> {
                            handleEntityProperty(node, property, context, oldValue, propertyType)
                        }
                        propertyType.isSubclassOf(Collection::class) -> {
                            handleCollectionProperty(node, property, context, oldValue)
                        }

                        else -> {
                            handleNormalProperty(node, property, context, oldValue)
                        }
                    }

                }

                context.instance!!
            }

            else -> throw IllegalArgumentException("json must be a object")
        }
    }

    private fun  handleNormalProperty(
        node: JsonNode?,
        property: BeanProperty,
        context: JsonContext,
        oldValue: Any?
    ) {
        if (node == null) {
            property.set(context.instance!!, null)
        } else {
            val value = deserializeValue(property, oldValue, context, node)

            val converterAnnotation = property.findAnnotation(UseConverter::class.java)

            if (converterAnnotation == null) {
                property.set(context.instance!!, value)
            } else {
                if (value is JsonString) {

                    val converter = converterAnnotation.value.primaryConstructor?.call()

                    val convertedValue = converter?.toJava(value.value, property.propertyType)

                    property.set(context.instance!!, convertedValue)

                } else {
                    throw IllegalArgumentException("Converter only support string type")
                }
            }
        }
    }

    private fun handleCollectionProperty(
        node: JsonNode?,
        property: BeanProperty,
        context: JsonContext,
        oldValue: Any?
    ) {
        if (node == null) {
            val collection = property.get(context.instance!!) as MutableCollection<*>
            collection.clear()
        } else {
            if (oldValue == null) {
                throw IllegalStateException("Collection property must be initialized")
            } else {
                val value = deserializeValue(property, oldValue, context, node)
                val originalCollection = property.get(context.instance!!) as MutableCollection<Any>
                originalCollection.clear()
                originalCollection.addAll(value as Collection<Any>)
            }
        }
    }

    private fun handleEntityProperty(
        node: JsonNode?,
        property: BeanProperty,
        context: JsonContext,
        oldValue: Any?,
        propertyType: KClass<*>
    ) {
        if (node == null) {
            property.set(context.instance!!, null)
        } else {
            if (oldValue == null) {
                val newInstance = propertyType.java.getConstructor().newInstance()

                val value = deserializeValue(property, newInstance, context, node)
                property.set(context.instance!!, value)
            } else {
                val value = deserializeValue(property, oldValue, context, node)
                property.set(context.instance!!, value)
            }
        }
    }

    private fun deserializeValue(
        property: BeanProperty,
        existingInstance: Any?,
        context: JsonContext,
        node: JsonNode
    ) : Any {
        val deserializer = DeserializerRegistry.findDeserializer(property.propertyType.raw)
        val jsonContext = JsonContext(property.propertyType, existingInstance, context.graph, context, property.name)
        return deserializer.deserialize(node, jsonContext)
    }

    private fun isSelectedByGraph(context: JsonContext, property: BeanProperty): Boolean {
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

    private fun resolveContainer(context: JsonContext): Any? {
        if (context.parent == null) return context.graph

        if (context.parent.type.raw.let { Collection::class.java.isAssignableFrom(it) || it.isArray }) {
            return resolveContainer(context.parent)
        }

        return findSubgraph(context)
    }

    private fun findSubgraph(context: JsonContext): Subgraph<*>? {
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