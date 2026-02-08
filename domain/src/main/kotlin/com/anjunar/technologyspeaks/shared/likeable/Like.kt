package com.anjunar.technologyspeaks.shared.likeable

import com.anjunar.technologyspeaks.core.AbstractEntity
import com.anjunar.technologyspeaks.core.User
import jakarta.json.bind.annotation.JsonbProperty
import jakarta.persistence.Entity
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "Shared#Like")
class Like : AbstractEntity() {

    @ManyToOne(optional = false)
    @JsonbProperty
    lateinit var user: User

}
