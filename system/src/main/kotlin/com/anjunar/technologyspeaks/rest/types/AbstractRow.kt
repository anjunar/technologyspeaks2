package com.anjunar.technologyspeaks.rest.types

import com.anjunar.json.mapper.schema.SchemaProvider
import com.anjunar.technologyspeaks.rest.types.LinksContainer
import jakarta.json.bind.annotation.JsonbProperty
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

abstract class AbstractRow<E : Any>(@JsonbProperty val data: E, clazz: KClass<E>) :
    LinksContainer.Interface by LinksContainer.Trait() {

    @JsonbProperty
    val schema = (clazz.companionObjectInstance as SchemaProvider).schema()

}