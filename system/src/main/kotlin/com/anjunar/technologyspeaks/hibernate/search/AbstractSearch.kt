package com.anjunar.technologyspeaks.hibernate.search

import com.anjunar.technologyspeaks.hibernate.search.annotations.RestSort
import jakarta.json.bind.annotation.JsonbProperty
import org.springframework.web.bind.annotation.RequestParam

open class AbstractSearch(
    @JsonbProperty
    @RestSort
    @RequestParam
    val sort: MutableList<String> = mutableListOf(),

    @JsonbProperty
    @RequestParam
    val index: Int = 0,

    @JsonbProperty
    @RequestParam
    val limit: Int = 5

)