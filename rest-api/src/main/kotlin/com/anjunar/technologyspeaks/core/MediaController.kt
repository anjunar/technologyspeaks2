package com.anjunar.technologyspeaks.core

import jakarta.annotation.security.RolesAllowed
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class MediaController {

    @GetMapping(value = ["/core/media/{id}"], produces = ["image/jpeg", "image/png", "image/gif"])
    @Transactional
    @RolesAllowed("User", "Administrator")
    fun media(@PathVariable("id") media: Media): ResponseEntity<ByteArray> {
        return ResponseEntity.ok()
            .header("Content-Type", media.contentType)
            .contentLength(media.data.size.toLong())
            .body(media.data)
    }

    @GetMapping(value = ["/core/media/{id}/thumbnail"], produces = ["image/jpeg", "image/png", "image/gif"])
    @Transactional
    @RolesAllowed("User", "Administrator")
    fun thumbnail(@PathVariable("id") media: Media): ResponseEntity<ByteArray> {
        return ResponseEntity.ok()
            .header("Content-Type", media.thumbnail.contentType)
            .contentLength(media.thumbnail.data.size.toLong())
            .body(media.thumbnail.data)
    }


}