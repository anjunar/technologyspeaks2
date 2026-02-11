package com.anjunar.technologyspeaks.hibernate.search

import com.anjunar.technologyspeaks.hibernate.search.annotations.RestSort
import jakarta.json.bind.annotation.JsonbProperty
import org.springframework.web.bind.annotation.RequestParam

open class AbstractSearch(
    @JsonbProperty
    @RestSort
    @RequestParam
    val sort: MutableList<String>,

    @JsonbProperty
    @RequestParam
    val index: Int,

    @JsonbProperty
    @RequestParam
    val limit: Int
)