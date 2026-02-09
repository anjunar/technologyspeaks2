package com.anjunar.technologyspeaks.timeline

import com.anjunar.technologyspeaks.rest.types.Data
import com.anjunar.technologyspeaks.security.IdentityHolder
import com.anjunar.technologyspeaks.shared.commentable.Comment
import jakarta.annotation.security.RolesAllowed
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class CommentController(val identityHolder: IdentityHolder) {

    @PostMapping(value = ["/timeline/posts/post/{id}/comment"], produces = ["application/json"], consumes = ["application/json"])
    @RolesAllowed("User", "Administrator")
    @Transactional
    fun comment(@PathVariable("id") post: Post, @RequestBody body: Comment): Data<Comment> {

        body.user = identityHolder.user
        body.persist()

        post.comments.add(body)
        post.merge()

        return Data(body, Comment.schema())
    }

}