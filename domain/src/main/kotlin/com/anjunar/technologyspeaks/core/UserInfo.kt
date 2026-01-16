package com.anjunar.technologyspeaks.core

import com.anjunar.json.mapper.provider.EntityProvider
import com.anjunar.json.mapper.provider.OwnerProvider
import com.anjunar.json.mapper.schema.EntitySchema
import com.anjunar.json.mapper.schema.SchemaProvider
import jakarta.json.bind.annotation.JsonbProperty
import jakarta.persistence.Entity
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate

@Entity
@Table(name = "Core#UserInfo")
class UserInfo(
    @NotBlank
    @Size(min = 2, max = 80)
    @JsonbProperty
    var firstName: String,

    @NotBlank
    @Size(min = 2, max = 80)
    @JsonbProperty
    var lastName: String,

    @NotNull
    @JsonbProperty
    var birthDate: LocalDate
) : AbstractEntity(), OwnerProvider {

    @OneToOne(optional = false, mappedBy = "info")
    lateinit var user: User

    override fun owner(): EntityProvider {
        return user.owner()
    }

    companion object : SchemaProvider {

        class Schema : EntitySchema<UserInfo>() {
            @JsonbProperty val firstName = property(UserInfo::firstName, ManagedRule())
            @JsonbProperty val lastName = property(UserInfo::lastName, ManagedRule())
            @JsonbProperty val birthDate = property(UserInfo::birthDate, ManagedRule())
        }

    }

}