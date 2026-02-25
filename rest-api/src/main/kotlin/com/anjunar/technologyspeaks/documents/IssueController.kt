package com.anjunar.technologyspeaks.documents

import com.anjunar.technologyspeaks.rest.EntityGraph
import com.anjunar.technologyspeaks.rest.types.Data
import com.anjunar.technologyspeaks.security.IdentityHolder
import com.anjunar.technologyspeaks.security.LinkBuilder
import jakarta.annotation.security.RolesAllowed
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class IssueController(val identityHolder: IdentityHolder) {

    @GetMapping("/document/documents/document/{id}/issues/issue", produces = ["application/json"])
    @RolesAllowed("User", "Administrator")
    @EntityGraph("Issue.full")
    fun create(@PathVariable("id") document: Document) : Data<Issue> {

        val entity = Issue("Neue Aufgabe")

        entity.addLinks(
            LinkBuilder.create(IssueController::save)
                .withVariable("id", document.id)
                .build()
        )

        return Data(entity, Issue.schema())
    }

    @GetMapping("/document/documents/document/{document}/issues/issue/{id}", produces = ["application/json"])
    @RolesAllowed("User", "Administrator")
    @EntityGraph("Issue.full")
    fun read(@PathVariable("document") document: Document, @PathVariable("id") entity : Issue) : Data<Issue> {

        entity.addLinks(
            LinkBuilder.create(IssueController::update)
                .withVariable("id", entity.document.id)
                .build(),
            LinkBuilder.create(IssueController::delete)
                .build()
        )

        return Data(entity, Issue.schema())
    }

    @PostMapping("/document/documents/document/{id}/issues/issue", produces = ["application/json"], consumes = ["application/json"])
    @RolesAllowed("User", "Administrator")
    @EntityGraph("Issue.full")
    fun save(@PathVariable("id") document: Document, @RequestBody entity : Issue) : Data<Issue> {
        entity.document = document
        entity.user = identityHolder.user
        entity.persist()

        entity.addLinks(
            LinkBuilder.create(IssueController::update)
                .withVariable("id", entity.document.id)
                .build(),
            LinkBuilder.create(IssueController::delete)
                .build()
        )

        return Data(entity, Issue.schema())
    }

    @PutMapping("/document/documents/document/{id}/issues/issue", produces = ["application/json"], consumes = ["application/json"])
    @RolesAllowed("User", "Administrator")
    @EntityGraph("Issue.full")
    fun update(@PathVariable("id") document: Document, @RequestBody entity : Issue) : Data<Issue> {
        entity.document = document
        entity.user = identityHolder.user

        entity.addLinks(
            LinkBuilder.create(IssueController::update)
                .withVariable("id", entity.document.id)
                .build(),
            LinkBuilder.create(IssueController::delete)
                .build()
        )

        return Data(entity, Issue.schema())
    }

    @DeleteMapping("/document/documents/document/issues/issue", consumes = ["application/json"])
    @RolesAllowed("User", "Administrator")
    @EntityGraph("Issue.full")
    fun delete(@RequestBody entity : Issue) : ResponseEntity<Unit> {
        entity.remove()
        return ResponseEntity.ok().build()
    }


}