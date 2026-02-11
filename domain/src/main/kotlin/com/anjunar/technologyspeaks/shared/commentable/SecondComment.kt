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
import com.anjunar.json.mapper.provider.DTO
import com.anjunar.technologyspeaks.shared.editor.Node
import com.anjunar.technologyspeaks.shared.editor.NodeType
import jakarta.json.bind.annotation.JsonbProperty
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Type

@Entity
@Table(name = "Shared#SecondComment")
class SecondComment : AbstractComment(), EntityContext<SecondComment> {

    companion object : RepositoryContext<SecondComment>(), SchemaProvider {

        class Schema : AbstractEntitySchema<SecondComment>() {
            val user = property(SecondComment::user)
            val editor = property(SecondComment::editor, DefaultWritableRule())
        }
    }
}