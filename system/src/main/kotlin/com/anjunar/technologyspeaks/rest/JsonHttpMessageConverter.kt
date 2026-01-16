package com.anjunar.technologyspeaks.rest

import com.anjunar.json.mapper.intermediate.JsonGenerator
import com.anjunar.json.mapper.intermediate.JsonParser
import com.anjunar.json.mapper.intermediate.model.JsonNode
import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpOutputMessage
import org.springframework.http.MediaType
import org.springframework.http.converter.AbstractHttpMessageConverter
import kotlin.reflect.full.isSubclassOf

class JsonHttpMessageConverter : AbstractHttpMessageConverter<Any>(MediaType.APPLICATION_JSON) {

    override fun supports(clazz: Class<*>): Boolean {
        return clazz.kotlin.isSubclassOf(JsonNode::class)
    }

    override fun readInternal(clazz: Class<*>, inputMessage: HttpInputMessage): Any {
        val readAllBytes = inputMessage.body.readAllBytes()
        val text = String(readAllBytes, Charsets.UTF_8)
        return JsonParser.parse(text)
    }

    override fun writeInternal(o: Any, outputMessage: HttpOutputMessage) {
        val text = JsonGenerator.generate(o as JsonNode)
        val bytes = text.toByteArray(Charsets.UTF_8)
        outputMessage.headers.contentType = MediaType.APPLICATION_JSON
        outputMessage.body.use { it.write(bytes) }
    }

}