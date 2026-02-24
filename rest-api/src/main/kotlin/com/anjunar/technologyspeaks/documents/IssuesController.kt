package com.anjunar.technologyspeaks.documents

import com.anjunar.technologyspeaks.hibernate.search.HibernateSearch
import com.anjunar.technologyspeaks.rest.EntityGraph
import com.anjunar.technologyspeaks.rest.types.Data
import com.anjunar.technologyspeaks.rest.types.Table
import com.anjunar.technologyspeaks.security.LinkBuilder
import jakarta.annotation.security.RolesAllowed
import jakarta.json.bind.annotation.JsonbProperty
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class IssuesController(val query: HibernateSearch) {

    @GetMapping(value = ["/document/documents/document/{id}/issues"], produces = ["application/json"])
    @RolesAllowed("User", "Administrator")
    @EntityGraph("Issue.full")
    fun list(search: IssueSearch): Table<IssueRow> {

        val searchContext = query.searchContext(search)

        val entities = query.entities(
            search.index,
            search.limit,
            Issue::class,
            IssueRow::class,
            searchContext,
            { query, root, expressions, builder -> query.select(builder.construct(IssueRow::class.java, root)) }
        )

        val count = query.count(Issue::class, searchContext)

        for (entity in entities) {
            entity.data.addLinks(
                LinkBuilder.create(IssueController::read)
                    .withVariable("id", entity.data.id)
                    .build()
            )
        }

        val table = Table(entities, count, Issue.schema())

        return table

    }


    companion object {
        class IssueRow(@JsonbProperty data: Issue) : Data<Issue>(data, Issue.schema())
    }

}