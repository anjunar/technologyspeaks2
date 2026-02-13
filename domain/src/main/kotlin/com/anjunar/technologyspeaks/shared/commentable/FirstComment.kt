package com.anjunar.technologyspeaks.shared.commentable

import com.anjunar.json.mapper.provider.EntityProvider
import com.anjunar.json.mapper.schema.DefaultWritableRule
import com.anjunar.json.mapper.schema.SchemaProvider
import com.anjunar.technologyspeaks.core.AbstractEntitySchema
import com.anjunar.technologyspeaks.hibernate.EntityContext
import com.anjunar.technologyspeaks.hibernate.RepositoryContext
import com.anjunar.technologyspeaks.shared.likeable.Like
import com.anjunar.technologyspeaks.shared.likeable.LikeContainer
import jakarta.json.bind.annotation.JsonbProperty
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "Shared#FirstComment")
class FirstComment : AbstractComment(), EntityContext<FirstComment>, LikeContainer.Interface {

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonbProperty
    val comments : MutableList<SecondComment> = ArrayList()

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonbProperty
    override val likes: MutableSet<Like> = mutableSetOf()

    override fun owner(): EntityProvider = user

    companion object : RepositoryContext<FirstComment>(), SchemaProvider {

        class Schema : AbstractEntitySchema<FirstComment>() {
            val user = property(FirstComment::user)
            val editor = property(FirstComment::editor, DefaultWritableRule())
            val comments = property(FirstComment::comments, DefaultWritableRule())
        }
    }
}

