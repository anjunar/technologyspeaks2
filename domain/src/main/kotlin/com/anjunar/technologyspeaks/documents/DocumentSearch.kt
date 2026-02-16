package com.anjunar.technologyspeaks.documents

import com.anjunar.technologyspeaks.hibernate.search.AbstractSearch
import jakarta.json.bind.annotation.JsonbProperty
import org.springframework.web.bind.annotation.RequestParam

class DocumentSearch(
    @JsonbProperty
    @RequestParam
    val name : String? = null,
    sort: MutableList<String> = mutableListOf(),
    index: Int = 0,
    limit: Int = 5
) : AbstractSearch(sort, index, limit)