package com.anjunar.technologyspeaks.core

import com.anjunar.technologyspeaks.rest.types.Data
import com.anjunar.technologyspeaks.security.LinkBuilder
import jakarta.annotation.security.RolesAllowed
import jakarta.json.bind.annotation.JsonbProperty
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController {

    @GetMapping(value = ["/core/users/user/{id}"], produces = ["application/json"])
    @RolesAllowed("User", "Administrator")
    fun read(@PathVariable("id") user : User): Data<User> {

        val form = Data(user, User.schema())

        form.addLinks(
            LinkBuilder.create(UserController::update)
                .build(),
            LinkBuilder.create(ManagedPropertyController::read)
                .withVariable("id", "")
                .build()
        )

        return form
    }

    @PutMapping(value = ["/core/users/user"], produces = ["application/json"], consumes = ["application/json"])
    @RolesAllowed("User", "Administrator")
    fun update(@RequestBody user : User): Data<User> {
        val form = Data(user.merge(), User.schema())

        form.addLinks(
            LinkBuilder.create(UserController::update)
                .build(),
            LinkBuilder.create(ManagedPropertyController::read)
                .withVariable("id", "")
                .build()
        )

        return form
    }
}