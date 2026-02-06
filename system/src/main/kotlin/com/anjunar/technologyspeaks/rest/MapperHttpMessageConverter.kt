package com.anjunar.technologyspeaks.rest

import com.anjunar.json.mapper.intermediate.model.JsonNode
import com.anjunar.technologyspeaks.rest.types.DTO
import com.anjunar.technologyspeaks.rest.types.Table
import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpOutputMessage
import org.springframework.http.MediaType
import org.springframework.http.converter.AbstractHttpMessageConverter
import kotlin.reflect.full.isSubclassOf

class MapperHttpMessageConverter : AbstractHttpMessageConverter<Any>(MediaType.APPLICATION_JSON) {

    override fun supports(clazz: Class<*>): Boolean {
        return clazz.kotlin.isSubclassOf(DTO::class)
    }

    override fun readInternal(clazz: Class<*>, inputMessage: HttpInputMessage): Any {
        return String(inputMessage.body.readAllBytes(), Charsets.UTF_8)
    }

    override fun writeInternal(o: Any, outputMessage: HttpOutputMessage) {
        val text = when (o) {
            is String -> o
            else -> o.toString()
        }

        val bytes = text.toByteArray(Charsets.UTF_8)
        outputMessage.headers.contentType = MediaType.APPLICATION_JSON
        outputMessage.body.use { it.write(bytes) }
    }
}