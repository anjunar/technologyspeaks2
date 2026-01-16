package com.anjunar.json.mapper.intermediate

import com.anjunar.json.mapper.intermediate.model.JsonArray
import com.anjunar.json.mapper.intermediate.model.JsonBoolean
import com.anjunar.json.mapper.intermediate.model.JsonNode
import com.anjunar.json.mapper.intermediate.model.JsonNull
import com.anjunar.json.mapper.intermediate.model.JsonNumber
import com.anjunar.json.mapper.intermediate.model.JsonObject
import com.anjunar.json.mapper.intermediate.model.JsonString
import java.util.LinkedHashMap

object JsonParser {

    fun parse(json: String): JsonNode {
        val parser = FastParser(json)
        val node = parser.parseValue()
        parser.skipWhitespace()
        if (!parser.eof()) {
            throw IllegalStateException("Unexpected trailing data at pos ${parser.pos}")
        }
        return node
    }

    private class FastParser(input: String) {
        private val chars: CharArray = input.toCharArray()
        private val len: Int = chars.size
        var pos: Int = 0

        fun eof(): Boolean = pos >= len

        private fun peek(): Char = if (pos < len) chars[pos] else 0.toChar()

        private fun nextChar(): Char {
            val c = chars[pos]
            pos += 1
            return c
        }

        fun skipWhitespace() {
            while (pos < len) {
                val c = chars[pos]
                if (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
                    pos += 1
                } else {
                    return
                }
            }
        }

        fun parseValue(): JsonNode {
            skipWhitespace()
            if (eof()) throw IllegalStateException("Unexpected end of input")
            return when (val c = peek()) {
                '{' -> parseObject()
                '[' -> parseArray()
                '"' -> JsonString(parseString())
                't' -> {
                    expectLiteral("true")
                    JsonBoolean(true)
                }

                'f' -> {
                    expectLiteral("false")
                    JsonBoolean(false)
                }

                'n' -> {
                    expectLiteral("null")
                    JsonNull()
                }

                '-', in '0'..'9' -> parseNumber()
                else -> throw IllegalStateException("Unexpected char '$c' at pos $pos")
            } as JsonNode
        }

        private fun expectLiteral(lit: String) {
            val start = pos
            val end = start + lit.length
            if (end > len) throw IllegalStateException("Unexpected end (expect '$lit')")
            var i = 0
            while (i < lit.length) {
                if (chars[pos + i] != lit[i]) {
                    throw IllegalStateException("Unexpected literal at pos $pos, expected $lit")
                }
                i += 1
            }
            pos = end
        }

        private fun parseObject(): JsonObject {
            if (nextChar() != '{') throw IllegalStateException("Expected '{'")
            val map = LinkedHashMap<String, JsonNode>()
            skipWhitespace()
            if (!eof() && peek() == '}') {
                pos += 1
                return JsonObject(map)
            }
            var continueLoop = true
            while (continueLoop) {
                skipWhitespace()
                if (eof() || peek() != '"') throw IllegalStateException("Expected string key at pos $pos")
                val key = parseString()
                skipWhitespace()
                if (eof() || nextChar() != ':') throw IllegalStateException("Expected ':' after key at pos $pos")
                val value = parseValue()
                map[key] = value
                skipWhitespace()
                if (eof()) throw IllegalStateException("Unterminated object")
                when (val ch = peek()) {
                    ',' -> pos += 1
                    '}' -> {
                        pos += 1
                        continueLoop = false
                    }
                    else -> throw IllegalStateException("Expected ',' or '}' in object at pos $pos but found '$ch'")
                }
            }
            return JsonObject(map)
        }

        private fun parseArray(): JsonArray {
            if (nextChar() != '[') throw IllegalStateException("Expected '['")
            val arr = ArrayList<JsonNode>()
            skipWhitespace()
            if (!eof() && peek() == ']') {
                pos += 1
                return JsonArray(arr)
            }
            var continueLoop = true
            while (continueLoop) {
                val v = parseValue()
                arr.add(v)
                skipWhitespace()
                if (eof()) throw IllegalStateException("Unterminated array")
                when (val ch = peek()) {
                    ',' -> pos += 1
                    ']' -> {
                        pos += 1
                        continueLoop = false
                    }
                    else -> throw IllegalStateException("Expected ',' or ']' in array at pos $pos but found '$ch'")
                }
            }
            return JsonArray(arr)
        }

