package com.anjunar.technologyspeaks.timeline

import com.anjunar.technologyspeaks.rest.EntityGraph
import com.anjunar.technologyspeaks.rest.types.Data
import com.anjunar.technologyspeaks.security.IdentityHolder
import com.anjunar.technologyspeaks.security.LinkBuilder
import jakarta.annotation.security.RolesAllowed
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RestController
class PostController(val identityHolder: IdentityHolder) {

    @GetMapping(value = ["/timeline/posts/post/{id}"], produces = ["application/json"])
    @RolesAllowed("User", "Administrator")
    @Transactional
    @EntityGraph("Post.full")
    fun read(@PathVariable("id") post: Post): Data<Post> {
        val data = Data(post, Post.schema())

        if (post.user == identityHolder.user) {
            data.addLinks(
                LinkBuilder.create(PostController::read)
                    .withVariable("id", post.id)
                    .build(),
                LinkBuilder.create(PostController::update)
                    .build(),
                LinkBuilder.create(PostController::delete)
                    .withVariable("id", post.id)
                    .build()
            )
        }

        return data
    }

    @PostMapping(value = ["/timeline/posts/post"], produces = ["application/json"], consumes = ["application/json"])
    @RolesAllowed("User", "Administrator")
    @Transactional
    @EntityGraph("Post.full")
    fun save(@RequestBody post: Post): Data<Post> {
        post.user = identityHolder.user
        post.persist()
        val data = Data(post, Post.schema())

        if (post.user == identityHolder.user) {
            data.addLinks(
                LinkBuilder.create(PostController::read)
                    .withVariable("id", post.id)
                    .build(),
                LinkBuilder.create(PostController::update)
                    .build(),
                LinkBuilder.create(PostController::delete)
                    .withVariable("id", post.id)
                    .build()
            )
        }

        return data
    }

    @PutMapping(value = ["/timeline/posts/post"], produces = ["application/json"], consumes = ["application/json"])
    @RolesAllowed("User", "Administrator")
    @Transactional
    @EntityGraph("Post.full")
    fun update(@RequestBody post: Post): Data<Post> {
        val data = Data(post.merge(), Post.schema())

        if (post.user == identityHolder.user) {
            data.addLinks(
                LinkBuilder.create(PostController::read)
                    .withVariable("id", post.id)
                    .build(),
                LinkBuilder.create(PostController::update)
                    .build(),
                LinkBuilder.create(PostController::delete)
                    .withVariable("id", post.id)
                    .build()
            )
        }

        return data
    }

    @DeleteMapping(value = ["/timeline/posts/post/{id}"])
    @RolesAllowed("User", "Administrator")
    @Transactional
    @EntityGraph("Post.full")
    fun delete(@PathVariable("id") post: Post) {
        post.remove()
    }

}