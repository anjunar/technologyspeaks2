package com.anjunar.technologyspeaks.rest

import com.anjunar.json.mapper.EntityLoader
import com.anjunar.json.mapper.JsonMapper
import com.anjunar.json.mapper.intermediate.JsonParser
import com.anjunar.json.mapper.intermediate.model.JsonObject
import com.anjunar.kotlin.universe.TypeResolver
import jakarta.persistence.EntityManager
import org.springframework.core.MethodParameter
import org.springframework.http.HttpInputMessage
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice
import java.lang.reflect.Type
import java.util.UUID

@ControllerAdvice
class MapperRequestBodyAdvice(val entityManager: EntityManager) : RequestBodyAdvice {

    override fun supports(
        methodParameter: MethodParameter,
        targetType: Type,
        converterType: Class<out HttpMessageConverter<*>>
    ): Boolean {
        return MapperHttpMessageConverter::class.java.isAssignableFrom(converterType)
    }

    override fun beforeBodyRead(
        inputMessage: HttpInputMessage,
        parameter: MethodParameter,
        targetType: Type,
        converterType: Class<out HttpMessageConverter<*>>
    ): HttpInputMessage {
        return inputMessage
    }

    @Transactional
    override fun afterBodyRead(
        body: Any,
        inputMessage: HttpInputMessage,
        parameter: MethodParameter,
        targetType: Type,
        converterType: Class<out HttpMessageConverter<*>>
    ): Any {

        return when (body) {
            is String -> {
                val jsonNode = JsonParser.parse(body)

                when (jsonNode) {
                    is JsonObject -> {

                        val resolvedClass = TypeResolver.resolve(targetType)

                        val idNode = jsonNode.value["id"]

                        val instance = if (idNode == null) {
                            resolvedClass.raw.getConstructor().newInstance()
                        } else {
                            val primaryKey = UUID.fromString(idNode.value.toString())

                            entityManager.find(resolvedClass.raw, primaryKey)
                        }

                        val annotation = parameter.getMethodAnnotation(EntityGraph::class.java)

                        val entityGraph = if (annotation == null) {
                            null
                        } else {
                            entityManager.getEntityGraph(annotation.value)
                        }

                        val loader = object : EntityLoader {
                            override fun load(id: UUID, clazz: Class<*>): Any? {
                                return entityManager.find(clazz, id)
                            }
                        }

                        JsonMapper.deserialize(jsonNode, instance, resolvedClass, entityGraph, loader)

                    }

                    else -> throw IllegalArgumentException("body must be a json object")
                }

            }
            else -> throw IllegalArgumentException("body must be a string")
        }

    }

    override fun handleEmptyBody(
        body: Any?,
        inputMessage: HttpInputMessage,
        parameter: MethodParameter,
        targetType: Type,
        converterType: Class<out HttpMessageConverter<*>>
    ): Any? {
        return body
    }
}