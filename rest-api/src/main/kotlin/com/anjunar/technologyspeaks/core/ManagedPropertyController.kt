package com.anjunar.technologyspeaks.core

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ManagedPropertyController {

    @GetMapping(value = ["/core/properties/property/{id}"], produces = ["application/json"])
    fun read(@PathVariable("id") managedProperty: ManagedProperty) : ManagedProperty {
        return managedProperty
    }

    @PutMapping(value = ["/core/properties/property"], produces = ["application/json"], consumes = ["application/json"])
    fun update(managedProperty: ManagedProperty) : ManagedProperty {
        return managedProperty
    }

}