package com.anjunar.technologyspeaks.timeline

import com.anjunar.technologyspeaks.hibernate.search.AbstractSearch

class PostSearch(
    sort: MutableList<String> = mutableListOf(),
    index: Int = 0,
    limit: Int = 5
) : AbstractSearch(sort, index, limit)