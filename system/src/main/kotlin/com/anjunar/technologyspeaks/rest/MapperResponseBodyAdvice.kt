package com.anjunar.technologyspeaks.rest

import com.anjunar.json.mapper.JsonMapper
import com.anjunar.kotlin.universe.TypeResolver
import jakarta.persistence.EntityManager
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice

@ControllerAdvice
class MapperResponseBodyAdvice(val entityManager: EntityManager) : ResponseBodyAdvice<Any> {

    override fun supports(returnType: MethodParameter, converterType: Class<out HttpMessageConverter<*>>): Boolean {
        return MapperHttpMessageConverter::class.java.isAssignableFrom(converterType)
    }

    @Transactional
    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse
    ): Any? {

        val resolvedClass = TypeResolver.resolve(returnType.genericParameterType)

        val annotation = returnType.getMethodAnnotation(EntityGraph::class.java)

        val entityGraph = if (annotation == null) {
            null
        } else {
            entityManager.getEntityGraph(annotation.value)
        }

        val serialize = JsonMapper.serialize(body!!, resolvedClass, entityGraph)

        return serialize

    }
}