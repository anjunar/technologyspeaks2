package com.anjunar.technologyspeaks.core

import com.anjunar.json.mapper.schema.EntitySchema
import com.anjunar.json.mapper.schema.Link
import java.util.UUID
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

@Suppress("UNCHECKED_CAST")
abstract class AbstractEntitySchema<E : AbstractEntity> : EntitySchema<E>() {

    val id = property(AbstractEntity::id as KProperty1<E, UUID>)
    val links = property(AbstractEntity::links as KProperty1<E, List<Link>>)

}