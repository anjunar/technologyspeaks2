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
import jakarta.validation.constraints.Size

@Entity
@Table(name = "Core#Address")
class Address (
    @NotBlank
    @Size(min = 2, max = 80)
    @JsonbProperty
    var street: String,

    @NotBlank
    @Size(min = 1, max = 80)
    @JsonbProperty
    var number: String,

    @NotBlank
    @Size(min = 5, max = 5)
    @JsonbProperty
    var zipCode: String,

    @NotBlank
    @Size(min = 2, max = 80)
    @JsonbProperty
    var country: String
) : AbstractEntity(), OwnerProvider {

    @OneToOne(optional = false, mappedBy = "address")
    lateinit var user: User

    override fun owner(): EntityProvider {
        return user.owner()
    }

    companion object : SchemaProvider {

        class Schema : EntitySchema<Address>() {

            @JsonbProperty val street = property(Address::street, ManagedRule())
            @JsonbProperty val number = property(Address::number, ManagedRule())
            @JsonbProperty val zipCode = property(Address::zipCode, ManagedRule())
            @JsonbProperty val country = property(Address::country, ManagedRule())

        }

    }

}