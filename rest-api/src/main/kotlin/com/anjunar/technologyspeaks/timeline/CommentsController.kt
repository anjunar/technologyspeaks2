package com.anjunar.technologyspeaks.timeline

import com.anjunar.technologyspeaks.hibernate.search.HibernateSearch
import com.anjunar.technologyspeaks.rest.types.Data
import com.anjunar.technologyspeaks.rest.types.Table
import com.anjunar.technologyspeaks.security.LinkBuilder
import com.anjunar.technologyspeaks.shared.commentable.Comment
import com.anjunar.technologyspeaks.shared.commentable.CommentSearch
import com.anjunar.technologyspeaks.timeline.PostsController.Companion.PostRow
import jakarta.annotation.security.RolesAllowed
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RestController

@RestController
class CommentsController(val query: HibernateSearch) {

    @GetMapping(value = ["/timeline/posts/post/{post}/comments"], produces = ["application/json"])
    @RolesAllowed("User", "Administrator")
    @Transactional
    fun comments(search: CommentSearch): Table<CommentRow> {
        val searchContext = query.searchContext(search)

        val entities = query.entities(
            search.index,
            search.limit,
            Comment::class,
            CommentRow::class,
            searchContext,
            { query, root, expressions, builder -> query.select(builder.construct(CommentRow::class.java, root)) }
        )

        val count = query.count(Comment::class, searchContext)

        for (row in entities) {

            row.addLinks(
                LinkBuilder.create(CommentController::comment)
                    .withVariable("id", search.post.id)
                    .build()
            )

        }

        return Table(entities, count, Post.schema())
    }

    companion object {
        class CommentRow(data: Comment) : Data<Comment>(data, Comment.schema())
    }

}