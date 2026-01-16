package com.anjunar.technologyspeaks.core

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class MediaController {

    @GetMapping(value = ["/core/media/{id}"], produces = ["image/jpeg"])
    fun read(@PathVariable("id") media: Media): Media {
        return media
    }

}