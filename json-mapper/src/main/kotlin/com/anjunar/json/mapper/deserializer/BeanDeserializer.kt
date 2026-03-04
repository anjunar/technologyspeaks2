package com.anjunar.json.mapper.deserializer

import com.anjunar.json.mapper.provider.EntityProvider
import com.anjunar.json.mapper.JsonContext
import com.anjunar.json.mapper.annotations.UseConverter
import com.anjunar.json.mapper.intermediate.model.JsonNode
import com.anjunar.json.mapper.intermediate.model.JsonNull
import com.anjunar.json.mapper.intermediate.model.JsonObject
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

    private fun handleNormalProperty(
        node: JsonNode?,
        property: BeanProperty,
        context: JsonContext,
        oldValue: Any?
    ) {
        if (node == null) {
            return
        }

        if (node is JsonNull) {
            context.instance?.let { instance ->
                context.checkForViolations(instance.javaClass, property.name, null) {
                    property.set(instance, null)
                }
            }
            return
        }

        val instance = context.instance!!

        val useConverter = property.findAnnotation(UseConverter::class.java)
        if (useConverter == null) {
            val value = deserializeValue(property.propertyType, property.name, oldValue, context, node)
            context.checkForViolations(instance.javaClass, property.name, value) {
                property.set(instance, value)
            }
            return
        }

        val rawValue = deserializeValue(TypeResolver.resolve(String::class.java), property.name, oldValue, context, node)
        val stringValue = rawValue as? String ?: throw IllegalArgumentException("Converter only support string type")

        val converter = useConverter.value.primaryConstructor?.call()
        val convertedValue = converter?.toJava(stringValue, property.propertyType)

        context.checkForViolations(instance.javaClass, property.name, convertedValue) {
            property.set(instance, convertedValue)
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
            return
        }

        val existingCollection = oldValue ?: throw IllegalStateException("Collection property must be initialized")
        val deserialized = deserializeValue(property.propertyType, property.name, existingCollection, context, node) as Collection<Any>

        val targetCollection = property.get(instance) as MutableCollection<Any>

        context.checkForViolations(instance.javaClass, property.name, targetCollection) {
            targetCollection.clear()
            targetCollection.addAll(deserialized)

            synchronizeBidirectionalRelations(instance, property, targetCollection)
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
            return
        }

        val existingMap = oldValue ?: throw IllegalStateException("Collection property must be initialized")
        val deserialized = deserializeValue(property.propertyType, property.name, existingMap, context, node) as Map<String, Any>

        val targetMap = property.get(instance) as MutableMap<String, Any?>

        context.checkForViolations(instance.javaClass, property.name, targetMap) {
            targetMap.clear()
            targetMap.putAll(deserialized)

            synchronizeBidirectionalRelations(instance, property, targetMap)
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

        if (node == null) return

        if (node is JsonNull) {

            context.checkForViolations(instance.javaClass, property.name, null) {
                property.set(instance, null)
            }

            return
        }

        if (oldValue != null) {
            val value = deserializeValue(property.propertyType, property.name, oldValue, context, node)
            context.checkForViolations(instance.javaClass, property.name, value) {
                setPropertyAndSynchronize(instance, property, value)
            }
            return
        }

        val jsonObject = node as JsonObject
        val jsonId = jsonObject.value["id"]

        if (jsonId != null) {
            val id = UUID.fromString(jsonId.value.toString())
            val entity = context.loader.load(id, propertyType.java)
            if (entity != null) {
                context.checkForViolations(instance.javaClass, property.name, entity) {
                    property.set(instance, entity)
                }
                return
            }
        }

        val value = deserializeNewEntity(propertyType, property, context, node)

        context.checkForViolations(instance.javaClass, property.name, value) {
            setPropertyAndSynchronize(instance, property, value)
        }
    }

    private fun deserializeNewEntity(
        propertyType: KClass<*>,
        property: BeanProperty,
        context: JsonContext,
        node: JsonNode
    ): Any {
        val newInstance = propertyType.java.getConstructor().newInstance()
        return deserializeValue(property.propertyType, property.name, newInstance, context, node)
    }

    private fun setPropertyAndSynchronize(owner: Any, property: BeanProperty, value: Any?) {
        property.set(owner, value)
        synchronizeBidirectionalRelations(owner, property, value)
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
        val jsonContext = JsonContext(propertyType, existingInstance, context.graph, context.loader, context.validator,context, name)
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

        if (!context.parent.type.kotlin.isSubclassOf(EntityProvider::class)) {
            return context.graph
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
