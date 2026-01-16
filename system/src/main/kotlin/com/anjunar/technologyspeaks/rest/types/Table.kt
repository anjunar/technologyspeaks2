package com.anjunar.technologyspeaks.rest.types

import com.anjunar.json.mapper.schema.EntitySchema
import jakarta.json.bind.annotation.JsonbProperty

class Table<C>(@JsonbProperty val rows : List<C>, @JsonbProperty val size : Long, @JsonbProperty val schema : EntitySchema<*>)