        private fun parseString(): String {
            if (nextChar() != '"') throw IllegalStateException("Expected '\"' at beginning of string")
            val start = pos
            var i = pos
            var hasEscape = false
            while (i < len) {
                val c = chars[i]
                if (c == '"') {
                    val end = i
                    pos = i + 1
                    return if (!hasEscape) {
                        String(chars, start, end - start)
                    } else {
                        unescapeFromChars(start, end)
                    }
                } else if (c == '\\') {
                    hasEscape = true
                    i += 2
                } else {
                    i += 1
                }
            }
            throw IllegalStateException("Unterminated string")
        }

        private fun unescapeFromChars(start: Int, end: Int): String {
            val sb = StringBuilder(end - start)
            var i = start
            while (i < end) {
                val c = chars[i]
                if (c == '\\') {
                    i += 1
                    if (i >= end) throw IllegalStateException("Invalid escape at end of string")
                    when (val esc = chars[i]) {
                        '"' -> {
                            sb.append('"')
                            i += 1
                        }
                        '\\' -> {
                            sb.append('\\')
                            i += 1
                        }
                        '/' -> {
                            sb.append('/')
                            i += 1
                        }
                        'b' -> {
                            sb.append('\b')
                            i += 1
                        }
                        'f' -> {
                            sb.append('\u000C')
                            i += 1
                        }
                        'n' -> {
                            sb.append('\n')
                            i += 1
                        }
                        'r' -> {
                            sb.append('\r')
                            i += 1
                        }
                        't' -> {
                            sb.append('\t')
                            i += 1
                        }
                        'u' -> {
                            if (i + 4 >= end) throw IllegalStateException("Invalid unicode escape")
                            var code = 0
                            var j = 1
                            while (j <= 4) {
                                val ch = chars[i + j]
                                code = (code shl 4) + hexValue(ch)
                                j += 1
                            }
                            sb.append(code.toChar())
                            i += 5
                        }
                        else -> throw IllegalStateException("Invalid escape '\\$esc' at pos $i")
                    }
                } else {
                    sb.append(c)
                    i += 1
                }
            }
            return sb.toString()
        }

        private fun hexValue(c: Char): Int {
            return when (c) {
                in '0'..'9' -> c.code - '0'.code
                in 'A'..'F' -> 10 + (c.code - 'A'.code)
                in 'a'..'f' -> 10 + (c.code - 'a'.code)
                else -> throw IllegalStateException("Invalid hex char '$c' in unicode escape")
            }
        }

        private fun parseNumber(): JsonNumber {
            val start = pos
            if (peek() == '-') pos += 1

            if (eof()) throw IllegalStateException("Unexpected end in number")
            if (peek() == '0') {
                pos += 1
            } else if (peek() in '1'..'9') {
                while (!eof() && chars[pos] in '0'..'9') {
                    pos += 1
                }
            } else {
                throw IllegalStateException("Invalid number at pos $pos")
            }

            if (!eof() && chars[pos] == '.') {
                pos += 1
                if (eof() || chars[pos] !in '0'..'9') {
                    throw IllegalStateException("Invalid fractional part in number")
                }
                while (!eof() && chars[pos] in '0'..'9') {
                    pos += 1
                }
            }

            if (!eof() && (chars[pos] == 'e' || chars[pos] == 'E')) {
                pos += 1
                if (!eof() && (chars[pos] == '+' || chars[pos] == '-')) {
                    pos += 1
                }
                if (eof() || chars[pos] !in '0'..'9') {
                    throw IllegalStateException("Invalid exponent in number")
                }
                while (!eof() && chars[pos] in '0'..'9') {
                    pos += 1
                }
            }

            return JsonNumber(String(chars, start, pos - start))
        }
    }
}