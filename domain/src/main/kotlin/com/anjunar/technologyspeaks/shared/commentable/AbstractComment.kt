package com.anjunar.technologyspeaks.shared.commentable

import com.anjunar.json.mapper.annotations.UseConverter
import com.anjunar.json.mapper.provider.EntityProvider
import com.anjunar.json.mapper.provider.OwnerProvider
import com.anjunar.technologyspeaks.core.AbstractEntity
import com.anjunar.technologyspeaks.core.User
import com.anjunar.technologyspeaks.shared.editor.Node
import com.anjunar.technologyspeaks.shared.editor.NodeType
import jakarta.json.bind.annotation.JsonbProperty
import jakarta.persistence.Column
import jakarta.persistence.ManyToOne
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.Type

@MappedSuperclass
abstract class AbstractComment : AbstractEntity(), OwnerProvider {

    @ManyToOne(optional = false)
    @JsonbProperty
    lateinit var user: User

    @Column(columnDefinition = "jsonb")
    @Type(NodeType::class)
    @JsonbProperty
    lateinit var editor: Node

    override fun owner(): EntityProvider = user

}