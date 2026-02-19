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
import javax.print.Doc

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

        for (entity in entities) {
            entity.data.addLinks(
                LinkBuilder.create(DocumentController::read)
                    .withVariable("id", entity.data.id)
                    .build()
            )
        }

        val table = Table(entities, count, Document.schema())

        table.addLinks(
            LinkBuilder.create(DocumentController::create)
                .build()
        )

        return table

    }


    companion object {
        class DocumentRow(@JsonbProperty data: Document, @JsonbProperty val score: Double) : Data<Document>(data, Document.schema())
    }

}