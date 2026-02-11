package com.anjunar.json.mapper.deserializer

import com.anjunar.json.mapper.provider.EntityProvider
import com.anjunar.json.mapper.JsonContext
import com.anjunar.json.mapper.annotations.UseConverter
import com.anjunar.json.mapper.intermediate.model.JsonNode
import com.anjunar.json.mapper.intermediate.model.JsonNull
import com.anjunar.json.mapper.intermediate.model.JsonObject
import com.anjunar.json.mapper.intermediate.model.JsonString
import com.anjunar.json.mapper.provider.DTO
import com.anjunar.json.mapper.schema.SchemaProvider
import com.anjunar.json.mapper.schema.VisibilityRule
import com.anjunar.kotlin.universe.ResolvedClass
import com.anjunar.kotlin.universe.TypeResolver
import com.anjunar.kotlin.universe.introspector.BeanIntrospector
import com.anjunar.kotlin.universe.introspector.BeanProperty
import jakarta.persistence.EntityGraph
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Subgraph
import java.lang.reflect.InvocationTargetException
import java.util.UUID
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
                            if (context.graph != null && ! isSelectedByGraph(context, property)) {
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
                        if (context.instance == null) {
                            null
                        } else {
                            property.get(context.instance)
                        }
                    } catch (e: InvocationTargetException) {
                        if (e.cause !is UninitializedPropertyAccessException) {
                            throw e.cause!!
                        } else {
                            null
                        }
                    }

                    val node = json.value[property.name]

                    when {
                        propertyType.isSubclassOf(DTO::class) -> {
                            handleEntityProperty(node, property, context, oldValue, propertyType)
                        }
                        propertyType.isSubclassOf(Collection::class) -> {
                            handleCollectionProperty(node, property, context, oldValue)
                        }
                        propertyType.isSubclassOf(Map::class) -> {
                            handleMapProperty(node, property, context, oldValue)
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
            if (context.instance != null) {
                property.set(context.instance, null)
            }
        } else {

            val converterAnnotation = property.findAnnotation(UseConverter::class.java)

            if (converterAnnotation == null) {
                val value = deserializeValue(property.propertyType, property.name, oldValue, context, node)

                property.set(context.instance!!, value)
            } else {
                val value = deserializeValue(TypeResolver.resolve(String::class.java), property.name, oldValue, context, node)

                if (value is String) {

                    val converter = converterAnnotation.value.primaryConstructor?.call()

                    val convertedValue = converter?.toJava(value, property.propertyType)

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
        val instance = context.instance!!
        if (node == null) {
            val collection = property.get(instance) as MutableCollection<Any?>
            collection.clear()
        } else {
            if (oldValue == null) {
                throw IllegalStateException("Collection property must be initialized")
            } else {
                val value = deserializeValue(property.propertyType, property.name, oldValue, context, node)
                val originalCollection = property.get(instance) as MutableCollection<Any>
                originalCollection.clear()
                originalCollection.addAll(value as Collection<Any>)
                synchronizeBidirectionalRelations(instance, property, originalCollection)
            }
        }
    }

    private fun handleMapProperty(
        node: JsonNode?,
        property: BeanProperty,
        context: JsonContext,
        oldValue: Any?
    ) {
        val instance = context.instance!!
        if (node == null) {
            val collection = property.get(instance) as MutableMap<String, Any?>
            collection.clear()
        } else {
            if (oldValue == null) {
                throw IllegalStateException("Collection property must be initialized")
            } else {
                val value = deserializeValue(property.propertyType, property.name, oldValue, context, node)
                val originalCollection = property.get(instance) as MutableMap<String, Any?>
                originalCollection.clear()
                originalCollection.putAll(value as Map<String, Any>)
                synchronizeBidirectionalRelations(instance, property, originalCollection)
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
        val instance = context.instance!!

        when(node) {
            null -> {}
            is JsonNull -> {
                property.set(instance, null)
            }
            else -> {
                if (oldValue == null) {
                    val jsonObject = node as JsonObject
                    val jsonId = jsonObject.value["id"]

                    if (jsonId == null) {
                        val newInstance = propertyType.java.getConstructor().newInstance()
                        val value = deserializeValue(property.propertyType, property.name, newInstance, context, node)
                        property.set(instance, value)
                        synchronizeBidirectionalRelations(instance, property, value)
                    } else {
                        val id = UUID.fromString(jsonId!!.value.toString())
                        val entity = context.loader.load(id, propertyType.java)
                        if (entity == null) {
                            val newInstance = propertyType.java.getConstructor().newInstance()
                            val value = deserializeValue(property.propertyType, property.name, newInstance, context, node)
                            property.set(instance, value)
                            synchronizeBidirectionalRelations(instance, property, value)
                        } else {
                            property.set(instance, entity)
                        }
                    }


                } else {
                    val value = deserializeValue(property.propertyType, property.name, oldValue, context, node)
                    property.set(instance, value)
                    synchronizeBidirectionalRelations(instance, property, value)
                }
            }
        }

    }

    private fun synchronizeBidirectionalRelations(owner: Any, property: BeanProperty, value: Any?) {
        if (value == null) return

        val oneToOne = property.findAnnotation(OneToOne::class.java)
        if (oneToOne != null) {
            synchronizeOneToOne(owner, property, value, oneToOne)
            return
        }

        val oneToMany = property.findAnnotation(OneToMany::class.java)
        if (oneToMany != null) {
            val values = value as? Iterable<*> ?: return
            synchronizeOneToMany(owner, values, oneToMany)
            return
        }

        val manyToOne = property.findAnnotation(ManyToOne::class.java)
        if (manyToOne != null) {
            synchronizeManyToOne(owner, property, value)
            return
        }

        val manyToMany = property.findAnnotation(ManyToMany::class.java)
        if (manyToMany != null) {
            val values = value as? Iterable<*> ?: return
            synchronizeManyToMany(owner, property, values, manyToMany)
        }
    }

    private fun synchronizeOneToOne(owner: Any, property: BeanProperty, value: Any, oneToOne: OneToOne) {
        val mappedBy = oneToOne.mappedBy
        val otherModel = BeanIntrospector.createWithType(value.javaClass)

        if (mappedBy.isNotBlank()) {
            val owningSide = otherModel.findProperty(mappedBy) ?: return
            setRelationIfNeeded(value, owningSide, owner)
            return
        }

        val inverseSide = resolveInverseOneToOne(value, property.name, owner.javaClass) ?: return
        setRelationIfNeeded(value, inverseSide, owner)
    }

    private fun synchronizeOneToMany(owner: Any, values: Iterable<*>, oneToMany: OneToMany) {
        val mappedBy = oneToMany.mappedBy
        if (mappedBy.isBlank()) return

        for (element in values) {
            if (element == null) continue
            val elementModel = BeanIntrospector.createWithType(element.javaClass)
            val owningSide = elementModel.findProperty(mappedBy) ?: continue
            setRelationIfNeeded(element, owningSide, owner)
        }
    }

    private fun synchronizeManyToOne(owner: Any, property: BeanProperty, value: Any) {
        val inverseCollection = resolveInverseOneToMany(value, property.name, owner.javaClass) ?: return
        addToCollectionIfNeeded(value, inverseCollection, owner)
    }

    private fun synchronizeManyToMany(owner: Any, property: BeanProperty, values: Iterable<*>, manyToMany: ManyToMany) {
        val mappedBy = manyToMany.mappedBy

        for (element in values) {
            if (element == null) continue
            val otherModel = BeanIntrospector.createWithType(element.javaClass)

            val otherSideProperty =
                if (mappedBy.isNotBlank()) {
                    otherModel.findProperty(mappedBy)
                } else {
                    resolveInverseManyToMany(element, property.name, owner.javaClass)
                }

            if (otherSideProperty == null) continue
            addToCollectionIfNeeded(element, otherSideProperty, owner)
        }
    }

    private fun setRelationIfNeeded(instance: Any, property: BeanProperty, value: Any) {
        if (!property.isWriteable) return
        if (!property.propertyType.raw.isAssignableFrom(value.javaClass)) return

        val existing = try {
            property.get(instance)
        } catch (_: Exception) {
            null
        }

        if (existing !== value) {
            property.set(instance, value)
        }
    }

    private fun addToCollectionIfNeeded(instance: Any, property: BeanProperty, value: Any) {
        if (!property.propertyType.kotlin.isSubclassOf(Collection::class)) return

        val elementType = property.propertyType.typeArguments.firstOrNull()?.raw
        if (elementType != null && !elementType.isAssignableFrom(value.javaClass)) return

        val collection = try {
            property.get(instance) as? MutableCollection<Any>
        } catch (_: Exception) {
            null
        } ?: return

        if (!collection.contains(value)) {
            collection.add(value)
        }
    }

    private fun resolveInverseOneToOne(target: Any, mappedBy: String, expectedType: Class<*>): BeanProperty? {
        val targetModel = BeanIntrospector.createWithType(target.javaClass)
        return targetModel.properties.firstOrNull { candidate ->
            val annotation = candidate.findAnnotation(OneToOne::class.java) ?: return@firstOrNull false
            if (annotation.mappedBy != mappedBy) return@firstOrNull false
            candidate.propertyType.raw.isAssignableFrom(expectedType)
        }
    }

    private fun resolveInverseOneToMany(target: Any, mappedBy: String, expectedElementType: Class<*>): BeanProperty? {
        val targetModel = BeanIntrospector.createWithType(target.javaClass)
        return targetModel.properties.firstOrNull { candidate ->
            val annotation = candidate.findAnnotation(OneToMany::class.java) ?: return@firstOrNull false
            if (annotation.mappedBy != mappedBy) return@firstOrNull false
            val elementType = candidate.propertyType.typeArguments.firstOrNull()?.raw
            elementType == null || elementType.isAssignableFrom(expectedElementType)
        }
    }

    private fun resolveInverseManyToMany(target: Any, mappedBy: String, expectedElementType: Class<*>): BeanProperty? {
        val targetModel = BeanIntrospector.createWithType(target.javaClass)
        return targetModel.properties.firstOrNull { candidate ->
            val annotation = candidate.findAnnotation(ManyToMany::class.java) ?: return@firstOrNull false
            if (annotation.mappedBy != mappedBy) return@firstOrNull false
            val elementType = candidate.propertyType.typeArguments.firstOrNull()?.raw
            elementType == null || elementType.isAssignableFrom(expectedElementType)
        }
    }

    private fun deserializeValue(
        propertyType: ResolvedClass,
        name : String,
        existingInstance: Any?,
        context: JsonContext,
        node: JsonNode
    ) : Any {
        val deserializer = DeserializerRegistry.findDeserializer(propertyType.raw, node)
        val jsonContext = JsonContext(propertyType, existingInstance, context.graph, context.loader,context, name)
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
