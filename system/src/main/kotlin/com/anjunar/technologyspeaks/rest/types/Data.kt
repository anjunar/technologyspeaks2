package com.anjunar.technologyspeaks.rest.types

import com.anjunar.json.mapper.schema.EntitySchema
import jakarta.json.bind.annotation.JsonbProperty

open class Data<E : Any>(@JsonbProperty val data: E, @JsonbProperty val schema: EntitySchema<*>) :
    LinksContainer.Interface by LinksContainer.Trait(), DTO