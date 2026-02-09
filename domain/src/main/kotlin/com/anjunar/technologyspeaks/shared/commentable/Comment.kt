package com.anjunar.technologyspeaks.shared.commentable

import com.anjunar.json.mapper.annotations.UseConverter
import com.anjunar.json.mapper.provider.EntityProvider
import com.anjunar.json.mapper.provider.OwnerProvider
import com.anjunar.json.mapper.schema.DefaultWritableRule
import com.anjunar.json.mapper.schema.SchemaProvider
import com.anjunar.technologyspeaks.core.AbstractEntity
import com.anjunar.technologyspeaks.core.AbstractEntitySchema
import com.anjunar.technologyspeaks.core.User
import com.anjunar.technologyspeaks.hibernate.EntityContext
import com.anjunar.technologyspeaks.hibernate.RepositoryContext
import com.anjunar.technologyspeaks.rest.types.DTO
import com.anjunar.technologyspeaks.shared.editor.Node
import com.anjunar.technologyspeaks.shared.editor.NodeType
import jakarta.json.bind.annotation.JsonbProperty
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Type
import java.util.UUID

@Entity
@Table(name = "Shared#Comment")
class Comment : AbstractEntity(), EntityContext<Comment>, OwnerProvider, DTO {

    @ManyToOne(optional = false)
    @JsonbProperty
    lateinit var user: User

    @Column(columnDefinition = "jsonb")
    @Type(NodeType::class)
    @UseConverter
    @JsonbProperty
    lateinit var editor: Node

    override fun owner(): EntityProvider = user

    companion object : RepositoryContext<Comment>(), SchemaProvider {

        class Schema : AbstractEntitySchema<Comment>() {
            val user = property(Comment::user)
            val editor = property(Comment::editor, DefaultWritableRule())
        }
    }
}

