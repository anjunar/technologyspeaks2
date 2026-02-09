package com.anjunar.technologyspeaks.shared.commentable

import jakarta.json.bind.annotation.JsonbProperty
import jakarta.persistence.CascadeType
import jakarta.persistence.OneToMany

object CommentContainer {

    interface Interface {
        val comments: MutableSet<Comment>
    }

}