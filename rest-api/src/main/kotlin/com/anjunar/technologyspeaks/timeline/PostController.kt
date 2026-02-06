package com.anjunar.technologyspeaks.timeline

import com.anjunar.technologyspeaks.rest.EntityGraph
import com.anjunar.technologyspeaks.rest.types.Data
import com.anjunar.technologyspeaks.security.IdentityHolder
import jakarta.annotation.security.RolesAllowed
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class PostController(val identityHolder: IdentityHolder) {

    @GetMapping(value = ["/timeline/posts/post/{id}"], produces = ["application/json"])
    @RolesAllowed("User", "Administrator")
    @Transactional
    @EntityGraph("Post.full")
    fun read(@PathVariable("id") post: Post) : Data<Post> {
        return Data(post, Post.schema())
    }

    @PostMapping(value = ["/timeline/posts/post"], produces = ["application/json"], consumes = ["application/json"])
    @RolesAllowed("User", "Administrator")
    @Transactional
    @EntityGraph("Post.full")
    fun save(@RequestBody post: Post) : Data<Post> {
        post.user = identityHolder.user
        post.persist()
        return Data(post, Post.schema())
    }

    @PutMapping(value = ["/timeline/posts/post"], produces = ["application/json"], consumes = ["application/json"])
    @RolesAllowed("User", "Administrator")
    @Transactional
    @EntityGraph("Post.full")
    fun update(@RequestBody post: Post) : Data<Post> {
        return Data(post.merge(), Post.schema())
    }

    @DeleteMapping(value = ["/timeline/posts/post/{id}"])
    @RolesAllowed("User", "Administrator")
    @Transactional
    @EntityGraph("Post.full")
    fun delete(@PathVariable("id") post: Post) {
        post.remove()
    }

}