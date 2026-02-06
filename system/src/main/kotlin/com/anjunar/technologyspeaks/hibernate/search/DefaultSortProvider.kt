package com.anjunar.technologyspeaks.hibernate.search

import jakarta.persistence.criteria.Order
import org.hibernate.query.SortDirection
import org.springframework.stereotype.Component

@Component
class DefaultSortProvider<E> : SortProvider<MutableList<String>, E> {
    override fun sort(context: Context<MutableList<String>, E>): MutableList<Order> {
        val sortValues = readSortValues(context).orEmpty()
        if (sortValues.isEmpty()) {
            return mutableListOf()
        }

        val orders = ArrayList<Order>(sortValues.size)
        for (value in sortValues) {
            val spec = parseSpec(value) ?: continue
            orders.add(
                when (spec.direction) {
                    SortDirection.DESCENDING -> context.builder.desc(context.root.get<Any>(spec.path))
                    SortDirection.ASCENDING -> context.builder.asc(context.root.get<Any>(spec.path))
                }
            )
        }

        return orders
    }

    private fun readSortValues(context: Context<MutableList<String>, E>): List<String>? {
        @Suppress("UNCHECKED_CAST")
        val rawValue = (context as Context<Any?, E>).value

        return when (rawValue) {
            is List<*> -> rawValue.filterIsInstance<String>()
            is AbstractSearch -> rawValue.sort
            else -> null
        }
    }

    private fun parseSpec(raw: String): SortSpec? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) {
            return null
        }

        var working = trimmed
        var directionFromPrefix: SortDirection? = null

        if (working.startsWith("-")) {
            directionFromPrefix = SortDirection.DESCENDING
            working = working.removePrefix("-").trimStart()
        } else if (working.startsWith("+")) {
            directionFromPrefix = SortDirection.ASCENDING
            working = working.removePrefix("+").trimStart()
        }

        val delimiterIndex = working.lastIndexOf(':')
        val path = if (delimiterIndex >= 0) working.substring(0, delimiterIndex).trim() else working
        if (path.isEmpty()) {
            return null
        }

        val directionText = if (delimiterIndex >= 0) working.substring(delimiterIndex + 1).trim() else ""
        val direction = directionFromPrefix ?: interpretDirection(directionText) ?: SortDirection.ASCENDING

        return SortSpec(path, direction)
    }

    private fun interpretDirection(raw: String): SortDirection? {
        if (raw.isBlank()) {
            return null
        }

        return try {
            SortDirection.interpret(raw.trim())
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private data class SortSpec(val path: String, val direction: SortDirection)
}
