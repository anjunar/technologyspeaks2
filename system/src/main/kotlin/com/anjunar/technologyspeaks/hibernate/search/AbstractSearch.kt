package com.anjunar.technologyspeaks.hibernate.search

import com.anjunar.technologyspeaks.hibernate.search.annotations.RestSort
import jakarta.json.bind.annotation.JsonbProperty

open class AbstractSearch {

    @JsonbProperty
    @RestSort
    var sort : MutableList<String> = mutableListOf()

    @JsonbProperty
    var index : Int = 0

    @JsonbProperty
    var limit : Int = 5

}