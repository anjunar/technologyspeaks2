package com.anjunar.technologyspeaks.documents

import com.anjunar.technologyspeaks.rest.EntityGraph
import com.anjunar.technologyspeaks.rest.types.Data
import com.anjunar.technologyspeaks.security.IdentityHolder
import com.anjunar.technologyspeaks.security.LinkBuilder
import com.anjunar.technologyspeaks.shared.editor.Node
import jakarta.annotation.security.RolesAllowed
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class DocumentController(val identityHolder: IdentityHolder) {

    @PostMapping(value = ["/document/documents/document"], produces = ["application/json"])
    @RolesAllowed("Document", "Administrator")
    @EntityGraph("Document.full")
    fun root(): Data<Document> {

        var entity = Document.query("title" to "Technology Speaks")

        if (entity == null) {
            entity = Document("Technology Speaks")
            entity.user = identityHolder.user
            val node = Node()
            node.type = "doc"
            entity.editor = node
            entity.persist()
        }

        val form = Data(entity, Document.schema())

        entity.addLinks(
            LinkBuilder.create(DocumentController::update)
                .build()
        )

        return form
    }

    @GetMapping(value = ["/document/documents/document/{id}"], produces = ["application/json"])
    @RolesAllowed("Document", "Administrator")
    @EntityGraph("Document.full")
    fun read(@PathVariable("id") entity : Document): Data<Document> {

        val form = Data(entity, Document.schema())

        entity.addLinks(
            LinkBuilder.create(DocumentController::update)
                .build()
        )

        return form
    }

    @PutMapping(value = ["/document/documents/document"], produces = ["application/json"], consumes = ["application/json"])
    @RolesAllowed("Document", "Administrator")
    @EntityGraph("Document.full")
    fun update(@RequestBody entity : Document): Data<Document> {
        val form = Data(entity, Document.schema())

        entity.addLinks(
            LinkBuilder.create(DocumentController::update)
                .build()
        )

        return form
    }
}