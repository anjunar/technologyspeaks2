package com.anjunar.technologyspeaks.rest

import com.anjunar.json.mapper.JsonMapper
import com.anjunar.json.mapper.intermediate.JsonParser
import com.anjunar.json.mapper.intermediate.model.JsonObject
import com.anjunar.kotlin.universe.TypeResolver
import jakarta.persistence.EntityManager
import org.springframework.core.MethodParameter
import org.springframework.http.HttpInputMessage
import org.springframework.http.converter.HttpMessageConverter
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

                        val idNode = jsonNode.value["id"] ?: throw IllegalArgumentException("missing property id")

                        val entityGraph = entityManager.getEntityGraph("User.full")

                        val instance = entityManager.find(resolvedClass.raw, UUID.fromString(idNode.value.toString()))

                        JsonMapper.deserialize(jsonNode, instance, resolvedClass, entityGraph)

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