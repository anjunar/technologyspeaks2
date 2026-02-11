package com.anjunar.technologyspeaks.timeline

import com.anjunar.technologyspeaks.shared.likeable.Like
import com.anjunar.technologyspeaks.shared.likeable.LikeService
import jakarta.annotation.security.RolesAllowed
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class LikeController(val likeService: LikeService) {

    @PostMapping(value = ["/timeline/posts/post/{id}/like"], produces = ["application/json"])
    @RolesAllowed("User", "Administrator")
    fun like(@PathVariable("id") post: Post): Set<Like> {
        return likeService.toggle(post)
    }

}