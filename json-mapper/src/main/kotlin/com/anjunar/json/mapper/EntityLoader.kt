package com.anjunar.json.mapper

import java.util.UUID

interface EntityLoader {

    fun load(id : UUID, clazz : Class<*>) : Any?

}