package com.anjunar.technologyspeaks.shared.commentable

import com.anjunar.technologyspeaks.hibernate.search.AbstractSearch

class CommentSearch(
    sort: MutableList<String> = mutableListOf("created:asc"),
    index: Int = 0,
    limit: Int = 50
) : AbstractSearch(sort, index, limit)

