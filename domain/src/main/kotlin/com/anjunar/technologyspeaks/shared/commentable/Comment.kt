package com.anjunar.technologyspeaks.shared.commentable

import com.anjunar.json.mapper.provider.EntityProvider
import com.anjunar.json.mapper.provider.OwnerProvider
import com.anjunar.json.mapper.schema.SchemaProvider
import com.anjunar.technologyspeaks.core.AbstractEntity
import com.anjunar.technologyspeaks.core.AbstractEntitySchema
import com.anjunar.technologyspeaks.core.User
import com.anjunar.technologyspeaks.hibernate.EntityContext
import com.anjunar.technologyspeaks.hibernate.RepositoryContext
import jakarta.json.bind.annotation.JsonbProperty
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "Shared#Comment")
class Comment : AbstractEntity(), EntityContext<Comment>, OwnerProvider {

    @ManyToOne(optional = false)
    @JsonbProperty
    lateinit var user: User

    @JsonbProperty
    lateinit var targetType: String

    @JsonbProperty
    lateinit var targetId: UUID

    @Column(columnDefinition = "text")
    @JsonbProperty
    var text: String = ""

    override fun owner(): EntityProvider = user

    companion object : RepositoryContext<Comment>(), SchemaProvider {

        class Schema : AbstractEntitySchema<Comment>() {
            val user = property(Comment::user)
            val targetType = property(Comment::targetType)
            val targetId = property(Comment::targetId)
            val text = property(Comment::text)
        }
    }
}

