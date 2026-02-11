package com.anjunar.technologyspeaks.timeline

import com.anjunar.technologyspeaks.rest.types.Data
import com.anjunar.technologyspeaks.security.IdentityHolder
import com.anjunar.technologyspeaks.shared.commentable.FirstComment
import jakarta.annotation.security.RolesAllowed
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class CommentController(val identityHolder: IdentityHolder) {

    @PostMapping(value = ["/timeline/posts/post/{id}/comment"], produces = ["application/json"], consumes = ["application/json"])
    @RolesAllowed("User", "Administrator")
    fun save(@PathVariable("id") post: Post, @RequestBody body: FirstComment): Data<FirstComment> {

        body.user = identityHolder.user
        body.comments.filter {
            try {
                it.user == null
            } catch (e: Exception) {
                true
            }
        }.forEach { it.user = identityHolder.user}
        body.persist()

        post.comments.add(body)

        return Data(body, FirstComment.schema())
    }

    @PutMapping(value = ["/timeline/posts/post/{id}/comment"], produces = ["application/json"], consumes = ["application/json"])
    @RolesAllowed("User", "Administrator")
    fun update(@PathVariable("id") post: Post, @RequestBody body: FirstComment): Data<FirstComment> {

        body.user = identityHolder.user
        body.comments.filter {
            try {
                it.user == null
            } catch (e: Exception) {
                true
            }
        }.forEach { it.user = identityHolder.user}

        post.comments.add(body)

        return Data(body, FirstComment.schema())
    }


}