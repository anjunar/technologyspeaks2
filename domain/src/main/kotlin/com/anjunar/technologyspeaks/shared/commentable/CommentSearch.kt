package com.anjunar.technologyspeaks.shared.commentable

import com.anjunar.technologyspeaks.hibernate.search.AbstractSearch
import com.anjunar.technologyspeaks.hibernate.search.Context
import com.anjunar.technologyspeaks.hibernate.search.PredicateProvider
import com.anjunar.technologyspeaks.hibernate.search.annotations.RestPredicate
import com.anjunar.technologyspeaks.timeline.Post
import jakarta.json.bind.annotation.JsonbProperty
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID

class CommentSearch : AbstractSearch() {

    @JsonbProperty
    @RestPredicate(PostPredicate::class)
    lateinit var post: Post

    companion object {

        @Component
        class PostPredicate : PredicateProvider<Post, Comment> {
            override fun build(context: Context<Post, Comment>) {

                val (value , session, builder, predicates, root, query, selection, name, parameters) = context

                val postQuery = query.subquery(Comment::class.java)
                val postFrom = postQuery.from(Post::class.java)

                val commentsJoin = postFrom.join<Post, Comment>("comments")

                postQuery
                    .select(commentsJoin)
                    .where(builder.equal(postFrom.get<UUID>("id"), value.id))

                predicates.add(root.`in`(postQuery))

            }

        }

    }

}

