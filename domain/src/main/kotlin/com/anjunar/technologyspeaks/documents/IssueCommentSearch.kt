package com.anjunar.technologyspeaks.documents

import com.anjunar.technologyspeaks.hibernate.search.AbstractSearch
import com.anjunar.technologyspeaks.hibernate.search.Context
import com.anjunar.technologyspeaks.hibernate.search.PredicateProvider
import com.anjunar.technologyspeaks.hibernate.search.annotations.RestPredicate
import com.anjunar.technologyspeaks.shared.commentable.FirstComment
import com.anjunar.technologyspeaks.timeline.Post
import jakarta.json.bind.annotation.JsonbProperty
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

class IssueCommentSearch(
    @JsonbProperty
    @PathVariable("issue")
    @RestPredicate(IssuePredicate::class)
    val issue: Issue,
    sort: MutableList<String>,
    index: Int = 0,
    limit: Int = 5
) : AbstractSearch(sort, index, limit) {

    companion object {

        @Component
        class IssuePredicate : PredicateProvider<Issue, FirstComment> {
            override fun build(context: Context<Issue, FirstComment>) {

                val (value , session, builder, predicates, root, query, selection, name, parameters) = context

                val postQuery = query.subquery(FirstComment::class.java)
                val postFrom = postQuery.from(Issue::class.java)

                val commentsJoin = postFrom.join<Issue, FirstComment>("comments")

                postQuery
                    .select(commentsJoin)
                    .where(builder.equal(postFrom.get<UUID>("id"), value.id))

                predicates.add(root.`in`(postQuery))

            }

        }

    }

}