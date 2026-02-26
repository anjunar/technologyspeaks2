package com.anjunar.technologyspeaks.timeline

import com.anjunar.technologyspeaks.hibernate.search.AbstractSearch
import com.anjunar.technologyspeaks.hibernate.search.Context
import com.anjunar.technologyspeaks.hibernate.search.PredicateProvider
import com.anjunar.technologyspeaks.hibernate.search.annotations.RestPredicate
import com.anjunar.technologyspeaks.shared.commentable.FirstComment
import jakarta.json.bind.annotation.JsonbProperty
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

class PostCommentSearch(
    @JsonbProperty
    @PathVariable("post")
    @RestPredicate(PostPredicate::class)
    val post: Post,
    sort: MutableList<String>,
    index: Int = 0,
    limit: Int = 5
) : AbstractSearch(sort, index, limit) {

    companion object {

        @Component
        class PostPredicate : PredicateProvider<Post, FirstComment> {
            override fun build(context: Context<Post, FirstComment>) {

                val (value , session, builder, predicates, root, query, selection, name, parameters) = context

                val postQuery = query.subquery(FirstComment::class.java)
                val postFrom = postQuery.from(Post::class.java)

                val commentsJoin = postFrom.join<Post, FirstComment>("comments")

                postQuery
                    .select(commentsJoin)
                    .where(builder.equal(postFrom.get<UUID>("id"), value.id))

                predicates.add(root.`in`(postQuery))

            }

        }

    }

}