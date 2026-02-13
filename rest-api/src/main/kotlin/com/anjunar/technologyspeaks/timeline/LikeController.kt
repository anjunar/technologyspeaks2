package com.anjunar.technologyspeaks.timeline

import com.anjunar.technologyspeaks.shared.commentable.FirstComment
import com.anjunar.technologyspeaks.shared.commentable.SecondComment
import com.anjunar.technologyspeaks.shared.likeable.Like
import com.anjunar.technologyspeaks.shared.likeable.LikeService
import jakarta.annotation.security.RolesAllowed
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class LikeController(val likeService: LikeService) {

    @PostMapping(value = ["/timeline/posts/post/{id}/like"], produces = ["application/json"])
    @RolesAllowed("User", "Administrator")
    fun likePost(@PathVariable("id") post: Post): Set<Like> {
        return likeService.toggle(post)
    }

    @PostMapping(value = ["/timeline/posts/post/comment/{id}/like"], produces = ["application/json"])
    @RolesAllowed("User", "Administrator")
    fun likeFirstComment(@PathVariable("id") comment: FirstComment): Set<Like> {
        return likeService.toggle(comment)
    }

    @PostMapping(value = ["/timeline/posts/post/comment/comment/{id}/like"], produces = ["application/json"])
    @RolesAllowed("User", "Administrator")
    fun likeSecondComment(@PathVariable("id") comment: SecondComment): Set<Like> {
        return likeService.toggle(comment)
    }


}