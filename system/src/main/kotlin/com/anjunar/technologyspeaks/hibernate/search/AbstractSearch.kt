package com.anjunar.technologyspeaks.hibernate.search

import com.anjunar.technologyspeaks.hibernate.search.annotations.RestSort
import jakarta.json.bind.annotation.JsonbProperty

abstract class AbstractSearch(@JsonbProperty @RestSort val sort : MutableList<String> = mutableListOf(),
                              @JsonbProperty val index : Int = 0,
                              @JsonbProperty val limit : Int = 5)