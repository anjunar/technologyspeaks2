package com.anjunar.technologyspeaks.timeline

import com.anjunar.technologyspeaks.core.UserController
import com.anjunar.technologyspeaks.security.LinkBuilder
import com.anjunar.technologyspeaks.rest.types.Table
import jakarta.annotation.security.RolesAllowed
import jakarta.persistence.EntityManager
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PostController(val entityManager: EntityManager) {

    @GetMapping(value = ["/timeline/posts"], produces = ["application/json"])
    @RolesAllowed("User", "Administrator")
    fun list(): Table<Post> {

        val entities = entityManager.createQuery("from Post p", Post::class.java)
            .resultList

        return Table(entities, entities.size.toLong(), Post.schema())

    }


}