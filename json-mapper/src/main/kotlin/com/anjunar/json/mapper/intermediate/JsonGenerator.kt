package com.anjunar.json.mapper.intermediate

import com.anjunar.json.mapper.intermediate.model.JsonArray
import com.anjunar.json.mapper.intermediate.model.JsonBoolean
import com.anjunar.json.mapper.intermediate.model.JsonNode
import com.anjunar.json.mapper.intermediate.model.JsonNull
import com.anjunar.json.mapper.intermediate.model.JsonNumber
import com.anjunar.json.mapper.intermediate.model.JsonObject
import com.anjunar.json.mapper.intermediate.model.JsonString

object JsonGenerator {

    fun generate(jsonNode: JsonNode): String {
        val sb = java.lang.StringBuilder()
        appendJson(jsonNode, sb)
        return sb.toString()
    }

    private fun appendJson(node: JsonNode, sb: StringBuilder) {
        when (node) {

            is JsonObject -> {
                sb.append('{')
                var first = true
                val it = node.value.entries.iterator()
                while (it.hasNext()) {
                    val (k, v) = it.next()
                    if (!first) sb.append(',')
                    first = false
                    sb.append('"')
                    escapeJsonString(k, sb)
                    sb.append("\":")
                    appendJson(v, sb)
                }
                sb.append('}')
            }

            is JsonArray -> {
                sb.append('[')
                var first = true
                val it = node.value.iterator()
                while (it.hasNext()) {
                    if (!first) sb.append(',')
                    first = false
                    appendJson(it.next(), sb)
                }
                sb.append(']')
            }

            is JsonString -> {
                sb.append('"')
                escapeJsonString(node.value, sb)
                sb.append('"')
            }

            is JsonNumber -> {
                sb.append(node.value)
            }

            is JsonBoolean -> {
                sb.append(node.value)
            }

            is JsonNull -> {
                sb.append("null")
            }

            else -> {
                throw IllegalStateException("Unexpected value: $node")
            }
        }
    }

    private fun escapeJsonString(s: String, sb: StringBuilder) {
        var i = 0
        val len = s.length
        while (i < len) {
            when (val c = s[i]) {
                '"'  -> sb.append("\\\"")
                '\\' -> sb.append("\\\\")
                '\b' -> sb.append("\\b")
                '\u000C' -> sb.append("\\f")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                else -> {
                    if (c < ' ') {
                        sb.append(String.format("\\u%04x", c.code))
                    } else {
                        sb.append(c)
                    }
                }
            }
            i += 1
        }
    }
}