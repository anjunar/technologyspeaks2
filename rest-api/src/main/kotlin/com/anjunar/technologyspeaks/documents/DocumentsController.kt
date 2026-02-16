package com.anjunar.technologyspeaks.documents

import com.anjunar.technologyspeaks.core.User
import com.anjunar.technologyspeaks.core.UserController
import com.anjunar.technologyspeaks.core.UserSearch
import com.anjunar.technologyspeaks.hibernate.search.HibernateSearch
import com.anjunar.technologyspeaks.rest.EntityGraph
import com.anjunar.technologyspeaks.rest.types.Data
import com.anjunar.technologyspeaks.rest.types.Table
import com.anjunar.technologyspeaks.security.LinkBuilder
import jakarta.annotation.security.RolesAllowed
import jakarta.json.bind.annotation.JsonbProperty
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class DocumentsController(val query: HibernateSearch) {

    @GetMapping(value = ["/document/documents"], produces = ["application/json"])
    @RolesAllowed("User", "Administrator")
    @EntityGraph("Document.full")
    fun list(search: DocumentSearch): Table<DocumentRow> {

        val searchContext = query.searchContext(search)

        val entities = query.entities(
            search.index,
            search.limit,
            Document::class,
            DocumentRow::class,
            searchContext,
            { query, root, expressions, builder -> query.select(builder.construct(DocumentRow::class.java, root, expressions.firstOrNull() ?: builder.literal(1.0))) }
        )

        val count = query.count(Document::class, searchContext)

        for (user in entities) {
            user.data.addLinks(
                LinkBuilder.create(UserController::read)
                    .withVariable("id", user.data.id)
                    .build()
            )
        }

        return Table(entities, count, Document.schema())

    }


    companion object {
        class DocumentRow(data: User, @JsonbProperty val score: Double) : Data<User>(data, User.schema())
    }

}