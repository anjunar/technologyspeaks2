package com.anjunar.technologyspeaks.documents

import com.anjunar.technologyspeaks.shared.commentable.FirstComment
import com.anjunar.technologyspeaks.shared.commentable.SecondComment
import com.anjunar.technologyspeaks.shared.likeable.Like
import com.anjunar.technologyspeaks.shared.likeable.LikeService
import jakarta.annotation.security.RolesAllowed
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class IssueLikeController(val likeService: LikeService) {

    @PostMapping(value = ["/document/documents/document/issues/issue/{id}/like"], produces = ["application/json"])
    @RolesAllowed("User", "Administrator")
    fun likeIssue(@PathVariable("id") post: Issue): Set<Like> {
        return likeService.toggle(post)
    }

    @PostMapping(value = ["/document/documents/document/issues/issue/comment/{id}/like"], produces = ["application/json"])
    @RolesAllowed("User", "Administrator")
    fun likeFirstComment(@PathVariable("id") comment: FirstComment): Set<Like> {
        return likeService.toggle(comment)
    }

    @PostMapping(value = ["/document/documents/document/issues/issue/comment/comment/{id}/like"], produces = ["application/json"])
    @RolesAllowed("User", "Administrator")
    fun likeSecondComment(@PathVariable("id") comment: SecondComment): Set<Like> {
        return likeService.toggle(comment)
    }


}