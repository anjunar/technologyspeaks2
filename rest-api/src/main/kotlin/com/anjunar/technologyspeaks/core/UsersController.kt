package com.anjunar.technologyspeaks.core

import com.anjunar.technologyspeaks.hibernate.search.HibernateSearch
import com.anjunar.technologyspeaks.rest.EntityGraph
import com.anjunar.technologyspeaks.security.LinkBuilder
import com.anjunar.technologyspeaks.rest.types.Data
import com.anjunar.technologyspeaks.rest.types.Table
import jakarta.annotation.security.RolesAllowed
import jakarta.json.bind.annotation.JsonbProperty
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class UsersController(val query: HibernateSearch) {

    @GetMapping(value = ["/core/users"], produces = ["application/json"])
    @RolesAllowed("User", "Administrator")
    @EntityGraph("User.full")
    fun list(search: UserSearch): Table<UserRow> {

        val searchContext = query.searchContext(search)

        val entities = query.entities(
            search.index,
            search.limit,
            User::class,
            UserRow::class,
            searchContext,
            { query, root, expressions, builder -> query.select(builder.construct(UserRow::class.java, root, expressions.firstOrNull() ?: builder.literal(1.0))) }
        )

        val count = query.count(User::class, searchContext)

        for (user in entities) {
            user.data.addLinks(
                LinkBuilder.create(UserController::read)
                    .withVariable("id", user.data.id)
                    .build()
            )
        }

        return Table(entities, count, User.schema())

    }


    companion object {
        class UserRow(data: User, @JsonbProperty val score: Double) : Data<User>(data, User.schema())
    }


}