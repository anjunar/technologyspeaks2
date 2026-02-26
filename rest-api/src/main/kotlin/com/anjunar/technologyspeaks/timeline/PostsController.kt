package com.anjunar.technologyspeaks.timeline

import com.anjunar.technologyspeaks.hibernate.search.HibernateSearch
import com.anjunar.technologyspeaks.rest.EntityGraph
import com.anjunar.technologyspeaks.rest.types.Data
import com.anjunar.technologyspeaks.rest.types.Table
import com.anjunar.technologyspeaks.security.IdentityHolder
import com.anjunar.technologyspeaks.security.LinkBuilder
import jakarta.annotation.security.RolesAllowed
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PostsController(val query: HibernateSearch, val identityHolder: IdentityHolder) {

    @GetMapping(value = ["/timeline/posts"], produces = ["application/json"])
    @RolesAllowed("User", "Administrator")
    @EntityGraph("Post.full")
    fun list(search : PostSearch): Table<PostRow> {

        val searchContext = query.searchContext(search)

        val entities = query.entities(
            search.index,
            search.limit,
            Post::class,
            PostRow::class,
            searchContext,
            { query, root, expressions, builder -> query.select(builder.construct(PostRow::class.java, root)) }
        )

        val count = query.count(Post::class, searchContext)

        for (post in entities) {
            post.data.addLinks(
                LinkBuilder.create(PostLikeController::likePost)
                    .withRel("like")
                    .withVariable("id", post.data.id)
                    .build()
            )

            if (identityHolder.user == post.data.user) {
                post.data.addLinks(
                    LinkBuilder.create(PostController::read)
                        .withVariable("id", post.data.id)
                        .build()
                )
            }

        }

        return Table(entities, count, Post.schema())

    }

    companion object {
        class PostRow(data: Post) : Data<Post>(data, Post.schema())
    }


}
