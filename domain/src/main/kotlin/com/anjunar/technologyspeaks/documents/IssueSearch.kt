package com.anjunar.technologyspeaks.documents

import com.anjunar.technologyspeaks.hibernate.search.AbstractSearch
import com.anjunar.technologyspeaks.hibernate.search.Context
import com.anjunar.technologyspeaks.hibernate.search.PredicateProvider
import com.anjunar.technologyspeaks.hibernate.search.annotations.RestPredicate
import org.hibernate.validator.constraints.UUID
import org.springframework.stereotype.Component

class IssueSearch(
    @RestPredicate(DocumentPredicate::class)
    val id : Document,
    sort: MutableList<String> = mutableListOf(),
    index: Int = 0,
    limit: Int = 5
) : AbstractSearch(sort, index, limit) {

    companion object {

        @Component
        class DocumentPredicate : PredicateProvider<Document, Issue> {
            override fun build(context: Context<Document, Issue>) {
                val (value , session, builder, predicates, root, query, selection, name, parameters) = context
                predicates.add(builder.equal(root.get<Document>("document").get<UUID>("id"), value.id))
            }

        }

    }

}