package com.anjunar.technologyspeaks.core

import com.anjunar.json.mapper.provider.EntityProvider
import com.anjunar.json.mapper.provider.OwnerProvider
import com.anjunar.json.mapper.schema.EntitySchema
import com.anjunar.json.mapper.schema.SchemaProvider
import com.anjunar.technologyspeaks.hibernate.EntityContext
import com.anjunar.technologyspeaks.hibernate.RepositoryContext
import jakarta.json.bind.annotation.JsonbProperty
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

@Entity
@Table(name = "Core#Email")
class EMail(@JsonbProperty @Email @NotBlank @Column(unique = true) var value: String)
    : AbstractEntity(), OwnerProvider, EntityContext<EMail> {

    @ManyToOne(optional = false)
    lateinit var user: User

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, mappedBy = "email")
    val credentials : MutableSet<Credential> = HashSet()

    override fun owner(): EntityProvider {
        return user.owner()
    }

    companion object : SchemaProvider, RepositoryContext<EMail>() {

        class Schema : EntitySchema<EMail>() {
            @JsonbProperty val id = property(EMail::id, OwnerRule())
            @JsonbProperty val value = property(EMail::value, OwnerRule())
        }

    }

}
