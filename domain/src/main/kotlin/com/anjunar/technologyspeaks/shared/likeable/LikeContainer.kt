package com.anjunar.technologyspeaks.shared.likeable

import jakarta.json.bind.annotation.JsonbProperty
import jakarta.persistence.CascadeType
import jakarta.persistence.OneToMany

object LikeContainer {

    interface Interface {

        val likes: MutableSet<Like>

    }

}