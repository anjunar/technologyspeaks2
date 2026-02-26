package com.anjunar.technologyspeaks.timeline

import com.anjunar.technologyspeaks.hibernate.search.HibernateSearch
import com.anjunar.technologyspeaks.rest.types.Data
import com.anjunar.technologyspeaks.rest.types.Table
import com.anjunar.technologyspeaks.security.IdentityHolder
import com.anjunar.technologyspeaks.security.LinkBuilder
import com.anjunar.technologyspeaks.shared.commentable.FirstComment
import jakarta.annotation.security.RolesAllowed
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PostCommentsController(val query: HibernateSearch, val identityHolder: IdentityHolder) {

    @GetMapping(value = ["/timeline/posts/post/{post}/comments"], produces = ["application/json"])
    @RolesAllowed("User", "Administrator")
    fun comments(search: PostCommentSearch): Table<CommentRow> {
        val searchContext = query.searchContext(search)

        val entities = query.entities(
            search.index,
            search.limit,
            FirstComment::class,
            CommentRow::class,
            searchContext,
            { query, root, expressions, builder -> query.select(builder.construct(CommentRow::class.java, root)) }
        )

        val count = query.count(FirstComment::class, searchContext)

        for (row in entities) {

            row.data.addLinks(
                LinkBuilder.create(PostLikeController::likeFirstComment)
                    .withRel("like")
                    .withVariable("id", row.data.id)
                    .build(),
                LinkBuilder.create(PostCommentController::update)
                    .withVariable("id", search.post.id)
                    .build(),
                LinkBuilder.create(PostCommentController::delete)
                    .withVariable("id", search.post.id)
                    .build()
            )


            row.data.comments.forEach { comment ->

                if (comment.user == identityHolder.user) {

                    comment.addLinks(
                        LinkBuilder.create(PostLikeController::likeSecondComment)
                            .withRel("like")
                            .withVariable("id", comment.id)
                            .build(),
                        LinkBuilder.create(PostCommentController::update)
                            .withVariable("id", search.post.id)
                            .build(),
                        LinkBuilder.create(PostCommentController::delete)
                            .withVariable("id", search.post.id)
                            .build()
                    )

                }

            }


        }

        return Table(entities, count, Post.schema())
    }

    companion object {
        class CommentRow(data: FirstComment) : Data<FirstComment>(data, FirstComment.schema())
    }

}