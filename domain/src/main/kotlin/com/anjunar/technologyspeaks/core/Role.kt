package com.anjunar.technologyspeaks.core

import com.anjunar.technologyspeaks.hibernate.EntityContext
import com.anjunar.technologyspeaks.hibernate.RepositoryContext
import jakarta.json.bind.annotation.JsonbProperty
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Entity
@Table(name = "Core#Role")
class Role(
    @Size(min = 3, max = 80)
    @NotBlank
    @Column(unique = true)
    @JsonbProperty
    var name: String,

    @Size(min = 3, max = 80)
    @NotBlank
    @JsonbProperty
    var description: String
) : AbstractEntity(), EntityContext<Role> {

    companion object : RepositoryContext<Role>()

}