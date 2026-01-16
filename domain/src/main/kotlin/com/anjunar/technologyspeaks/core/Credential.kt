package com.anjunar.technologyspeaks.core

import com.anjunar.technologyspeaks.hibernate.RepositoryContext
import jakarta.json.bind.annotation.JsonbProperty
import jakarta.persistence.Entity
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.Size

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "Core#Credential")
class Credential() : AbstractEntity() {

    @ManyToMany
    @Size(min = 1, max = 10)
    @JsonbProperty
    val roles: MutableSet<Role> = HashSet()

    @ManyToOne(optional = false)
    lateinit var email : EMail

    companion object : RepositoryContext<Credential>()

}